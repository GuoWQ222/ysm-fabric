/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.mixin.fabric;

import com.elfmcys.ysm.fabric.FabricEventHooks;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Forge fires PlayerLoggedInEvent at the end of placeNewPlayer, after the player joined its level. Fabric's
 * ServerPlayConnectionEvents.JOIN fires earlier (before level.addNewPlayer), which opened the model session before
 * the entity-join handler ran and made it query a session that was not yet active.
 */
@Mixin(PlayerList.class)
public abstract class PlayerListLoginMixin {
    @Inject(method = "placeNewPlayer", at = @At("TAIL"))
    private void ysm$afterPlaceNewPlayer(Connection connection, ServerPlayer player, CommonListenerCookie cookie,
        CallbackInfo ci) {
        FabricEventHooks.postPlayerLoggedIn(player);
    }
}
