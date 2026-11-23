/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.platform.event.forge;

import com.elfmcys.ysm.platform.event.Event;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

/** Fabric port: posted when a living entity blocks damage with a shield. */
public class ShieldBlockEvent extends Event {
    private final LivingEntity entity;
    private final DamageSource source;
    private final float blockedDamage;

    public ShieldBlockEvent(LivingEntity entity, DamageSource source, float blockedDamage) {
        this.entity = entity;
        this.source = source;
        this.blockedDamage = blockedDamage;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public DamageSource getDamageSource() {
        return source;
    }

    public float getBlockedDamage() {
        return blockedDamage;
    }
}
