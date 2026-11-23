/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.mixin.fabric.client;

import com.elfmcys.ysm.client.event.RegisterEntityRenderersEvent;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Builds YSM's renderers from the same context the vanilla renderers were just built with. */
@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherReloadMixin {
    @Inject(method = "onResourceManagerReload", at = @At("TAIL"))
    private void ysm$buildRenderers(ResourceManager resourceManager, CallbackInfo ci,
        @Local EntityRendererProvider.Context context) {
        RegisterEntityRenderersEvent.init(context);
    }
}
