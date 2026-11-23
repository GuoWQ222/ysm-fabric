/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.fabric;

import com.elfmcys.ysm.platform.event.forge.ClientPlayerNetworkEvent;
import com.elfmcys.ysm.platform.event.forge.EntityJoinLevelEvent;
import com.elfmcys.ysm.platform.event.forge.EntityLeaveLevelEvent;
import com.elfmcys.ysm.platform.event.forge.FMLClientSetupEvent;
import com.elfmcys.ysm.platform.event.forge.FMLLoadCompleteEvent;
import com.elfmcys.ysm.platform.event.forge.RegisterKeyMappingsEvent;
import com.elfmcys.ysm.platform.event.forge.TickEvent;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

/**
 * Client-only Fabric callbacks. Kept apart from {@link FabricEventHooks}: a dedicated server loads that class, and
 * verifying these lambdas there would load client classes such as ClientLevel.
 */
public final class FabricClientEventHooks {
    private FabricClientEventHooks() {
    }

    public static void register() {
        com.elfmcys.ysm.client.event.ClientCommandHandler.register();
        com.elfmcys.ysm.client.gui.pip.YsmGuiModels.register();
        com.elfmcys.ysm.client.event.ClientSetupEvent.registerHudElements();
        ClientTickEvents.START_CLIENT_TICK.register(client -> FabricEventHooks.post(new TickEvent.ClientTickEvent(TickEvent.Phase.START)));
        ClientTickEvents.END_CLIENT_TICK.register(client -> FabricEventHooks.post(new TickEvent.ClientTickEvent(TickEvent.Phase.END)));
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> FabricEventHooks.post(new ClientPlayerNetworkEvent.LoggingIn(client.player, handler)));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> FabricEventHooks.post(new ClientPlayerNetworkEvent.LoggingOut(client.player, handler)));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> com.elfmcys.ysm.model.resource.client.SoundAcceptance.reset());
        ClientEntityEvents.ENTITY_LOAD.register((entity, level) -> FabricEventHooks.post(new EntityJoinLevelEvent(entity, level)));
        ClientEntityEvents.ENTITY_UNLOAD.register((entity, level) -> FabricEventHooks.post(new EntityLeaveLevelEvent(entity, level)));
        // Forge ran client setup during the first resource reload, after the window and GPU device existed. Fabric
        // client initializers run inside the Minecraft constructor before both, so setup waits for the main loop.
        // 3.0-dev PauseScreenMixin: skin button on the pause screen, mobile launchers only.
        net.fabricmc.fabric.api.client.screen.v1.ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            if (screen instanceof net.minecraft.client.gui.screens.PauseScreen pause) {
                var buttons = com.elfmcys.ysm.client.gui.AndroidCompat.addYsmSkinButton(pause);
                if (buttons != null)
                    net.fabricmc.fabric.api.client.screen.v1.Screens.getWidgets(screen).addAll(buttons);
            }
        });
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            FabricEventHooks.post(new FMLClientSetupEvent());
            FabricEventHooks.post(new FMLLoadCompleteEvent());
        });
        FabricEventHooks.post(new RegisterKeyMappingsEvent());
    }
}
