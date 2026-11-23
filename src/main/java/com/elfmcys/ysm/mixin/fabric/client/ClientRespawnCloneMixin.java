/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.mixin.fabric.client;

import com.elfmcys.ysm.fabric.FabricEventHooks;
import com.elfmcys.ysm.platform.event.forge.ClientPlayerNetworkEvent;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Forge's ClientPlayerNetworkEvent.Clone: end of handleRespawn, once the new LocalPlayer replaced the old one. */
@Mixin(ClientPacketListener.class)
public abstract class ClientRespawnCloneMixin {
    @Inject(method = "handleRespawn", at = @At("TAIL"))
    private void ysm$afterRespawn(ClientboundRespawnPacket packet, CallbackInfo ci,
        @Local(ordinal = 0) LocalPlayer oldPlayer) {
        var newPlayer = Minecraft.getInstance().player;
        if (newPlayer != null && newPlayer != oldPlayer)
            FabricEventHooks.post(
                new ClientPlayerNetworkEvent.Clone(oldPlayer, newPlayer, (ClientPacketListener)(Object)this));
    }
}
