/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.platform.network;

import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

/** Fabric port: the PacketDistributor targets YSM used (server, one player, entity trackers). */
public final class PacketTarget {
    private final NetworkDirection direction;
    private final Consumer<Packet<?>> sender;

    private PacketTarget(NetworkDirection direction, Consumer<Packet<?>> sender) {
        this.direction = direction;
        this.sender = sender;
    }

    public NetworkDirection getDirection() {
        return direction;
    }

    public void send(Packet<?> packet) {
        sender.accept(packet);
    }

    /** Wraps a payload in the vanilla custom-payload packet for this target's direction. */
    public Packet<?> packet(CustomPacketPayload payload) {
        return direction == NetworkDirection.PLAY_TO_SERVER ? new ServerboundCustomPayloadPacket(payload)
            : new ClientboundCustomPayloadPacket(payload);
    }

    public static PacketTarget server() {
        return new PacketTarget(NetworkDirection.PLAY_TO_SERVER, packet -> {
            var listener = net.minecraft.client.Minecraft.getInstance().getConnection();
            if (listener != null)
                listener.send(packet);
        });
    }

    public static PacketTarget player(ServerPlayer player) {
        Objects.requireNonNull(player, "player");
        return new PacketTarget(NetworkDirection.PLAY_TO_CLIENT, packet -> player.connection.send(packet));
    }

    @SuppressWarnings("unchecked")
    public static PacketTarget trackingEntity(Entity entity) {
        return new PacketTarget(NetworkDirection.PLAY_TO_CLIENT, packet -> {
            if (entity.level() instanceof ServerLevel level)
                level.getChunkSource().sendToTrackingPlayers(entity,
                    (Packet<? super net.minecraft.network.protocol.game.ClientGamePacketListener>)packet);
        });
    }

    @SuppressWarnings("unchecked")
    public static PacketTarget trackingEntityAndSelf(Entity entity) {
        return new PacketTarget(NetworkDirection.PLAY_TO_CLIENT, packet -> {
            if (entity.level() instanceof ServerLevel level)
                level.getChunkSource().sendToTrackingPlayersAndSelf(entity,
                    (Packet<? super net.minecraft.network.protocol.game.ClientGamePacketListener>)packet);
        });
    }
}
