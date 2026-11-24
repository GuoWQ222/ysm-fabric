/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.network;

import com.elfmcys.ysm.network.protocol.MessageDirection;
import com.elfmcys.ysm.platform.network.NetworkDirection;
import com.elfmcys.ysm.platform.network.NetworkEvent;
//#if LOADER == fabric
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
//#endif
import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;

/** Fabric port: client-only half of {@link NetworkHandler} (Fabric's client networking classes). */
public final class NetworkHandlerClient {
    private NetworkHandlerClient() {}

    //#if LOADER == fabric
    static void init() {
        ClientPlayNetworking.registerGlobalReceiver(SoundPolicyPayload.TYPE,
            (payload, context) -> com.elfmcys.ysm.model.resource.client.SoundAcceptance.setLevel(payload.level()));
        ClientPlayNetworking.registerGlobalReceiver(YsmFramePayload.TYPE, (payload, context) -> receive(payload));
    }
    //#endif

    /** Client side of a YSM frame (any loader). */
    public static void receive(YsmFramePayload payload) {
        var client = Minecraft.getInstance();
        var listener = client.getConnection();
        if (listener == null)
            return;
        NetworkHandler.receive(payload, new NetworkEvent.Context(NetworkDirection.PLAY_TO_CLIENT,
            listener.getConnection(), null, client), MessageDirection.SERVER_TO_CLIENT);
    }

    private static boolean canSend(net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<?> type) {
        //#if LOADER == fabric
        return ClientPlayNetworking.canSend(type);
        //#elseif LOADER == neoforge
        //$$ var listener = Minecraft.getInstance().getConnection();
        //$$ return listener != null && listener.hasChannel(type);
        //#else
        //$$ return com.elfmcys.ysm.forge.ForgeNetwork.clientCanSend(type);
        //#endif
    }

    static boolean isRemoteChannelPresent() {
        var listener = Minecraft.getInstance().getConnection();
        return listener != null && com.elfmcys.ysm.platform.Connections.channel(listener.getConnection()) != null
            && canSend(YsmFramePayload.TYPE);
    }

    static boolean isChannelPresent(Connection connection) {
        var listener = Minecraft.getInstance().getConnection();
        return listener != null && listener.getConnection() == connection
            && canSend(YsmFramePayload.TYPE);
    }
}
