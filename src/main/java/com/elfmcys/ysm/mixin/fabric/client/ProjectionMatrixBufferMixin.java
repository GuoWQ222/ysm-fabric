/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.mixin.fabric.client;

import com.elfmcys.ysm.platform.render.YsmRenderMatrices;
import com.mojang.blaze3d.buffers.GpuBuffer;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Records the CPU projection written into each projection uniform buffer (see YsmRenderMatrices). */
@Mixin(ProjectionMatrixBuffer.class)
public abstract class ProjectionMatrixBufferMixin {
    @Shadow
    @Final
    private GpuBuffer buffer;

    @Inject(method = "writeBuffer", at = @At("HEAD"))
    private void ysm$recordProjection(Matrix4f projectionMatrix,
        CallbackInfoReturnable<com.mojang.blaze3d.buffers.GpuBufferSlice> cir) {
        YsmRenderMatrices.recordUpload(buffer, projectionMatrix);
    }
}
