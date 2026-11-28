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
import com.mojang.blaze3d.vertex.VertexFormat;
import java.nio.ByteBuffer;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Minecraft before 1.21 (replaces the main file there): the builder owns its direct ByteBuffer, as in 1.20, so this is
 * upstream YSM's BufferBuilderMixin: reserve grows the buffer without advancing it; advance commits complete
 * native-written vertices.
 */
@Mixin(BufferBuilder.class)
public abstract class BufferBuilderDirectWriteMixin implements VertexBufferAccessor {
    @Unique
    private static final int YSM_MAX_VERTEX_COUNT = 16777215;
    @Shadow
    private ByteBuffer buffer;
    @Shadow
    private int nextElementByte;
    @Shadow
    private int vertices;
    @Shadow
    private VertexFormat format;
    @Shadow
    private boolean building;

    @Shadow
    protected abstract void ensureCapacity(int increaseAmount);

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
        var requiredBytes = Math.multiplyExact(vertexCount, format.getVertexSize());
        ensureCapacity(requiredBytes);
        // the buffer may have been replaced while growing: take its base address afterwards
        return new ReservedVertexRange(MemoryUtil.memAddress0(buffer) + nextElementByte, requiredBytes, buffer);
    }

    @Override
    public void ysm$advance(int vertexCount) {
        if (vertexCount <= 0)
            return;
        vertices += vertexCount;
        nextElementByte += vertexCount * format.getVertexSize();
    }
}
