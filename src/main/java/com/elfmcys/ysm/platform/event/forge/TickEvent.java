/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.platform.event.forge;

import com.elfmcys.ysm.platform.event.Event;
import net.minecraft.server.MinecraftServer;

/** Fabric port: Forge tick events, posted from Fabric tick callbacks. */
public class TickEvent extends Event {
    public enum Phase { START, END }

    public final Phase phase;

    protected TickEvent(Phase phase) {
        this.phase = phase;
    }

    public static class ClientTickEvent extends TickEvent {
        public ClientTickEvent(Phase phase) {
            super(phase);
        }
    }

    public static class ServerTickEvent extends TickEvent {
        private final MinecraftServer server;

        public ServerTickEvent(Phase phase, MinecraftServer server) {
            super(phase);
            this.server = server;
        }

        public MinecraftServer getServer() {
            return server;
        }
    }
}
