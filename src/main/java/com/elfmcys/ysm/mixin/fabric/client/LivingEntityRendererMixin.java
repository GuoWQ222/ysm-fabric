/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.mixin.fabric.client;

import com.elfmcys.ysm.client.event.ReplacePlayerRenderEvent;
import com.elfmcys.ysm.platform.render.YsmRenderStateKeys;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fabric port of the RenderPlayerEvent.Pre replacement: YSM submits a player's body first; vanilla then skips its
 * own model and layers for that state but still submits name tag and leash.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {
    @Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
        at = @At("HEAD"))
    private void ysm$replaceBody(LivingEntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector,
        CameraRenderState camera, CallbackInfo ci) {
        YsmRenderStateKeys.setReplaced(state, false);
        if (state instanceof AvatarRenderState && YsmRenderStateKeys.entity(state) instanceof AbstractClientPlayer player
            && com.elfmcys.ysm.client.compat.FirstPersonCompat.submitExtracted(
                YsmRenderStateKeys.firstPersonModel(state),
                () -> ReplacePlayerRenderEvent.tryReplace(player, state, poseStack, collector)))
            YsmRenderStateKeys.setReplaced(state, true);
    }

    @ModifyExpressionValue(method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;getRenderType(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;ZZZ)Lnet/minecraft/client/renderer/rendertype/RenderType;"))
    private RenderType ysm$skipVanillaModel(RenderType original, LivingEntityRenderState state) {
        return YsmRenderStateKeys.isReplaced(state) ? null : original;
    }

    @ModifyExpressionValue(method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;shouldRenderLayers(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;)Z"))
    private boolean ysm$skipVanillaLayers(boolean original, LivingEntityRenderState state) {
        return original && !YsmRenderStateKeys.isReplaced(state);
    }

    @Shadow
    @Final
    protected java.util.List<?> layers;

    /**
     * The layers of a YSM-drawn player are skipped (YSM draws armour, held items, cape etc. itself), but Carry On (from
     * Minecraft 1.21.9) draws the carried block or entity through its own layer on the player renderer: those layers
     * still run, where vanilla runs its layers (the pose is in model space here), with vanilla's arguments.
     */
    @Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;shouldRenderLayers(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;)Z"))
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void ysm$carryOnLayers(LivingEntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector,
        CameraRenderState camera, CallbackInfo ci) {
        if (!YsmRenderStateKeys.isReplaced(state) || !com.elfmcys.ysm.client.compat.carryon.CarryOnCompat.isInstalled())
            return;
        for (Object layer : layers) {
            if (layer.getClass().getName().startsWith("tschipp.carryon."))
                ((net.minecraft.client.renderer.entity.layers.RenderLayer)layer).submit(poseStack, collector,
                    state.lightCoords, state, state.yRot, state.xRot);
        }
    }
}
