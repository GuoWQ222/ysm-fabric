/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.legacy.item;

import com.elfmcys.ysm.mixin.fabric.ArrowPotionAccessor;
import com.google.common.collect.Iterables;
import net.minecraft.world.effect.MobEffectInstance;

/**
 * Minecraft before 1.20.5: an arrow's effects as 1.20.5's PotionContents lists them (the potion's, then the extra
 * ones). versions/below-1.20.5.remap points the PotionContents calls of YSM's Molang functions here.
 */
public final class ArrowPotions {
    private ArrowPotions() {}

    public record Contents(Iterable<MobEffectInstance> allEffects) {
        public Iterable<MobEffectInstance> getAllEffects() {
            return allEffects;
        }
    }

    public static Contents of(Object arrow) {
        var accessor = (ArrowPotionAccessor)arrow;
        var potion = accessor.ysm$potion();
        return new Contents(potion == null ? accessor.ysm$effects()
            : Iterables.concat(potion.getEffects(), accessor.ysm$effects()));
    }
}
