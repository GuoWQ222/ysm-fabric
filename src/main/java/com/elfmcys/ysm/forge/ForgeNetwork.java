/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.forge;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.network.NetworkHandler;
import com.elfmcys.ysm.network.SoundPolicyPayload;
import com.elfmcys.ysm.network.YsmFramePayload;
import com.elfmcys.ysm.network.protocol.MessageDirection;
import com.elfmcys.ysm.platform.Connections;
import com.elfmcys.ysm.platform.network.NetworkDirection;
import com.elfmcys.ysm.platform.network.NetworkEvent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraftforge.network.Channel;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkProtocol;

/**
 * Forge glue: one optional payload channel carrying YSM's payloads (each payload id is a child of the channel, so the
 * ids on the wire are the same as on Fabric and NeoForge). Handlers run on the game thread, as on the other loaders.
 */
public final class ForgeNetwork {
    private static Channel<CustomPacketPayload> channel;

    private ForgeNetwork() {}

    public static void register() {
        channel = ChannelBuilder.named(Identifier.fromNamespaceAndPath(YesSteveModel.MOD_ID, "network"))
            .networkProtocolVersion(1)
            .optional()
            .payloadChannel()
            .play()
            .bidirectional()
            .addMain(YsmFramePayload.TYPE, YsmFramePayload.CODEC.cast(), (payload, context) -> {
                var player = context.getSender();
                if (player != null) {
                    NetworkHandler.receive(payload, new NetworkEvent.Context(NetworkDirection.PLAY_TO_SERVER,
                        Connections.of(player), player, player.level().getServer()), MessageDirection.CLIENT_TO_SERVER);
                } else {
                    Client.receive(payload);
                }
            })
            .clientbound()
            .addMain(SoundPolicyPayload.TYPE, SoundPolicyPayload.CODEC.cast(), (payload, context) -> {
                Client.soundPolicy(payload);
            })
            .build();
    }

    /** A vanilla custom-payload packet carrying the payload through this channel. */
    public static Packet<?> packet(PacketFlow flow, CustomPacketPayload payload) {
        return NetworkProtocol.PLAY.buildPacket(flow, channel, payload);
    }

    public static boolean canSend(ServerGamePacketListenerImpl listener, CustomPacketPayload.Type<?> type) {
        return channel != null && channel.isRemotePresent(listener.getConnection());
    }

    public static boolean clientCanSend(CustomPacketPayload.Type<?> type) {
        return Client.canSend();
    }

    /** Client-only classes stay behind this nested class (not loaded on a dedicated server). */
    private static final class Client {
        static void receive(YsmFramePayload payload) {
            com.elfmcys.ysm.network.NetworkHandlerClient.receive(payload);
        }

        static void soundPolicy(SoundPolicyPayload payload) {
            com.elfmcys.ysm.model.resource.client.SoundAcceptance.setLevel(payload.level());
        }

        static boolean canSend() {
            var listener = net.minecraft.client.Minecraft.getInstance().getConnection();
            return channel != null && listener != null && channel.isRemotePresent(listener.getConnection());
        }
    }
}
