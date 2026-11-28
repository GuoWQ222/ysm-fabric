/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.forge;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.legacy.network.CustomPacketPayload;
import com.elfmcys.ysm.network.NetworkHandler;
import com.elfmcys.ysm.network.SoundPolicyPayload;
import com.elfmcys.ysm.network.YsmFramePayload;
import com.elfmcys.ysm.network.protocol.MessageDirection;
import com.elfmcys.ysm.platform.Connections;
import com.elfmcys.ysm.platform.network.NetworkDirection;
import com.elfmcys.ysm.platform.network.NetworkEvent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.SimpleChannel;

/**
 * Forge glue for Minecraft before 1.20.5 (Forge 48 / 49; replaces the main file there): Forge's SimpleChannel on the
 * same ysm:network channel carries YSM's two payloads as indexed messages (frames both ways, the sound policy to the
 * client). Handlers run on the game thread, as on the other loaders.
 */
public final class ForgeNetwork {
    private static final int FRAME = 0;
    private static final int SOUND_POLICY = 1;
    private static SimpleChannel channel;

    private ForgeNetwork() {}

    public static void register() {
        channel = ChannelBuilder.named(new ResourceLocation(YesSteveModel.MOD_ID, "network"))
            .networkProtocolVersion(1)
            .optional()
            .simpleChannel();
        channel.messageBuilder(YsmFramePayload.class, FRAME)
            .encoder((payload, buffer) -> YsmFramePayload.CODEC.encode(buffer, payload))
            .decoder(buffer -> YsmFramePayload.CODEC.decode(buffer))
            .consumerMainThread((payload, context) -> {
                var player = context.getSender();
                if (player != null) {
                    NetworkHandler.receive(payload, new NetworkEvent.Context(NetworkDirection.PLAY_TO_SERVER,
                        Connections.of(player), player, player.level().getServer()), MessageDirection.CLIENT_TO_SERVER);
                } else {
                    Client.receive(payload);
                }
            })
            .add();
        channel.messageBuilder(SoundPolicyPayload.class, SOUND_POLICY,
                net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT)
            .encoder((payload, buffer) -> SoundPolicyPayload.CODEC.encode(buffer, payload))
            .decoder(buffer -> SoundPolicyPayload.CODEC.decode(buffer))
            .consumerMainThread((payload, context) -> Client.soundPolicy(payload))
            .add();
    }

    /** A vanilla custom-payload packet carrying the payload through this channel. */
    public static Packet<?> packet(PacketFlow flow, CustomPacketPayload payload) {
        var direction = flow == PacketFlow.SERVERBOUND ? net.minecraftforge.network.NetworkDirection.PLAY_TO_SERVER
            : net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT;
        return direction.buildPacket(channel.toBuffer(payload), channel.getName()).getThis();
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
