/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.platform.event;

/** Fabric port: the single YSM event bus (Forge had a game bus and a mod bus; both map here). */
public final class MinecraftForge {
    public static final IEventBus EVENT_BUS = new EventBus("ysm");

    private MinecraftForge() {}
}
