/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Fabric port (2.6.5 AcceptSoundFX): the server's model sound policy, sent next to the model-session offer.
 * 0 = all sounds, 1 = short sounds only, 2 = no sounds. A separate payload keeps YSM's own protocol unchanged.
 */
public record SoundPolicyPayload(int level) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SoundPolicyPayload> TYPE =
        new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("ysm", "sound_policy"));
    public static final StreamCodec<ByteBuf, SoundPolicyPayload> CODEC =
        ByteBufCodecs.VAR_INT.map(SoundPolicyPayload::new, SoundPolicyPayload::level);

    @Override
    public Type<SoundPolicyPayload> type() {
        return TYPE;
    }
}
