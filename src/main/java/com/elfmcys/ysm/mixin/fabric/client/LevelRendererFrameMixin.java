/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.mixin.fabric.client;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.client.animation.AnimationParallelTicker;
import com.elfmcys.ysm.util.RenderUtil;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Fabric port of LevelRendererMixin (end): the level pass, including YSM's deferred draws, is finished. */
@Mixin(LevelRenderer.class)
public abstract class LevelRendererFrameMixin {
    @Inject(method = "render", at = @At("HEAD"))
    private void ysm$beginLevel(CallbackInfo ci) {
        if (YesSteveModel.isAvailable()) {
            RenderUtil.setRenderingLevel(true);
            // The first-person background is drawn once per frame (1.20 reset it after cutout blocks).
            com.elfmcys.ysm.client.event.RenderFirstPlayerBackground.reset();
        }
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void ysm$endLevel(CallbackInfo ci) {
        if (YesSteveModel.isAvailable()) {
            AnimationParallelTicker.waitAll();
            RenderUtil.setRenderingLevel(false);
        }
    }
}
