/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.neoforge;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.platform.event.YsmEvents;
import com.elfmcys.ysm.platform.event.forge.AddReloadListenerEvent;
import com.elfmcys.ysm.platform.event.forge.FMLCommonSetupEvent;
import com.elfmcys.ysm.platform.event.forge.TickEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;

/**
 * NeoForge glue: posts YSM's Forge-shaped platform events from NeoForge's events (the counterpart of the Fabric
 * port's FabricEventHooks). Events NeoForge has no hook for keep coming from the shared mixins (login, input, screen
 * opening, client respawn, living tick).
 */
public final class NeoForgeEventHooks {
    private NeoForgeEventHooks() {}

    public static void registerCommon() {
        var bus = NeoForge.EVENT_BUS;
        bus.addListener((net.neoforged.neoforge.event.tick.ServerTickEvent.Pre event) ->
            YsmEvents.post(new TickEvent.ServerTickEvent(TickEvent.Phase.START, event.getServer())));
        bus.addListener((net.neoforged.neoforge.event.tick.ServerTickEvent.Post event) ->
            YsmEvents.post(new TickEvent.ServerTickEvent(TickEvent.Phase.END, event.getServer())));
        // PlayerLoggedInEvent: posted by the shared PlayerListLoginMixin (same order on every loader).
        bus.addListener((net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer player)
                YsmEvents.post(new com.elfmcys.ysm.platform.event.forge.PlayerEvent.PlayerLoggedOutEvent(player));
        });
        bus.addListener((net.neoforged.neoforge.event.entity.player.PlayerEvent.Clone event) -> {
            if (event.getEntity() instanceof ServerPlayer player)
                YsmEvents.post(new com.elfmcys.ysm.platform.event.forge.PlayerEvent.Clone(player, event.getOriginal(),
                    event.isWasDeath()));
        });
        bus.addListener((net.neoforged.neoforge.event.entity.player.PlayerEvent.StartTracking event) -> {
            if (event.getEntity() instanceof ServerPlayer player)
                YsmEvents.post(new com.elfmcys.ysm.platform.event.forge.PlayerEvent.StartTracking(player,
                    event.getTarget()));
        });
        // NeoForge fires these for client and server levels, as Fabric's server + client entity events together.
        bus.addListener((net.neoforged.neoforge.event.entity.EntityJoinLevelEvent event) ->
            YsmEvents.post(new com.elfmcys.ysm.platform.event.forge.EntityJoinLevelEvent(event.getEntity(),
                event.getLevel())));
        bus.addListener((net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent event) ->
            YsmEvents.post(new com.elfmcys.ysm.platform.event.forge.EntityLeaveLevelEvent(event.getEntity(),
                event.getLevel())));
        bus.addListener((net.neoforged.neoforge.event.level.LevelEvent.Unload event) -> {
            if (event.getLevel() instanceof ServerLevel level)
                YsmEvents.post(new com.elfmcys.ysm.platform.event.forge.LevelEvent.Unload(level));
        });
        bus.addListener((net.neoforged.neoforge.event.RegisterCommandsEvent event) ->
            YsmEvents.post(new com.elfmcys.ysm.platform.event.forge.RegisterCommandsEvent(event.getDispatcher(),
                event.getBuildContext(), event.getCommandSelection())));
        com.elfmcys.ysm.platform.ServerHolder.register();
        bus.addListener((net.neoforged.neoforge.event.server.ServerAboutToStartEvent event) ->
            YsmEvents.post(new com.elfmcys.ysm.platform.event.forge.ServerAboutToStartEvent(event.getServer())));
        bus.addListener((net.neoforged.neoforge.event.server.ServerStoppingEvent event) ->
            YsmEvents.post(new com.elfmcys.ysm.platform.event.forge.ServerStoppingEvent(event.getServer())));
        bus.addListener((net.neoforged.neoforge.event.entity.living.MobEffectEvent.Added event) -> {
            if (!event.getEntity().level().isClientSide())
                YsmEvents.post(new com.elfmcys.ysm.platform.event.forge.MobEffectEvent.Added(event.getEntity(),
                    event.getEffectInstance()));
        });
        bus.addListener((net.neoforged.neoforge.event.entity.living.MobEffectEvent.Remove event) -> {
            if (!event.getEntity().level().isClientSide() && event.getEffectInstance() != null)
                YsmEvents.post(new com.elfmcys.ysm.platform.event.forge.MobEffectEvent.Remove(event.getEntity(),
                    event.getEffect(), event.getEffectInstance()));
        });
        bus.addListener((net.neoforged.neoforge.event.AddServerReloadListenersEvent event) -> {
            var reload = new AddReloadListenerEvent();
            YsmEvents.post(reload);
            int i = 0;
            for (var listener : reload.getListeners())
                event.addListener(Identifier.fromNamespaceAndPath(YesSteveModel.MOD_ID, "reload_" + i++), listener);
        });
        YsmEvents.post(new FMLCommonSetupEvent());
    }
}
