/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.mixin.fabric;

import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.alchemy.PotionContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Fabric port: 26.x arrows keep their effects in PotionContents (was a Set of effect instances). */
@Mixin(Arrow.class)
public interface ArrowPotionAccessor {
    @Invoker("getPotionContents")
    PotionContents ysm$getPotionContents();
}
