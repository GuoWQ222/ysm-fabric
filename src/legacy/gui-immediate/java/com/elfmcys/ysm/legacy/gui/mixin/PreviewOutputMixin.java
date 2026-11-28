/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.legacy.gui.mixin;

import com.elfmcys.ysm.client.gui.PreviewRenderHost;
import com.mojang.blaze3d.pipeline.RenderTarget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
//#if MC >= 1.21.5
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//#else
//$$ import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//#endif

/**
 * Minecraft before 1.21.6: while an offscreen preview is drawn (PreviewRenderHost), every render type draws into the
 * preview framebuffer. 1.21.5's render types name their framebuffer through their output state; older ones bind theirs
 * when set up (the main one, or Fabulous graphics' item-entity / translucent ones), which is redirected.
 */
//#if MC >= 1.21.5
@Mixin(targets = "net.minecraft.client.renderer.RenderStateShard$OutputStateShard")
public abstract class PreviewOutputMixin {
    @Inject(method = "getRenderTarget", at = @At("HEAD"), cancellable = true)
    private void ysm$previewTarget(CallbackInfoReturnable<RenderTarget> cir) {
        var target = PreviewRenderHost.drawingTarget();
        if (target != null)
            cir.setReturnValue(target);
    }
}
//#else
//$$ @Mixin(RenderTarget.class)
//$$ public abstract class PreviewOutputMixin {
//$$     @Inject(method = "bindWrite", at = @At("HEAD"), cancellable = true)
//$$     private void ysm$previewTarget(boolean setViewport, CallbackInfo ci) {
//$$         var target = PreviewRenderHost.drawingTarget();
//$$         if (target != null && target != (Object)this) {
//$$             target.bindWrite(setViewport);
//$$             ci.cancel();
//$$         }
//$$     }
//$$ }
//#endif
