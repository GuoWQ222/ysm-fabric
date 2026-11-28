/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.legacy.submit.mixin;

import com.elfmcys.ysm.client.event.ReplacePlayerRenderEvent;
import com.elfmcys.ysm.legacy.render.SubmitNodeCollector;
import com.elfmcys.ysm.legacy.state.StateExtractor;
import com.elfmcys.ysm.platform.render.YsmRenderStateKeys;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Minecraft before 1.21.2 (the 1.21.2 - 1.21.8 mixin of the same name, for entity-based rendering): vanilla draws a
 * living entity in render(entity, yaw, partialTick, poseStack, buffers, light). YSM draws a player's body first from a
 * render state built from the player; vanilla then skips its own model and layers for that call but still draws name
 * tag and leash.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererLegacyMixin {
    private static final String RENDER = "render(Lnet/minecraft/world/entity/LivingEntity;FF"
        + "Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V";

    @Inject(method = RENDER, at = @At("HEAD"))
    private void ysm$replaceBody(LivingEntity entity, float yaw, float partialTick, PoseStack poseStack,
        MultiBufferSource buffers, int light, CallbackInfo ci, @Share("ysmReplaced") LocalBooleanRef replaced) {
        replaced.set(false);
        if (entity instanceof AbstractClientPlayer player) {
            var state = (com.elfmcys.ysm.legacy.state.LivingEntityRenderState)StateExtractor.extract(player, partialTick);
            YsmRenderStateKeys.setLight(state, light);
            try (var collector = new SubmitNodeCollector(buffers)) {
                if (ReplacePlayerRenderEvent.tryReplace(player, state, poseStack, collector))
                    replaced.set(true);
            }
        }
    }

    @ModifyExpressionValue(method = RENDER, at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;getRenderType(Lnet/minecraft/world/entity/LivingEntity;ZZZ)Lnet/minecraft/client/renderer/RenderType;"))
    private RenderType ysm$skipVanillaModel(RenderType original, @Share("ysmReplaced") LocalBooleanRef replaced) {
        return replaced.get() ? null : original;
    }

    @WrapWithCondition(method = RENDER, at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/renderer/entity/layers/RenderLayer;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/Entity;FFFFFF)V"))
    private boolean ysm$skipVanillaLayers(RenderLayer<?, ?> layer, PoseStack poseStack, MultiBufferSource buffers,
        int light, Entity entity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
        float netHeadYaw, float headPitch, @Share("ysmReplaced") LocalBooleanRef replaced) {
        return !replaced.get();
    }
}
