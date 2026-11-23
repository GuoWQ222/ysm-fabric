/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.platform.network;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.thread.BlockableEventLoop;
import org.jetbrains.annotations.Nullable;

/** Fabric port: the parts of Forge's NetworkEvent that YSM's protocol handlers use. */
public final class NetworkEvent {
    private NetworkEvent() {}

    /** Per-packet context, as Forge's NetworkEvent.Context. */
    public static final class Context {
        private final NetworkDirection direction;
        private final Connection connection;
        private final @Nullable ServerPlayer sender;
        private final BlockableEventLoop<?> mainThread;
        private boolean packetHandled;

        public Context(NetworkDirection direction, Connection connection, @Nullable ServerPlayer sender,
            BlockableEventLoop<?> mainThread) {
            this.direction = Objects.requireNonNull(direction, "direction");
            this.connection = Objects.requireNonNull(connection, "connection");
            this.sender = sender;
            this.mainThread = Objects.requireNonNull(mainThread, "mainThread");
        }

        public NetworkDirection getDirection() {
            return direction;
        }

        public Connection getNetworkManager() {
            return connection;
        }

        /** The sending player for client-to-server packets, otherwise null. */
        public @Nullable ServerPlayer getSender() {
            return sender;
        }

        /** Runs the task on the receiving side's main thread: inline when already there, as Forge did. */
        public CompletableFuture<Void> enqueueWork(Runnable task) {
            if (mainThread.isSameThread()) {
                task.run();
                return CompletableFuture.completedFuture(null);
            }
            return CompletableFuture.runAsync(task, mainThread);
        }

        public void setPacketHandled(boolean handled) {
            packetHandled = handled;
        }

        public boolean getPacketHandled() {
            return packetHandled;
        }
    }
}
