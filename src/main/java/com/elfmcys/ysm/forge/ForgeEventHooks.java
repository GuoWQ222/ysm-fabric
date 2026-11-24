/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.forge;

import com.elfmcys.ysm.platform.event.YsmEvents;
import com.elfmcys.ysm.platform.event.forge.AddReloadListenerEvent;
import com.elfmcys.ysm.platform.event.forge.FMLCommonSetupEvent;
import com.elfmcys.ysm.platform.event.forge.TickEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * Forge glue: translates Forge's events into YSM's internal ones (the counterpart of FabricEventHooks and
 * NeoForgeEventHooks). EventBus 7: one bus per event type. Listeners are block lambdas so a cancellable bus picks the
 * non-cancelling Consumer overload (YsmEvents.post returns a boolean).
 */
public final class ForgeEventHooks {
    private ForgeEventHooks() {}

    public static void registerCommon() {
        net.minecraftforge.event.TickEvent.ServerTickEvent.Pre.BUS.addListener(event -> {
            YsmEvents.post(new TickEvent.ServerTickEvent(TickEvent.Phase.START, event.server()));
        });
        net.minecraftforge.event.TickEvent.ServerTickEvent.Post.BUS.addListener(event -> {
            YsmEvents.post(new TickEvent.ServerTickEvent(TickEvent.Phase.END, event.server()));
        });
        // PlayerLoggedInEvent: posted by the shared PlayerListLoginMixin (same order on every loader).
        net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent.BUS.addListener(event -> {
            if (event.getEntity() instanceof ServerPlayer player)
                YsmEvents.post(new com.elfmcys.ysm.platform.event.forge.PlayerEvent.PlayerLoggedOutEvent(player));
        });
        net.minecraftforge.event.entity.player.PlayerEvent.Clone.BUS.addListener(event -> {
            if (event.getEntity() instanceof ServerPlayer player)
                YsmEvents.post(new com.elfmcys.ysm.platform.event.forge.PlayerEvent.Clone(player, event.getOriginal(),
                    event.isWasDeath()));
        });
        net.minecraftforge.event.entity.player.PlayerEvent.StartTracking.BUS.addListener(event -> {
            if (event.getEntity() instanceof ServerPlayer player)
                YsmEvents.post(new com.elfmcys.ysm.platform.event.forge.PlayerEvent.StartTracking(player,
                    event.getTarget()));
        });
        // Forge fires these for client and server levels, as Fabric's server + client entity events together.
        net.minecraftforge.event.entity.EntityJoinLevelEvent.BUS.addListener(event -> {
            YsmEvents.post(new com.elfmcys.ysm.platform.event.forge.EntityJoinLevelEvent(event.getEntity(),
                event.getLevel()));
        });
        net.minecraftforge.event.entity.EntityLeaveLevelEvent.BUS.addListener(event -> {
            YsmEvents.post(new com.elfmcys.ysm.platform.event.forge.EntityLeaveLevelEvent(event.getEntity(),
                event.getLevel()));
        });
        net.minecraftforge.event.level.LevelEvent.Unload.BUS.addListener(event -> {
            if (event.getLevel() instanceof ServerLevel level)
                YsmEvents.post(new com.elfmcys.ysm.platform.event.forge.LevelEvent.Unload(level));
        });
        net.minecraftforge.event.RegisterCommandsEvent.BUS.addListener(event -> {
            YsmEvents.post(new com.elfmcys.ysm.platform.event.forge.RegisterCommandsEvent(event.getDispatcher(),
                event.getBuildContext(), event.getCommandSelection()));
        });
        com.elfmcys.ysm.platform.ServerHolder.register();
        net.minecraftforge.event.server.ServerAboutToStartEvent.BUS.addListener(event -> {
            YsmEvents.post(new com.elfmcys.ysm.platform.event.forge.ServerAboutToStartEvent(event.getServer()));
        });
        net.minecraftforge.event.server.ServerStoppingEvent.BUS.addListener(event -> {
            YsmEvents.post(new com.elfmcys.ysm.platform.event.forge.ServerStoppingEvent(event.getServer()));
        });
        net.minecraftforge.event.entity.living.MobEffectEvent.Added.BUS.addListener(event -> {
            if (!event.getEntity().level().isClientSide())
                YsmEvents.post(new com.elfmcys.ysm.platform.event.forge.MobEffectEvent.Added(event.getEntity(),
                    event.getEffectInstance()));
        });
        net.minecraftforge.event.entity.living.MobEffectEvent.Remove.BUS.addListener(event -> {
            if (!event.getEntity().level().isClientSide() && event.getEffectInstance() != null)
                YsmEvents.post(new com.elfmcys.ysm.platform.event.forge.MobEffectEvent.Remove(event.getEntity(),
                    event.getEffectInstance().getEffect(), event.getEffectInstance()));
        });
        net.minecraftforge.event.AddReloadListenerEvent.BUS.addListener(event -> {
            var reload = new AddReloadListenerEvent();
            YsmEvents.post(reload);
            for (var listener : reload.getListeners())
                event.addListener(listener);
        });
        YsmEvents.post(new FMLCommonSetupEvent());
    }
}
