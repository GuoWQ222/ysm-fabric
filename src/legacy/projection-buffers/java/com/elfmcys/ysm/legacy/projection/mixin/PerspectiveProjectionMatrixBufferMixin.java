/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.legacy.projection.mixin;

import com.elfmcys.ysm.platform.render.YsmRenderMatrices;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import net.minecraft.client.renderer.PerspectiveProjectionMatrixBuffer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Minecraft 1.21.6 - 1.21.11 (before 26.1's single ProjectionMatrixBuffer): the level projection is written into this
 * buffer from the matrix argument. Records it for YsmRenderMatrices.
 */
@Mixin(PerspectiveProjectionMatrixBuffer.class)
public abstract class PerspectiveProjectionMatrixBufferMixin {
    @Shadow
    @Final
    private GpuBuffer buffer;

    @Inject(method = "getBuffer", at = @At("HEAD"))
    private void ysm$recordProjection(Matrix4f projectionMatrix, CallbackInfoReturnable<GpuBufferSlice> cir) {
        YsmRenderMatrices.recordUpload(buffer, projectionMatrix);
    }
}
