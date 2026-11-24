/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.neoforge;

import com.elfmcys.ysm.platform.event.YsmEvents;
import com.elfmcys.ysm.platform.event.forge.FMLClientSetupEvent;
import com.elfmcys.ysm.platform.event.forge.FMLLoadCompleteEvent;
import com.elfmcys.ysm.platform.event.forge.RegisterKeyMappingsEvent;
import com.elfmcys.ysm.platform.event.forge.TickEvent;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.common.NeoForge;

/**
 * NeoForge client glue (the counterpart of FabricClientEventHooks). Entity join/leave on client levels comes from the
 * common hooks, as NeoForge fires those events for both sides.
 */
public final class NeoForgeClientEventHooks {
    private NeoForgeClientEventHooks() {}

    public static void register() {
        var modBus = YsmNeoForge.modBus();
        var bus = NeoForge.EVENT_BUS;
        com.elfmcys.ysm.client.gui.pip.YsmGuiModels.register();
        com.elfmcys.ysm.client.event.ClientCommandHandler.register();
        modBus.addListener((net.neoforged.neoforge.client.event.RegisterGuiLayersEvent event) ->
            com.elfmcys.ysm.client.event.ClientSetupEvent.hudLayers().forEach((id, layer) ->
                // NeoForge draws registered layers even while the HUD is hidden (F1); Fabric/Forge-era YSM hid them
                event.registerAboveAll(id, (graphics, delta) -> {
                    if (!com.elfmcys.ysm.platform.McCompat.hudHidden())
                        layer.extractRenderState(graphics, delta);
                })));
        // YSM's key mappings are collected by its own event, then registered in NeoForge's
        YsmEvents.post(new RegisterKeyMappingsEvent());
        modBus.addListener((net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent event) ->
            RegisterKeyMappingsEvent.COLLECTED.forEach(event::register));
        bus.addListener((net.neoforged.neoforge.client.event.ClientTickEvent.Pre event) ->
            YsmEvents.post(new TickEvent.ClientTickEvent(TickEvent.Phase.START)));
        bus.addListener((net.neoforged.neoforge.client.event.ClientTickEvent.Post event) ->
            YsmEvents.post(new TickEvent.ClientTickEvent(TickEvent.Phase.END)));
        bus.addListener((net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingIn event) ->
            YsmEvents.post(new com.elfmcys.ysm.platform.event.forge.ClientPlayerNetworkEvent.LoggingIn(
                event.getPlayer(), Minecraft.getInstance().getConnection())));
        bus.addListener((net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut event) -> {
            YsmEvents.post(new com.elfmcys.ysm.platform.event.forge.ClientPlayerNetworkEvent.LoggingOut(
                event.getPlayer(), Minecraft.getInstance().getConnection()));
            com.elfmcys.ysm.model.resource.client.SoundAcceptance.reset();
        });
        // 3.0-dev PauseScreenMixin: skin button on the pause screen, mobile launchers only.
        bus.addListener((net.neoforged.neoforge.client.event.ScreenEvent.Init.Post event) -> {
            if (event.getScreen() instanceof net.minecraft.client.gui.screens.PauseScreen pause) {
                var buttons = com.elfmcys.ysm.client.gui.AndroidCompat.addYsmSkinButton(pause);
                if (buttons != null)
                    buttons.forEach(event::addListener);
            }
        });
        // YSM's client setup blocks the render thread until its startup work is done, draining queued tasks. It runs once,
        // after the first frame: inside mod loading it deadlocks (setup runs on a worker the render thread waits for),
        // and inside the first tick's task queue it can finish the resource reload before any frame, after which that
        // tick's texture animation draws without the Globals uniform that only GameRenderer.render sets.
        var setupDone = new java.util.concurrent.atomic.AtomicBoolean();
        bus.addListener((net.neoforged.neoforge.client.event.RenderFrameEvent.Post event) -> {
            if (setupDone.compareAndSet(false, true)) {
                YsmEvents.post(new FMLClientSetupEvent());
                YsmEvents.post(new FMLLoadCompleteEvent());
            }
        });
    }
}
