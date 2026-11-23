/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.mixin.fabric.client;

import com.elfmcys.ysm.accessor.ReservedVertexRange;
import com.elfmcys.ysm.accessor.VertexBufferAccessor;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Fabric port of BufferBuilderMixin for 26.x's ByteBufferBuilder-backed builder: reserve grows the staging buffer
 * without advancing it; advance commits complete native-written vertices.
 */
@Mixin(BufferBuilder.class)
public abstract class BufferBuilderDirectWriteMixin implements VertexBufferAccessor {
    @Unique
    private static final int YSM_MAX_VERTEX_COUNT = 16777215;
    @Shadow
    @Final
    private ByteBufferBuilder buffer;
    @Shadow
    private long vertexPointer;
    @Shadow
    private int vertices;
    @Shadow
    @Final
    private VertexFormat format;
    @Shadow
    @Final
    private int vertexSize;
    @Shadow
    private int elementsToFill;
    @Shadow
    private boolean building;

    @Shadow
    protected abstract void endLastVertex();

    @Override
    public boolean ysm$ok() {
        return building;
    }

    @Override
    public VertexFormat ysm$vertexFormat() {
        return format;
    }

    @Override
    public ReservedVertexRange ysm$reserve(int vertexCount) {
        if (vertexCount < 0)
            throw new IllegalArgumentException("Negative vertex count");
        if ((long)vertices + vertexCount > YSM_MAX_VERTEX_COUNT)
            throw new IllegalStateException("Trying to write too many vertices into BufferBuilder");
        endLastVertex();
        var requiredBytes = Math.multiplyExact(vertexCount, vertexSize);
        var accessor = (ByteBufferBuilderAccessor)buffer;
        accessor.ysm$ensureCapacity(Math.addExact(accessor.ysm$writeOffset(), requiredBytes));
        return new ReservedVertexRange(accessor.ysm$pointer() + accessor.ysm$writeOffset(), requiredBytes, buffer);
    }

    @Override
    public void ysm$advance(int vertexCount) {
        if (vertexCount <= 0)
            return;
        long start = buffer.reserve(Math.multiplyExact(vertexCount, vertexSize));
        vertices += vertexCount;
        vertexPointer = start + (long)(vertexCount - 1) * vertexSize;
        elementsToFill = 0;
    }
}
