/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.platform.event.forge;

import com.elfmcys.ysm.platform.event.Event;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

/** Fabric port: Forge mob effect events, posted from LivingEntity mixins. */
public class MobEffectEvent extends Event {
    private final LivingEntity entity;
    private final @Nullable MobEffectInstance effectInstance;

    protected MobEffectEvent(LivingEntity entity, @Nullable MobEffectInstance effectInstance) {
        this.entity = entity;
        this.effectInstance = effectInstance;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public @Nullable MobEffectInstance getEffectInstance() {
        return effectInstance;
    }

    public static class Added extends MobEffectEvent {
        public Added(LivingEntity entity, MobEffectInstance effectInstance) {
            super(entity, effectInstance);
        }
    }

    public static class Remove extends MobEffectEvent {
        private final Holder<MobEffect> effect;

        public Remove(LivingEntity entity, Holder<MobEffect> effect, @Nullable MobEffectInstance effectInstance) {
            super(entity, effectInstance);
            this.effect = effect;
        }

        public Holder<MobEffect> getEffect() {
            return effect;
        }
    }

    public static class Expired extends MobEffectEvent {
        public Expired(LivingEntity entity, MobEffectInstance effectInstance) {
            super(entity, effectInstance);
        }
    }
}
