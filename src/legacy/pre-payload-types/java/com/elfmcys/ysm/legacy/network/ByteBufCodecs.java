/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.legacy.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;

/** Minecraft before 1.20.5: the 1.20.5 ByteBufCodecs YSM uses (see StreamCodec). */
public final class ByteBufCodecs {
    private ByteBufCodecs() {}

    public static final StreamCodec<ByteBuf, Integer> VAR_INT = StreamCodec.of(
        (buffer, value) -> new FriendlyByteBuf(buffer).writeVarInt(value),
        buffer -> new FriendlyByteBuf(buffer).readVarInt());
}
