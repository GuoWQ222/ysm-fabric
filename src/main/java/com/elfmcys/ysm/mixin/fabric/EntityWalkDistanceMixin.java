/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.mixin.fabric;

import com.elfmcys.ysm.platform.EntityWalkDistance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Fabric port: restores 1.20's walkDist (see {@link EntityWalkDistance}). */
@Mixin(Entity.class)
public abstract class EntityWalkDistanceMixin implements EntityWalkDistance {
    @Unique
    private float ysm$walkDist;

    @Inject(method = "applyMovementEmissionAndPlaySound", at = @At("HEAD"))
    private void ysm$accumulateWalkDist(Entity.MovementEmission emission, Vec3 clippedMovement,
        net.minecraft.core.BlockPos effectPos, net.minecraft.world.level.block.state.BlockState effectState,
        CallbackInfo ci) {
        ysm$walkDist += (float)(clippedMovement.horizontalDistance() * 0.6F);
    }

    @Override
    public float ysm$walkDist() {
        return ysm$walkDist;
    }
}
