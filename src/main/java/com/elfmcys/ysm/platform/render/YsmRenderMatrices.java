/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.platform.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Map;
import java.util.WeakHashMap;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

/**
 * Fabric port: 26.x keeps the projection only as a GPU uniform buffer. ProjectionMatrixBufferMixin records the CPU
 * matrix written into each projection buffer, so the native renderer gets the projection that is active when its
 * vertices are written (its back-face culling and translucent sort depend on the real camera).
 */
public final class YsmRenderMatrices {
    private static final Map<GpuBuffer, Matrix4f> UPLOADED = new WeakHashMap<>();
    private static final Matrix4f IDENTITY = new Matrix4f();

    private YsmRenderMatrices() {}

    /** Render thread: a projection matrix was written into this buffer. */
    public static void recordUpload(GpuBuffer buffer, Matrix4fc matrix) {
        UPLOADED.computeIfAbsent(buffer, ignored -> new Matrix4f()).set(matrix);
    }

    /** Render thread: the projection bound by RenderSystem right now. */
    public static Matrix4fc currentProjection() {
        var slice = RenderSystem.getProjectionMatrixBuffer();
        var matrix = slice == null ? null : UPLOADED.get(slice.buffer());
        return matrix == null ? IDENTITY : matrix;
    }

    /** Render thread: the current model-view (camera rotation during level rendering). */
    public static Matrix4fc currentModelView() {
        return RenderSystem.getModelViewStack();
    }
}
