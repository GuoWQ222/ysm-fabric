// Modified for the unofficial Fabric 26.2 port by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.renderer;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.capability.PlayerAnimatableCapability;
import com.elfmcys.ysm.client.entity.CustomFirstPersonArmEntity;
import com.elfmcys.ysm.client.entity.FirstPersonPart;
import com.elfmcys.ysm.config.ClientConfig;
import com.elfmcys.ysm.event.api.SpecialPlayerRenderEvent;
import com.elfmcys.ysm.geckolib3.core.util.Color;
import com.elfmcys.ysm.geckolib3.geo.CustomTranslucentRenderType;
import com.elfmcys.ysm.platform.event.MinecraftForge;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.HumanoidArm;

/**
 * First-person arms and body background from the model's first-person parts.
 *
 * <p>
 * Fabric port (26.x): the part is prepared at submit time and drawn from a custom-geometry callback; the callback
 * re-checks the prepared part (renderPart rejects stale revisions), so a revoked model draws nothing.
 */
public class CustomFirstPersonArmRenderer {
    private CustomFirstPersonArmEntity armEntity = null;

    public boolean render(LocalPlayer player, PlayerAnimatableCapability cap, HumanoidArm arm, PoseStack poseStack,
        SubmitNodeCollector collector, int packedLight, float partialTick) {
        return render(player, cap, FirstPersonPart.forArm(arm), poseStack, collector, packedLight,
            OverlayTexture.NO_OVERLAY, partialTick);
    }

    /** @return true when the part was submitted, so the vanilla arm must be skipped */
    public boolean render(LocalPlayer player, PlayerAnimatableCapability cap, FirstPersonPart part, PoseStack poseStack,
        SubmitNodeCollector collector, int packedLight, int overlay, float partialTick) {
        if (!canRender(player, cap)) {
            release();
            return false;
        }
        if (armEntity == null || !armEntity.isAssigned() || armEntity.getEntity() != player
            || armEntity.getMainModelEntity() != cap) {
            release();
            armEntity = new CustomFirstPersonArmEntity(player, cap);
        }
        var owner = armEntity;
        var prepared = owner.preparePart(part, partialTick);
        if (prepared == null || !owner.canConsume(prepared))
            return false;
        var renderEvent = new SpecialPlayerRenderEvent(player, cap, owner.getInstalledModelId());
        if (MinecraftForge.EVENT_BUS.post(renderEvent))
            return false;
        var texture = renderEvent.getTextureLocationOverride() == null ? owner.getTextureLocation()
            : renderEvent.getTextureLocationOverride();
        var renderType = CustomTranslucentRenderType.create(texture);
        poseStack.pushPose();
        try {
            if (part != FirstPersonPart.BACKGROUND) {
                poseStack.translate(part == FirstPersonPart.LEFT_ARM ? 0.25 : -0.25, 1.8, 0);
                poseStack.scale(-1, -1, 1);
            }
            int color = Color.WHITE.getColor();
            collector.submitCustomGeometry(poseStack, renderType, (pose, buffer) -> {
                if (owner != armEntity || !canRender(player, cap))
                    return;
                var consumed = owner.renderPart(prepared, () -> buffer, pose, packedLight, overlay, color);
                if (consumed && part != FirstPersonPart.BACKGROUND)
                    owner.countRender();
            });
            return true;
        } finally {
            poseStack.popPose();
        }
    }

    private static boolean canRender(LocalPlayer player, PlayerAnimatableCapability cap) {
        return YesSteveModel.isAvailable() && !ClientConfig.DISABLE_SELF_MODEL.get()
            && !ClientConfig.DISABLE_SELF_HANDS.get() && player != null && player == Minecraft.getInstance().player
            && cap.isPresentationAllowed();
    }

    public void release() {
        var previous = armEntity;
        armEntity = null;
        if (previous != null)
            previous.retire();
    }

    public void releaseForPlayer(LocalPlayer player) {
        if (armEntity != null && armEntity.getEntity() == player)
            release();
    }
}
