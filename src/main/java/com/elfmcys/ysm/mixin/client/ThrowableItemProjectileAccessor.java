// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.mixin.client;

import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ThrowableItemProjectile.class)
public interface ThrowableItemProjectileAccessor {
    @Invoker("getDefaultItem")
    Item ysm$GetDefaultItem();
}
