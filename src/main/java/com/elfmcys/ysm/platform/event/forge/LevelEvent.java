/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.platform.event.forge;

import com.elfmcys.ysm.platform.event.Event;
import net.minecraft.world.level.LevelAccessor;

/** Fabric port: Forge level events. */
public class LevelEvent extends Event {
    private final LevelAccessor level;

    protected LevelEvent(LevelAccessor level) {
        this.level = level;
    }

    public LevelAccessor getLevel() {
        return level;
    }

    public static class Unload extends LevelEvent {
        public Unload(LevelAccessor level) {
            super(level);
        }
    }
}
