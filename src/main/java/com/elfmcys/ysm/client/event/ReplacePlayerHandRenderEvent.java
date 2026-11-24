// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.event;

import com.elfmcys.ysm.platform.capability.ICapabilityHolder;
import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.capability.PlayerAnimatableCapabilityProvider;
import com.elfmcys.ysm.config.ClientConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.entity.HumanoidArm;

/**
 * Replaces the bare first-person arm with the model's arm. Fabric port: called from ItemInHandRendererMixin where
 * vanilla draws the arm (was Forge's RenderArmEvent).
 */
public final class ReplacePlayerHandRenderEvent {
    private ReplacePlayerHandRenderEvent() {}

    /** @return true when YSM drew the arm and vanilla must skip it */
    public static boolean tryRender(HumanoidArm arm, PoseStack poseStack, SubmitNodeCollector collector,
        int packedLight) {
        if (!YesSteveModel.isAvailable() || ClientConfig.DISABLE_SELF_MODEL.get()
            || ClientConfig.DISABLE_SELF_HANDS.get())
            return false;
        var player = Minecraft.getInstance().player;
        if (player == null)
            return false;
        var cap = ICapabilityHolder.of(player).getCapability(PlayerAnimatableCapabilityProvider.CAP).orElse(null);
        if (cap == null)
            return false;
        float partialTick = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
        return RegisterEntityRenderersEvent.getFirstPersonArmRenderer().render(player, cap, arm, poseStack, collector,
            packedLight, partialTick);
    }
}
