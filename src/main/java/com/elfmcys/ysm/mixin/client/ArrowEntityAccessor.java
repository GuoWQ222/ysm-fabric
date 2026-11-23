// Modified for the unofficial Fabric 26.2 port by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.mixin.client;

import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(Arrow.class)
public interface ArrowEntityAccessor {
    @Accessor("effects")
    Set<MobEffectInstance> getEffects();
}
