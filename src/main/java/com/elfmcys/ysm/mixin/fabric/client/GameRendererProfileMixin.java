/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.mixin.fabric.client;

import com.elfmcys.ysm.natives.NativeProfiler;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Fabric port of GameRendererMixin: native profiler frame marks around each rendered frame. */
@Mixin(GameRenderer.class)
public abstract class GameRendererProfileMixin {
    @Unique
    private boolean ysm$profileFrameActive;

    //#if MC >= 26.3
    //$$ @Inject(method = "render()V", at = @At("HEAD"))
    //$$ private void ysm$beforeRender(CallbackInfo ci) {
    //#elseif MC >= 1.21
    @Inject(method = "render(Lnet/minecraft/client/DeltaTracker;Z)V", at = @At("HEAD"))
    private void ysm$beforeRender(DeltaTracker deltaTracker, boolean advanceGameTime, CallbackInfo ci) {
    //#else
    //$$ // before 1.21: render(partialTick, nanoTime, renderLevel)
    //$$ @Inject(method = "render(FJZ)V", at = @At("HEAD"))
    //$$ private void ysm$beforeRender(float partialTick, long nanoTime, boolean renderLevel, CallbackInfo ci) {
    //#endif
        ysm$profileFrameActive = NativeProfiler.beginFrame();
    }

    //#if MC >= 26.3
    //$$ @Inject(method = "render()V", at = @At("RETURN"))
    //$$ private void ysm$afterRender(CallbackInfo ci) {
    //#elseif MC >= 1.21
    @Inject(method = "render(Lnet/minecraft/client/DeltaTracker;Z)V", at = @At("RETURN"))
    private void ysm$afterRender(DeltaTracker deltaTracker, boolean advanceGameTime, CallbackInfo ci) {
    //#else
    //$$ @Inject(method = "render(FJZ)V", at = @At("RETURN"))
    //$$ private void ysm$afterRender(float partialTick, long nanoTime, boolean renderLevel, CallbackInfo ci) {
    //#endif
        NativeProfiler.endFrame(ysm$profileFrameActive);
        ysm$profileFrameActive = false;
    }
}
