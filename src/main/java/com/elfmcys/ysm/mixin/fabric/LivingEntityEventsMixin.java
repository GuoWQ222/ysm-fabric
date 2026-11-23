/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.mixin.fabric;

import com.elfmcys.ysm.platform.ShieldBlockCooldownHolder;
import com.elfmcys.ysm.platform.event.MinecraftForge;
import com.elfmcys.ysm.platform.event.forge.LivingEvent;
import com.elfmcys.ysm.platform.event.forge.ShieldBlockEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Fabric port: posts Forge's LivingTickEvent and ShieldBlockEvent, and stores the shield-block cooldown. */
@Mixin(LivingEntity.class)
public abstract class LivingEntityEventsMixin implements ShieldBlockCooldownHolder {
    @Unique
    private int ysm$shieldBlockCooldown = -1;

    @Inject(method = "tick", at = @At("HEAD"))
    private void ysm$livingTick(CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new LivingEvent.LivingTickEvent((LivingEntity)(Object)this));
    }

    @Inject(method = "applyItemBlocking", at = @At("RETURN"))
    private void ysm$shieldBlocked(ServerLevel level, DamageSource source, float damage,
        CallbackInfoReturnable<Float> cir) {
        if (cir.getReturnValueF() > 0.0F)
            MinecraftForge.EVENT_BUS.post(new ShieldBlockEvent((LivingEntity)(Object)this, source, damage));
    }

    @Override
    public int ysm$getShieldBlockCooldown() {
        return ysm$shieldBlockCooldown;
    }

    @Override
    public void ysm$setShieldBlockCooldown(int ticks) {
        ysm$shieldBlockCooldown = ticks;
    }
}
