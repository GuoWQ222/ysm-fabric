/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.legacy.state;

import net.minecraft.world.phys.Vec3;

/** 1.21.2's fishing-hook render state for older Minecraft: the line's start relative to the hook. */
public class FishingHookRenderState extends EntityRenderState {
    public Vec3 lineOriginOffset = Vec3.ZERO;
}
