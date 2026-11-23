/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.platform;

import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

/** Fabric port: replaces Forge's ServerLifecycleHooks.getCurrentServer(). */
public final class ServerHolder {
    private static volatile @Nullable MinecraftServer current;

    private ServerHolder() {}

    public static @Nullable MinecraftServer getCurrentServer() {
        return current;
    }

    static void set(@Nullable MinecraftServer server) {
        current = server;
    }

    /** Called from the Fabric lifecycle hooks. */
    public static void register() {
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STARTING.register(ServerHolder::set);
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPED.register(server -> set(null));
    }
}
