/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.mixin.fabric;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.accessor.IArrowExtraInfo;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fabric port of AbstractArrowEntityMixin: exposes in-ground state (synced by vanilla in 26.x) and caches the
 * shooter's main-hand item id when the owner is set, for Molang.
 */
@Mixin(AbstractArrow.class)
public abstract class AbstractArrowExtraInfoMixin implements IArrowExtraInfo {
    @Shadow
    protected int inGroundTime;
    @Unique
    private String ysm$shootItemId = "";

    @Shadow
    protected abstract boolean isInGround();

    @Override
    public boolean ysm$isInGround() {
        return isInGround();
    }

    @Override
    public int ysm$inGroundTime() {
        return inGroundTime;
    }

    @Override
    public String ysm$getShootItemId() {
        return ysm$shootItemId;
    }

    @Inject(method = "setOwner(Lnet/minecraft/world/entity/Entity;)V", at = @At("RETURN"))
    private void ysm$cacheShootItem(Entity owner, CallbackInfo ci) {
        if (!YesSteveModel.isAvailable())
            return;
        if (owner instanceof LivingEntity living) {
            var key = BuiltInRegistries.ITEM.getKey(living.getMainHandItem().getItem());
            ysm$shootItemId = key.toString();
        }
    }
}
