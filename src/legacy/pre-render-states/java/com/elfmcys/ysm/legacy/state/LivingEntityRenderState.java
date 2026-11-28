/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.legacy.state;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.Pose;
import org.jetbrains.annotations.Nullable;

/** 1.21.2's living-entity render state for older Minecraft (see EntityRenderState). */
public class LivingEntityRenderState extends EntityRenderState {
    public float bodyRot;
    public float yRot;
    public float xRot;
    public float deathTime;
    public float walkAnimationPos;
    public float walkAnimationSpeed;
    public float scale = 1.0F;
    public boolean isUpsideDown;
    public boolean isFullyFrozen;
    public boolean isBaby;
    public Pose pose = Pose.STANDING;
    public @Nullable Direction bedOrientation;

    public boolean hasPose(Pose pose) {
        return this.pose == pose;
    }
}
