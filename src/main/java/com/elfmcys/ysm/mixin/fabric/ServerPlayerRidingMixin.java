/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.mixin.fabric;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.event.CapabilityEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Fabric port of ServerPlayerMixin: the driver's vehicle model is applied once the player is seated. */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerRidingMixin {
    @Inject(method = "startRiding(Lnet/minecraft/world/entity/Entity;ZZ)Z",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;positionRider(Lnet/minecraft/world/entity/Entity;)V",
            shift = At.Shift.AFTER))
    private void ysm$afterPositionRider(Entity vehicle, boolean force, boolean sendEventAndTriggers,
        CallbackInfoReturnable<Boolean> cir) {
        if (!YesSteveModel.isAvailable())
            return;
        var player = (ServerPlayer)(Object)this;
        if (vehicle.getFirstPassenger() == player)
            CapabilityEvent.onVehicleSetModel(vehicle, player);
    }
}
