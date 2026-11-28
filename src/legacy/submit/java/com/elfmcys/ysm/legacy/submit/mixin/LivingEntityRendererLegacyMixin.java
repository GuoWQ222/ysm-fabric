/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.legacy.submit.mixin;

import com.elfmcys.ysm.client.event.ReplacePlayerRenderEvent;
import com.elfmcys.ysm.legacy.render.SubmitNodeCollector;
import com.elfmcys.ysm.platform.render.YsmRenderStateKeys;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Minecraft before 1.21.9 (LivingEntityRendererMixin's counterpart): vanilla draws a living entity in render(state,
 * poseStack, buffers, light). YSM draws a player's body first; vanilla then skips its own model and layers for that
 * state but still draws name tag and leash.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererLegacyMixin {
    private static final String RENDER = "render(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;"
        + "Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V";

    @Inject(method = RENDER, at = @At("HEAD"))
    private void ysm$replaceBody(LivingEntityRenderState state, PoseStack poseStack, MultiBufferSource buffers,
        int light, CallbackInfo ci) {
        YsmRenderStateKeys.setReplaced(state, false);
        YsmRenderStateKeys.setLight(state, light);
        if (state instanceof PlayerRenderState && YsmRenderStateKeys.entity(state) instanceof AbstractClientPlayer player) {
            try (var collector = new SubmitNodeCollector(buffers)) {
                if (ReplacePlayerRenderEvent.tryReplace(player, state, poseStack, collector))
                    YsmRenderStateKeys.setReplaced(state, true);
            }
        }
    }

    @ModifyExpressionValue(method = RENDER, at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;getRenderType(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;ZZZ)Lnet/minecraft/client/renderer/RenderType;"))
    private RenderType ysm$skipVanillaModel(RenderType original, LivingEntityRenderState state) {
        return YsmRenderStateKeys.isReplaced(state) ? null : original;
    }

    @ModifyExpressionValue(method = RENDER, at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;shouldRenderLayers(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;)Z"))
    private boolean ysm$skipVanillaLayers(boolean original, LivingEntityRenderState state) {
        return original && !YsmRenderStateKeys.isReplaced(state);
    }
}
