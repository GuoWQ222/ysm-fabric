/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.network;

import io.netty.buffer.ByteBuf;
import java.util.Objects;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Fabric port: one encoded YSM frame (FrameCodec bytes) on the ysm channel. The bytes are immutable so the same
 * payload can be encoded once per receiving connection.
 */
public record YsmFramePayload(byte[] frame) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<YsmFramePayload> TYPE =
        new CustomPacketPayload.Type<>(NetworkHandler.CHANNEL_NAME);
    public static final StreamCodec<ByteBuf, YsmFramePayload> CODEC =
        StreamCodec.of((buffer, payload) -> buffer.writeBytes(payload.frame), buffer -> {
            var bytes = new byte[buffer.readableBytes()];
            buffer.readBytes(bytes);
            return new YsmFramePayload(bytes);
        });

    public YsmFramePayload {
        Objects.requireNonNull(frame, "frame");
    }

    @Override
    public Type<YsmFramePayload> type() {
        return TYPE;
    }
}
