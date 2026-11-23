/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.platform.event.forge;

import com.elfmcys.ysm.platform.event.Event;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.Connection;
import org.jetbrains.annotations.Nullable;

/** Fabric port: Forge client connection events, posted from Fabric client connection callbacks. */
public class ClientPlayerNetworkEvent extends Event {
    private final @Nullable LocalPlayer player;
    private final @Nullable ClientPacketListener listener;

    protected ClientPlayerNetworkEvent(@Nullable LocalPlayer player, @Nullable ClientPacketListener listener) {
        this.player = player;
        this.listener = listener;
    }

    public @Nullable LocalPlayer getPlayer() {
        return player;
    }

    public @Nullable Connection getConnection() {
        return listener == null ? null : listener.getConnection();
    }

    public static class LoggingIn extends ClientPlayerNetworkEvent {
        public LoggingIn(@Nullable LocalPlayer player, ClientPacketListener listener) {
            super(player, listener);
        }
    }

    public static class LoggingOut extends ClientPlayerNetworkEvent {
        public LoggingOut(@Nullable LocalPlayer player, @Nullable ClientPacketListener listener) {
            super(player, listener);
        }
    }

    public static class Clone extends ClientPlayerNetworkEvent {
        private final LocalPlayer oldPlayer;

        public Clone(LocalPlayer oldPlayer, LocalPlayer newPlayer, ClientPacketListener listener) {
            super(newPlayer, listener);
            this.oldPlayer = oldPlayer;
        }

        public LocalPlayer getOldPlayer() {
            return oldPlayer;
        }

        public LocalPlayer getNewPlayer() {
            return getPlayer();
        }
    }
}
