/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.platform.event;

/** Posts YSM's Forge-shaped platform events on its own bus (loader-neutral; each loader's hooks call it). */
public final class YsmEvents {
    private YsmEvents() {}

    /** @return true when a cancelable event was canceled */
    public static boolean post(Event event) {
        return MinecraftForge.EVENT_BUS.post(event);
    }
}
