/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.mixin.fabric;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.event.CapabilityEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Fabric port of ProjectileEntityMixin: a player-owned projectile takes the owner's projectile model (server). */
@Mixin(Projectile.class)
public abstract class ProjectileOwnerMixin {
    @Inject(method = "setOwner(Lnet/minecraft/world/entity/Entity;)V", at = @At("RETURN"))
    private void ysm$setOwner(Entity owner, CallbackInfo ci) {
        if (!YesSteveModel.isAvailable())
            return;
        var projectile = (Projectile)(Object)this;
        if (projectile.level() == null || projectile.level().isClientSide())
            return;
        if (owner instanceof ServerPlayer player)
            CapabilityEvent.onProjectileSetOwner(projectile, player);
    }
}
