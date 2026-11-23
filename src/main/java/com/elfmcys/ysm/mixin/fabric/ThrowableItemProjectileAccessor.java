package com.elfmcys.ysm.mixin.fabric;

import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Fabric port: the default item of a throwable projectile, for Molang. */
@Mixin(ThrowableItemProjectile.class)
public interface ThrowableItemProjectileAccessor {
    @Invoker("getDefaultItem")
    Item ysm$GetDefaultItem();
}
