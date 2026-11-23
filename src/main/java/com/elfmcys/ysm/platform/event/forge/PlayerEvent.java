/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.platform.event.forge;

import com.elfmcys.ysm.platform.event.Event;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/** Fabric port: Forge player events, posted from Fabric connection/tracking/respawn callbacks. */
public class PlayerEvent extends Event {
    private final Player player;

    protected PlayerEvent(Player player) {
        this.player = player;
    }

    public Player getEntity() {
        return player;
    }

    public static class PlayerLoggedInEvent extends PlayerEvent {
        public PlayerLoggedInEvent(Player player) {
            super(player);
        }
    }

    public static class PlayerLoggedOutEvent extends PlayerEvent {
        public PlayerLoggedOutEvent(Player player) {
            super(player);
        }
    }

    public static class Clone extends PlayerEvent {
        private final Player original;
        private final boolean wasDeath;

        public Clone(Player newPlayer, Player original, boolean wasDeath) {
            super(newPlayer);
            this.original = original;
            this.wasDeath = wasDeath;
        }

        public Player getOriginal() {
            return original;
        }

        public boolean isWasDeath() {
            return wasDeath;
        }
    }

    public static class StartTracking extends PlayerEvent {
        private final Entity target;

        public StartTracking(Player player, Entity target) {
            super(player);
            this.target = target;
        }

        public Entity getTarget() {
            return target;
        }
    }
}
