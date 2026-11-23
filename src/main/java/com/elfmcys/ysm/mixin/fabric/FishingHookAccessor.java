package com.elfmcys.ysm.mixin.fabric;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FishingHook;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Fabric port: fishing hook state for Molang (biting, hooked entity). */
@Mixin(FishingHook.class)
public interface FishingHookAccessor {
    @Accessor("biting")
    boolean ysm$IsBiting();

    @Accessor("hookedIn")
    @Nullable
    Entity ysm$GetHookedIn();
}
