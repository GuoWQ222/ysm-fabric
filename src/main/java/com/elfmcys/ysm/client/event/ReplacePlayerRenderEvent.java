// Modified for the unofficial Fabric 26.2 port by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.event;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.capability.PlayerAnimatableCapabilityProvider;
import com.elfmcys.ysm.client.compat.FirstPersonCompat;
import com.elfmcys.ysm.client.compat.PlayerAnimatorCompat;
import com.elfmcys.ysm.client.compat.realcamera.RealCameraCompat;
import com.elfmcys.ysm.config.ClientConfig;
import com.elfmcys.ysm.platform.render.YsmRenderStateKeys;
import com.elfmcys.ysm.util.PersonView;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

/**
 * Replaces a player's vanilla body with its YSM model. Fabric port: called from LivingEntityRendererMixin at the start
 * of the vanilla submit (was a RenderPlayerEvent.Pre subscriber).
 */
public final class ReplacePlayerRenderEvent {
    private ReplacePlayerRenderEvent() {}

    /** @return true when YSM owns this draw and vanilla must skip its body and layers */
    public static boolean tryReplace(AbstractClientPlayer player, LivingEntityRenderState state, PoseStack poseStack,
        SubmitNodeCollector collector) {
        if (!YesSteveModel.isAvailable())
            return false;
        var self = Minecraft.getInstance().player;
        if (player.equals(self) && ClientConfig.DISABLE_SELF_MODEL.get())
            return false;
        if (!player.equals(self) && ClientConfig.DISABLE_OTHER_MODEL.get())
            return false;
        if (player.isSpectator())
            return false;
        var cap = player.getCapability(PlayerAnimatableCapabilityProvider.CAP).orElse(null);
        if (cap == null || !cap.isPresentationAllowed())
            return false;
        if (PersonView.isFirstPersonView(cap) && !FirstPersonCompat.isRenderingPlayer() && !RealCameraCompat.isActive()
            && !ClientConfig.DISABLE_EXTERNAL_FIRST_PERSON_ANIM.get() && PlayerAnimatorCompat.hasThirdPersonModelAnim(self))
            return false;
        boolean[] replaced = {false};
        RegisterEntityRenderersEvent.getPlayerRenderer().renderReplacement(cap, state,
            YsmRenderStateKeys.partialTick(state), poseStack, collector, () -> replaced[0] = true);
        return replaced[0];
    }
}
