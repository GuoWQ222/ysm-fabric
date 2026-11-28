/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.legacy.submit.mixin;

import com.elfmcys.ysm.client.event.RenderFirstPlayerBackground;
import com.elfmcys.ysm.client.event.ReplacePlayerHandRenderEvent;
import com.elfmcys.ysm.legacy.render.SubmitNodeCollector;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Minecraft before 1.21.9 (ItemInHandRendererMixin's counterpart): YSM's first-person model draws behind the hands
 * and replaces the vanilla arms, which these versions draw straight into the frame's buffers.
 */
@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererLegacyMixin {
    @Inject(method = "renderHandsWithItems", at = @At("HEAD"))
    private void ysm$background(float frameInterp, PoseStack poseStack, MultiBufferSource.BufferSource buffers,
        LocalPlayer player, int lightCoords, CallbackInfo ci) {
        try (var collector = new SubmitNodeCollector(buffers)) {
            RenderFirstPlayerBackground.onHands(frameInterp, poseStack, collector, lightCoords);
        }
    }

    //#if MC >= 1.21.2
    @WrapOperation(method = {"renderPlayerArm", "renderMapHand"}, at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/renderer/entity/player/PlayerRenderer;renderRightHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/resources/ResourceLocation;Z)V"))
    private void ysm$rightArm(PlayerRenderer renderer, PoseStack poseStack, MultiBufferSource buffers, int lightCoords,
        ResourceLocation skinTexture, boolean hasSleeve, Operation<Void> original) {
        boolean replaced;
        try (var collector = new SubmitNodeCollector(buffers)) {
            replaced = ReplacePlayerHandRenderEvent.tryRender(HumanoidArm.RIGHT, poseStack, collector, lightCoords);
        }
        if (!replaced)
            original.call(renderer, poseStack, buffers, lightCoords, skinTexture, hasSleeve);
    }
    //#else
    //$$ // before 1.21.2 the hand renderers take the player
    //$$ @WrapOperation(method = {"renderPlayerArm", "renderMapHand"}, at = @At(value = "INVOKE",
    //$$     target = "Lnet/minecraft/client/renderer/entity/player/PlayerRenderer;renderRightHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;)V"))
    //$$ private void ysm$rightArm(PlayerRenderer renderer, PoseStack poseStack, MultiBufferSource buffers, int lightCoords,
    //$$     net.minecraft.client.player.AbstractClientPlayer player, Operation<Void> original) {
    //$$     boolean replaced;
    //$$     try (var collector = new SubmitNodeCollector(buffers)) {
    //$$         replaced = ReplacePlayerHandRenderEvent.tryRender(HumanoidArm.RIGHT, poseStack, collector, lightCoords);
    //$$     }
    //$$     if (!replaced)
    //$$         original.call(renderer, poseStack, buffers, lightCoords, player);
    //$$ }
    //#endif

    //#if MC >= 1.21.2
    @WrapOperation(method = {"renderPlayerArm", "renderMapHand"}, at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/renderer/entity/player/PlayerRenderer;renderLeftHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/resources/ResourceLocation;Z)V"))
    private void ysm$leftArm(PlayerRenderer renderer, PoseStack poseStack, MultiBufferSource buffers, int lightCoords,
        ResourceLocation skinTexture, boolean hasSleeve, Operation<Void> original) {
        boolean replaced;
        try (var collector = new SubmitNodeCollector(buffers)) {
            replaced = ReplacePlayerHandRenderEvent.tryRender(HumanoidArm.LEFT, poseStack, collector, lightCoords);
        }
        if (!replaced)
            original.call(renderer, poseStack, buffers, lightCoords, skinTexture, hasSleeve);
    }
    //#else
    //$$ // before 1.21.2 the hand renderers take the player
    //$$ @WrapOperation(method = {"renderPlayerArm", "renderMapHand"}, at = @At(value = "INVOKE",
    //$$     target = "Lnet/minecraft/client/renderer/entity/player/PlayerRenderer;renderLeftHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;)V"))
    //$$ private void ysm$leftArm(PlayerRenderer renderer, PoseStack poseStack, MultiBufferSource buffers, int lightCoords,
    //$$     net.minecraft.client.player.AbstractClientPlayer player, Operation<Void> original) {
    //$$     boolean replaced;
    //$$     try (var collector = new SubmitNodeCollector(buffers)) {
    //$$         replaced = ReplacePlayerHandRenderEvent.tryRender(HumanoidArm.LEFT, poseStack, collector, lightCoords);
    //$$     }
    //$$     if (!replaced)
    //$$         original.call(renderer, poseStack, buffers, lightCoords, player);
    //$$ }
    //#endif
}
