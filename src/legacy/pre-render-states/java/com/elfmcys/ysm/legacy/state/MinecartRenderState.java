/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.legacy.state;

import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/** 1.21.2's minecart render state for older Minecraft: rotation and the rail sample points (see StateExtractor). */
public class MinecartRenderState extends EntityRenderState {
    public boolean isNewRender;
    public float xRot;
    public float yRot;
    public @Nullable Vec3 posOnRail;
    public @Nullable Vec3 frontPos;
    public @Nullable Vec3 backPos;
}
