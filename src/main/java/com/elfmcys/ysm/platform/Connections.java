/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.platform;

import com.elfmcys.ysm.mixin.fabric.ServerCommonPacketListenerAccessor;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;

/** Fabric port: replaces 1.20's public {@code com.elfmcys.ysm.platform.Connections.of(player)}. */
public final class Connections {
    private Connections() {}

    /** The netty channel of a connection (null before it is open). */
    public static io.netty.channel.Channel channel(Connection connection) {
        return ((com.elfmcys.ysm.mixin.fabric.ConnectionAccessor)connection).ysm$getChannel();
    }

    public static Connection of(ServerPlayer player) {
        return ((ServerCommonPacketListenerAccessor)player.connection).ysm$getConnection();
    }
}
