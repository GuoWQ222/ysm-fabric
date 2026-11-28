/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.mixin.fabric.client;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Lets the direct vertex writer grow the staging buffer without advancing it. */
@Mixin(ByteBufferBuilder.class)
public interface ByteBufferBuilderAccessor {
    @Accessor("pointer")
    long ysm$pointer();

    //#if MC >= 1.21.6
    @Accessor("writeOffset")
    long ysm$writeOffset();

    @Invoker("ensureCapacity")
    void ysm$ensureCapacity(long requiredCapacity);
    //#else
    //$$ // before 1.21.6 the builder counts in int
    //$$ @Accessor("writeOffset")
    //$$ int ysm$writeOffset();
    //$$
    //$$ @Invoker("ensureCapacity")
    //$$ void ysm$ensureCapacity(int requiredCapacity);
    //#endif
}
