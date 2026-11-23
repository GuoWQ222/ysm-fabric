/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.mixin.fabric.client;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.client.animation.AnimationParallelTicker;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Forge drew the HUD and screens after LevelRenderer.renderLevel had joined YSM's parallel animation updates. 26.2
 * extracts the GUI right after the level extraction and before the level draw, so pending updates are joined here
 * (the invocation stays open for the level draw; LevelRendererFrameMixin still closes it). GUI code (model screens,
 * HUD) then reads entity state on the render thread as before.
 */
@Mixin(Gui.class)
public abstract class GuiExtractWaitMixin {
    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void ysm$joinAnimationUpdates(CallbackInfo ci) {
        if (YesSteveModel.isAvailable() && AnimationParallelTicker.isInvocationActive())
            AnimationParallelTicker.joinPending();
    }
}
