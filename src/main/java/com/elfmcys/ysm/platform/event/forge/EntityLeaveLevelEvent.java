/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.platform.event.forge;

import com.elfmcys.ysm.platform.event.Event;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

/** Fabric port: posted from Fabric server/client entity load/unload callbacks. */
public class EntityLeaveLevelEvent extends Event {
    private final Entity entity;
    private final Level level;

    public EntityLeaveLevelEvent(Entity entity, Level level) {
        this.entity = entity;
        this.level = level;
    }

    public Entity getEntity() {
        return entity;
    }

    public Level getLevel() {
        return level;
    }
}
