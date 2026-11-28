/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.mixin.fabric;

import java.util.Set;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.alchemy.Potion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Minecraft before 1.20.5 (replaces the main file there): an arrow keeps its potion and its extra effects instead of
 * PotionContents; com.elfmcys.ysm.legacy.item.ArrowPotions puts them together.
 */
@Mixin(Arrow.class)
public interface ArrowPotionAccessor {
    @Accessor("potion")
    Potion ysm$potion();

    @Accessor("effects")
    Set<MobEffectInstance> ysm$effects();
}
