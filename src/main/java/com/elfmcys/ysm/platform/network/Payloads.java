/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.platform.network;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * The vanilla custom-payload packet for one of YSM's payloads. Fabric and NeoForge encode mod payloads directly; Forge
 * sends them through its payload channel (wrapped as a ForgePayload carrying the payload's id).
 */
public final class Payloads {
    private Payloads() {}

    public static Packet<?> toServer(CustomPacketPayload payload) {
        //#if LOADER == forge
        //$$ return com.elfmcys.ysm.forge.ForgeNetwork.packet(net.minecraft.network.protocol.PacketFlow.SERVERBOUND, payload);
        //#else
        return new ServerboundCustomPayloadPacket(payload);
        //#endif
    }

    public static Packet<?> toClient(CustomPacketPayload payload) {
        //#if LOADER == forge
        //$$ return com.elfmcys.ysm.forge.ForgeNetwork.packet(net.minecraft.network.protocol.PacketFlow.CLIENTBOUND, payload);
        //#else
        return new ClientboundCustomPayloadPacket(payload);
        //#endif
    }
}
