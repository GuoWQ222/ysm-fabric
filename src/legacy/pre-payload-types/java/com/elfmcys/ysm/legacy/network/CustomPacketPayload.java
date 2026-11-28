/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.legacy.network;

import net.minecraft.resources.ResourceLocation;

/**
 * Minecraft before 1.20.5 has no typed custom payloads (CustomPacketPayload.Type). This stand-in (mapped from
 * net.minecraft.network.protocol.common.custom.CustomPacketPayload by versions/below-1.20.5.remap) gives YSM's two
 * payloads their 1.20.5 shape; Forge's SimpleChannel carries them there (src/legacy/pre-payload-types ForgeNetwork).
 */
public interface CustomPacketPayload {
    /** 1.20.5's payload type: the payload's id. */
    record Type<T extends CustomPacketPayload>(ResourceLocation id) {}

    Type<? extends CustomPacketPayload> type();
}
