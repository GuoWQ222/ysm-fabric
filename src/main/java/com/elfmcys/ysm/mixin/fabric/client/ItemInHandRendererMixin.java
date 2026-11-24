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
//#if MC < 26.3
import net.minecraft.client.renderer.ItemInHandRenderer;
//#endif
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Fabric port of Forge's RenderHandEvent / RenderArmEvent hooks for YSM's first-person model. */
//#if MC >= 26.3
//$$ @Mixin(net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer.class)
//#else
@Mixin(ItemInHandRenderer.class)
//#endif
public abstract class ItemInHandRendererMixin {
    //#if MC >= 26.2
    @Inject(method = "submitHandsWithItems", at = @At("HEAD"))
    //#else
    //$$ @Inject(method = "renderHandsWithItems", at = @At("HEAD"))
    //#endif
    //#if MC >= 26.3
    //$$ private void ysm$background(float frameInterp, PoseStack poseStack, SubmitNodeCollector collector,
    //$$     net.minecraft.client.renderer.state.level.PlayerRenderState player,
    //$$     net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState hands, CallbackInfo ci) {
    //$$     // 26.3 renders the hands from render states; the light is the avatar state's
    //$$     RenderFirstPlayerBackground.onHands(frameInterp, poseStack, collector, player.avatarRenderState.lightCoords);
    //$$ }
    //#else
    private void ysm$background(float frameInterp, PoseStack poseStack, SubmitNodeCollector collector,
        LocalPlayer player, int lightCoords, CallbackInfo ci) {
        RenderFirstPlayerBackground.onHands(frameInterp, poseStack, collector, lightCoords);
    }
    //#endif

    //#if MC >= 26.3
    //$$ @WrapOperation(method = "renderPlayerHand",
    //#else
    @WrapOperation(method = {"renderPlayerArm", "renderMapHand"},
    //#endif
        at = @At(value = "INVOKE",
            //#if LOADER == neoforge && MC < 26.2
            //$$ // NeoForge 26.1.x patches the call to pass the player (extra parameter)
            //$$ target = "Lnet/minecraft/client/renderer/entity/player/AvatarRenderer;renderRightHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/Identifier;ZLnet/minecraft/client/player/AbstractClientPlayer;)V"))
            //$$ private void ysm$rightArm(AvatarRenderer<?> renderer, PoseStack poseStack, SubmitNodeCollector collector,
            //$$     int lightCoords, Identifier skinTexture, boolean hasSleeve, net.minecraft.client.player.AbstractClientPlayer extra,
            //$$     Operation<Void> original) {
            //$$     if (!ReplacePlayerHandRenderEvent.tryRender(HumanoidArm.RIGHT, poseStack, collector, lightCoords))
            //$$         original.call(renderer, poseStack, collector, lightCoords, skinTexture, hasSleeve, extra);
            //$$ }
            //#elseif LOADER == neoforge && MC < 26.3
            //$$ // NeoForge 26.2 patches the call to pass the avatar (extra parameter); 26.3 calls vanilla's
            //$$ target = "Lnet/minecraft/client/renderer/entity/player/AvatarRenderer;renderRightHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/Identifier;ZLnet/minecraft/world/entity/Avatar;)V"))
            //$$ private void ysm$rightArm(AvatarRenderer<?> renderer, PoseStack poseStack, SubmitNodeCollector collector,
            //$$     int lightCoords, Identifier skinTexture, boolean hasSleeve, net.minecraft.world.entity.Avatar extra,
            //$$     Operation<Void> original) {
            //$$     if (!ReplacePlayerHandRenderEvent.tryRender(HumanoidArm.RIGHT, poseStack, collector, lightCoords))
            //$$         original.call(renderer, poseStack, collector, lightCoords, skinTexture, hasSleeve, extra);
            //$$ }
            //#else
            target = "Lnet/minecraft/client/renderer/entity/player/AvatarRenderer;renderRightHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/Identifier;Z)V"))
    private void ysm$rightArm(AvatarRenderer<?> renderer, PoseStack poseStack, SubmitNodeCollector collector,
        int lightCoords, Identifier skinTexture, boolean hasSleeve, Operation<Void> original) {
        if (!ReplacePlayerHandRenderEvent.tryRender(HumanoidArm.RIGHT, poseStack, collector, lightCoords))
            original.call(renderer, poseStack, collector, lightCoords, skinTexture, hasSleeve);
    }
            //#endif

    //#if MC >= 26.3
    //$$ @WrapOperation(method = "renderPlayerHand",
    //#else
    @WrapOperation(method = {"renderPlayerArm", "renderMapHand"},
    //#endif
        at = @At(value = "INVOKE",
            //#if LOADER == neoforge && MC < 26.2
            //$$ // NeoForge 26.1.x patches the call to pass the player (extra parameter)
            //$$ target = "Lnet/minecraft/client/renderer/entity/player/AvatarRenderer;renderLeftHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/Identifier;ZLnet/minecraft/client/player/AbstractClientPlayer;)V"))
            //$$ private void ysm$leftArm(AvatarRenderer<?> renderer, PoseStack poseStack, SubmitNodeCollector collector,
            //$$     int lightCoords, Identifier skinTexture, boolean hasSleeve, net.minecraft.client.player.AbstractClientPlayer extra,
            //$$     Operation<Void> original) {
            //$$     if (!ReplacePlayerHandRenderEvent.tryRender(HumanoidArm.LEFT, poseStack, collector, lightCoords))
            //$$         original.call(renderer, poseStack, collector, lightCoords, skinTexture, hasSleeve, extra);
            //$$ }
            //#elseif LOADER == neoforge && MC < 26.3
            //$$ // NeoForge 26.2 patches the call to pass the avatar (extra parameter); 26.3 calls vanilla's
            //$$ target = "Lnet/minecraft/client/renderer/entity/player/AvatarRenderer;renderLeftHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/Identifier;ZLnet/minecraft/world/entity/Avatar;)V"))
            //$$ private void ysm$leftArm(AvatarRenderer<?> renderer, PoseStack poseStack, SubmitNodeCollector collector,
            //$$     int lightCoords, Identifier skinTexture, boolean hasSleeve, net.minecraft.world.entity.Avatar extra,
            //$$     Operation<Void> original) {
            //$$     if (!ReplacePlayerHandRenderEvent.tryRender(HumanoidArm.LEFT, poseStack, collector, lightCoords))
            //$$         original.call(renderer, poseStack, collector, lightCoords, skinTexture, hasSleeve, extra);
            //$$ }
            //#else
            target = "Lnet/minecraft/client/renderer/entity/player/AvatarRenderer;renderLeftHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/Identifier;Z)V"))
    private void ysm$leftArm(AvatarRenderer<?> renderer, PoseStack poseStack, SubmitNodeCollector collector,
        int lightCoords, Identifier skinTexture, boolean hasSleeve, Operation<Void> original) {
        if (!ReplacePlayerHandRenderEvent.tryRender(HumanoidArm.LEFT, poseStack, collector, lightCoords))
            original.call(renderer, poseStack, collector, lightCoords, skinTexture, hasSleeve);
    }
            //#endif
}
