/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.neoforge;

import com.elfmcys.ysm.network.NetworkHandler;
import com.elfmcys.ysm.network.SoundPolicyPayload;
import com.elfmcys.ysm.network.YsmFramePayload;
import com.elfmcys.ysm.network.protocol.MessageDirection;
import com.elfmcys.ysm.platform.Connections;
import com.elfmcys.ysm.platform.network.NetworkDirection;
import com.elfmcys.ysm.platform.network.NetworkEvent;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/**
 * NeoForge glue: registers YSM's payloads (optional channel, so vanilla servers and clients still connect) and routes
 * them to the shared handlers; the counterpart of the Fabric registration in NetworkHandler.init.
 */
public final class NeoForgeNetwork {
    private NeoForgeNetwork() {}

    public static void register() {
        YsmNeoForge.modBus().addListener((RegisterPayloadHandlersEvent event) -> {
            var registrar = event.registrar("1").optional();
            // NeoForge 26: the registrar takes the server-side handler; client-side handlers are registered through
            // RegisterClientPayloadHandlersEvent (missing ones abort client startup)
            registrar.playBidirectional(YsmFramePayload.TYPE, YsmFramePayload.CODEC, (payload, context) -> {
                if (context.player() instanceof ServerPlayer player)
                    NetworkHandler.receive(payload, new NetworkEvent.Context(NetworkDirection.PLAY_TO_SERVER,
                        Connections.of(player), player, player.level().getServer()), MessageDirection.CLIENT_TO_SERVER);
            });
            registrar.playToClient(SoundPolicyPayload.TYPE, SoundPolicyPayload.CODEC);
        });
        if (com.elfmcys.ysm.platform.Platform.isClient())
            Client.registerHandlers();
    }

    /** Client-only classes stay behind this nested class (not loaded on a dedicated server). */
    private static final class Client {
        static void registerHandlers() {
            YsmNeoForge.modBus().addListener(
                (net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent event) -> {
                    event.register(YsmFramePayload.TYPE, (payload, context) -> receive(payload));
                    event.register(SoundPolicyPayload.TYPE, (payload, context) -> soundPolicy(payload));
                });
        }

        static void receive(YsmFramePayload payload) {
            com.elfmcys.ysm.network.NetworkHandlerClient.receive(payload);
        }

        static void soundPolicy(SoundPolicyPayload payload) {
            com.elfmcys.ysm.model.resource.client.SoundAcceptance.setLevel(payload.level());
        }
    }
}
