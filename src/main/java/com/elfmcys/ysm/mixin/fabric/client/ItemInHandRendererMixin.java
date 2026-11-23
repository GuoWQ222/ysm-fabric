/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.mixin.fabric.client;

import com.elfmcys.ysm.client.event.RenderFirstPlayerBackground;
import com.elfmcys.ysm.client.event.ReplacePlayerHandRenderEvent;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Fabric port of Forge's RenderHandEvent / RenderArmEvent hooks for YSM's first-person model. */
@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {
    @Inject(method = "submitHandsWithItems", at = @At("HEAD"))
    private void ysm$background(float frameInterp, PoseStack poseStack, SubmitNodeCollector collector,
        LocalPlayer player, int lightCoords, CallbackInfo ci) {
        RenderFirstPlayerBackground.onHands(frameInterp, poseStack, collector, lightCoords);
    }

    @WrapOperation(method = {"renderPlayerArm", "renderMapHand"},
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/player/AvatarRenderer;renderRightHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/Identifier;Z)V"))
    private void ysm$rightArm(AvatarRenderer<?> renderer, PoseStack poseStack, SubmitNodeCollector collector,
        int lightCoords, Identifier skinTexture, boolean hasSleeve, Operation<Void> original) {
        if (!ReplacePlayerHandRenderEvent.tryRender(HumanoidArm.RIGHT, poseStack, collector, lightCoords))
            original.call(renderer, poseStack, collector, lightCoords, skinTexture, hasSleeve);
    }

    @WrapOperation(method = {"renderPlayerArm", "renderMapHand"},
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/player/AvatarRenderer;renderLeftHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/Identifier;Z)V"))
    private void ysm$leftArm(AvatarRenderer<?> renderer, PoseStack poseStack, SubmitNodeCollector collector,
        int lightCoords, Identifier skinTexture, boolean hasSleeve, Operation<Void> original) {
        if (!ReplacePlayerHandRenderEvent.tryRender(HumanoidArm.LEFT, poseStack, collector, lightCoords))
            original.call(renderer, poseStack, collector, lightCoords, skinTexture, hasSleeve);
    }
}
