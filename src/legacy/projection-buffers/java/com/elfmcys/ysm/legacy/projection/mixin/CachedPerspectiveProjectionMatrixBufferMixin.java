/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.legacy.projection.mixin;

import com.elfmcys.ysm.platform.render.YsmRenderMatrices;
import com.mojang.blaze3d.buffers.GpuBuffer;
import net.minecraft.client.renderer.CachedPerspectiveProjectionMatrixBuffer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Minecraft 1.21.6 - 1.21.11: the cached perspective projection (first-person hand) is built when the size or field
 * of view changes, then written into this buffer. Records the built matrix for YsmRenderMatrices.
 */
@Mixin(CachedPerspectiveProjectionMatrixBuffer.class)
public abstract class CachedPerspectiveProjectionMatrixBufferMixin {
    @Shadow
    @Final
    private GpuBuffer buffer;

    @Inject(method = "createProjectionMatrix", at = @At("RETURN"))
    private void ysm$recordProjection(CallbackInfoReturnable<Matrix4f> cir) {
        YsmRenderMatrices.recordUpload(buffer, cir.getReturnValue());
    }
}
