// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.event;

import com.elfmcys.ysm.platform.capability.ICapabilityHolder;
import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.capability.PlayerAnimatableCapabilityProvider;
import com.elfmcys.ysm.client.entity.FirstPersonPart;
import com.elfmcys.ysm.config.ClientConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;

/**
 * The model's first-person body background, drawn once per frame in the hand pass. Fabric port: called from
 * ItemInHandRendererMixin (was Forge's RenderHandEvent); the per-frame reset runs when the level render starts.
 */
public final class RenderFirstPlayerBackground {
    // A successful hand phase consumes the background once, independently of animation time.
    private static LocalPlayer renderedPlayer;

    private RenderFirstPlayerBackground() {}

    public static void reset() {
        renderedPlayer = null;
    }

    public static void onHands(float partialTick, PoseStack poseStack, SubmitNodeCollector collector,
        int packedLight) {
        if (!YesSteveModel.isAvailable() || ClientConfig.DISABLE_SELF_MODEL.get()
            || ClientConfig.DISABLE_SELF_HANDS.get())
            return;
        var player = Minecraft.getInstance().player;
        if (player == null || renderedPlayer == player)
            return;
        var cap = ICapabilityHolder.of(player).getCapability(PlayerAnimatableCapabilityProvider.CAP).orElse(null);
        if (cap == null)
            return;
        var renderer = RegisterEntityRenderersEvent.getFirstPersonArmRenderer();
        poseStack.pushPose();
        try {
            if (Minecraft.getInstance().options.bobView().get())
                bobView(poseStack, partialTick, player);
            poseStack.translate(0, -1.5, 0);
            if (renderer.render(player, cap, FirstPersonPart.BACKGROUND, poseStack, collector, packedLight,
                OverlayTexture.NO_OVERLAY, partialTick))
                renderedPlayer = player;
        } finally {
            poseStack.popPose();
        }
    }

    /** 1.20 view bobbing on 26.x's client avatar state (walk distance and bob). */
    private static void bobView(PoseStack poseStack, float partialTick, LocalPlayer player) {
        //#if MC >= 1.21.9
        var avatar = player.avatarState();
        float phase = avatar.getBackwardsInterpolatedWalkDistance(partialTick);
        float bob = avatar.getInterpolatedBob(partialTick);
        //#else
        //$$ // before 1.21.9 the player keeps walk distance and bob (GameRenderer.bobView reads them the same way)
        //$$ float phase = -(player.walkDist + (player.walkDist - player.walkDistO) * partialTick);
        //$$ float bob = Mth.lerp(partialTick, player.oBob, player.bob);
        //#endif
        poseStack.translate(-Mth.sin(phase * (float)Math.PI) * bob * 0.5F,
            Math.abs(Mth.cos(phase * (float)Math.PI) * bob), 0);
        com.elfmcys.ysm.platform.McCompat.rotate(poseStack, Axis.ZN.rotationDegrees(Mth.sin(phase * (float)Math.PI) * bob * 3));
        com.elfmcys.ysm.platform.McCompat.rotate(poseStack, Axis.XN.rotationDegrees(Math.abs(Mth.cos(phase * (float)Math.PI - 0.2F) * bob) * 5));
    }
}
