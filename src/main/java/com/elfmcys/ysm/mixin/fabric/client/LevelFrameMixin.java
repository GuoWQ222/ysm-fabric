/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.mixin.fabric.client;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.client.animation.AnimationParallelTicker;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
//#if MC >= 26.2
import net.minecraft.client.renderer.extract.LevelExtractor;
//#else
//$$ import net.minecraft.client.renderer.LevelRenderer;
//#endif
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fabric port of LevelRendererMixin (start): 26.x extracts the level before rendering it, so YSM's parallel animation
 * updates start when extraction starts; LevelRendererFrameMixin waits for them after the level render.
 */
//#if MC >= 26.2
@Mixin(LevelExtractor.class)
//#else
//$$ @Mixin(LevelRenderer.class)
//#endif
public abstract class LevelFrameMixin {
    //#if MC >= 26.2
    @Inject(method = "extract", at = @At("HEAD"))
    //#elseif MC >= 26.1
    //$$ @Inject(method = "extractLevel", at = @At("HEAD"))
    //#endif
    //#if MC >= 26.1
    private void ysm$scheduleAnimations(DeltaTracker deltaTracker, Camera camera, float deltaPartialTick,
        CallbackInfo ci) {
    //#else
    //$$ // 1.21.x: renderLevel extracts the level itself, with the game-time partial tick of the frame
    //$$ @Inject(method = "renderLevel", at = @At("HEAD"))
    //$$ private void ysm$scheduleAnimations(CallbackInfo ci) {
    //$$     float deltaPartialTick = net.minecraft.client.Minecraft.getInstance().getDeltaTracker()
    //$$         .getGameTimeDeltaPartialTick(false);
    //#endif
        if (!YesSteveModel.isAvailable())
            return;
        // A frame can extract without rendering the level; finish that frame's updates first.
        if (AnimationParallelTicker.isInvocationActive())
            AnimationParallelTicker.waitAll();
        AnimationParallelTicker.scheduleAll(deltaPartialTick);
    }
}
