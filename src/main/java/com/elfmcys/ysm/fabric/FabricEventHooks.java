/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.fabric;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.platform.event.MinecraftForge;
import com.elfmcys.ysm.platform.event.forge.AddReloadListenerEvent;
import com.elfmcys.ysm.platform.event.forge.EntityJoinLevelEvent;
import com.elfmcys.ysm.platform.event.forge.EntityLeaveLevelEvent;
import com.elfmcys.ysm.platform.event.forge.FMLCommonSetupEvent;
import com.elfmcys.ysm.platform.event.forge.LevelEvent;
import com.elfmcys.ysm.platform.event.forge.MobEffectEvent;
import com.elfmcys.ysm.platform.event.forge.PlayerEvent;
import com.elfmcys.ysm.platform.event.forge.RegisterCommandsEvent;
import com.elfmcys.ysm.platform.event.forge.ServerAboutToStartEvent;
import com.elfmcys.ysm.platform.event.forge.ServerStoppingEvent;
import com.elfmcys.ysm.platform.event.forge.TickEvent;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.entity.event.v1.effect.ServerMobEffectEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;

/**
 * Fabric port: posts the Forge-shaped platform events from Fabric API callbacks. Events that Fabric has no
 * callback for (input, screen opening, client respawn, shield blocking, living tick) are posted from mixins.
 */
public final class FabricEventHooks {
    private FabricEventHooks() {}

    public static void postPlayerLoggedIn(net.minecraft.server.level.ServerPlayer player) {
        post(new PlayerEvent.PlayerLoggedInEvent(player));
    }

    public static boolean post(com.elfmcys.ysm.platform.event.Event event) {
        return MinecraftForge.EVENT_BUS.post(event);
    }

    public static void registerCommon() {
        ServerTickEvents.START_SERVER_TICK.register(server -> post(new TickEvent.ServerTickEvent(TickEvent.Phase.START, server)));
        ServerTickEvents.END_SERVER_TICK.register(server -> post(new TickEvent.ServerTickEvent(TickEvent.Phase.END, server)));
        // PlayerLoggedInEvent: posted by PlayerListLoginMixin at the end of placeNewPlayer (Forge order).
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> post(new PlayerEvent.PlayerLoggedOutEvent(handler.player)));
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> post(new PlayerEvent.Clone(newPlayer, oldPlayer, !alive)));
        EntityTrackingEvents.START_TRACKING.register((entity, player) -> post(new PlayerEvent.StartTracking(player, entity)));
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> post(new EntityJoinLevelEvent(entity, level)));
        ServerEntityEvents.ENTITY_UNLOAD.register((entity, level) -> post(new EntityLeaveLevelEvent(entity, level)));
        ServerLevelEvents.UNLOAD.register((server, level) -> post(new LevelEvent.Unload(level)));
        CommandRegistrationCallback.EVENT.register((dispatcher, context, selection) -> post(new RegisterCommandsEvent(dispatcher, context, selection)));
        com.elfmcys.ysm.platform.ServerHolder.register();
        ServerLifecycleEvents.SERVER_STARTING.register(server -> post(new ServerAboutToStartEvent(server)));
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> post(new ServerStoppingEvent(server)));
        ServerMobEffectEvents.AFTER_ADD.register((effect, entity, context) -> post(new MobEffectEvent.Added(entity, effect)));
        ServerMobEffectEvents.AFTER_REMOVE.register((effect, entity, context) -> post(new MobEffectEvent.Remove(entity, effect.getEffect(), effect)));

        var reload = new AddReloadListenerEvent();
        post(reload);
        int i = 0;
        for (var listener : reload.getListeners())
            ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(
                Identifier.fromNamespaceAndPath(YesSteveModel.MOD_ID, "reload_" + i++), listener);
    }

    public static void postCommonSetup() {
        post(new FMLCommonSetupEvent());
    }
}
