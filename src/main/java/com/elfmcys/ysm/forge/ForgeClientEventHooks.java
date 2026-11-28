/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.forge;

import com.elfmcys.ysm.platform.event.YsmEvents;
import com.elfmcys.ysm.platform.event.forge.FMLClientSetupEvent;
import com.elfmcys.ysm.platform.event.forge.FMLLoadCompleteEvent;
import com.elfmcys.ysm.platform.event.forge.RegisterKeyMappingsEvent;
import com.elfmcys.ysm.platform.event.forge.TickEvent;
import net.minecraft.client.Minecraft;

/**
 * Forge client glue (the counterpart of FabricClientEventHooks and NeoForgeClientEventHooks). Entity join/leave on
 * client levels comes from the common hooks, as Forge fires those events for both sides.
 */
public final class ForgeClientEventHooks {
    private ForgeClientEventHooks() {}

    public static void register() {
        com.elfmcys.ysm.client.gui.pip.YsmGuiModels.register();
        com.elfmcys.ysm.client.event.ClientCommandHandler.register();
        // Forge for 1.20.6, 1.21 and 1.21.6 / 1.21.7: a mixin draws the layers (src/legacy/gui-hud; 1.20.6 has the event but
        // its layers take a float partial tick, and vanilla Gui.render runs there)
        //#if MC >= 1.21.8 || MC < 1.21.6 && MC >= 1.21.1
        net.minecraftforge.client.event.AddGuiOverlayLayersEvent.BUS.addListener(event -> {
            var layers = event.getLayeredDraw();
            // hidden with the HUD (F1), as the Fabric HUD elements and the original Forge overlays
            com.elfmcys.ysm.client.event.ClientSetupEvent.hudLayers().forEach((id, layer) ->
                layers.add(id, (graphics, delta) -> {
                    if (!com.elfmcys.ysm.platform.McCompat.hudHidden())
                        layer.extractRenderState(graphics, delta);
                }));
        });
        //#endif
        //#if MC < 1.20.5
        //$$ // Forge 48 / 49 (Minecraft 1.20.2 - 1.20.4): ForgeGui draws the HUD (vanilla Gui.render does not run) from registered
        //$$ // overlays, as upstream YSM registered its own on 1.20.1
        //$$ com.elfmcys.ysm.forge.YsmForge.modEventBus().addListener((net.minecraftforge.client.event.RegisterGuiOverlaysEvent event) ->
        //$$     com.elfmcys.ysm.client.event.ClientSetupEvent.hudLayers().forEach((id, layer) ->
        //$$         event.registerAboveAll(id.getPath(), (gui, graphics, partialTick, width, height) -> {
        //$$             if (!com.elfmcys.ysm.platform.McCompat.hudHidden())
        //$$                 layer.render(graphics, com.elfmcys.ysm.legacy.DeltaTracker.of(partialTick));
        //$$         })));
        //#endif
        // YSM's key mappings are collected by its own event, then registered in Forge's
        YsmEvents.post(new RegisterKeyMappingsEvent());
        net.minecraftforge.client.event.RegisterKeyMappingsEvent.BUS.addListener(event -> {
            RegisterKeyMappingsEvent.COLLECTED.forEach(event::register);
        });
        net.minecraftforge.event.TickEvent.ClientTickEvent.Pre.BUS.addListener(event -> {
            YsmEvents.post(new TickEvent.ClientTickEvent(TickEvent.Phase.START));
        });
        net.minecraftforge.event.TickEvent.ClientTickEvent.Post.BUS.addListener(event -> {
            YsmEvents.post(new TickEvent.ClientTickEvent(TickEvent.Phase.END));
        });
        net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingIn.BUS.addListener(event -> {
            YsmEvents.post(new com.elfmcys.ysm.platform.event.forge.ClientPlayerNetworkEvent.LoggingIn(
                event.getPlayer(), Minecraft.getInstance().getConnection()));
        });
        net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingOut.BUS.addListener(event -> {
            YsmEvents.post(new com.elfmcys.ysm.platform.event.forge.ClientPlayerNetworkEvent.LoggingOut(
                event.getPlayer(), Minecraft.getInstance().getConnection()));
            com.elfmcys.ysm.model.resource.client.SoundAcceptance.reset();
        });
        // 3.0-dev PauseScreenMixin: skin button on the pause screen, mobile launchers only.
        net.minecraftforge.client.event.ScreenEvent.Init.Post.BUS.addListener(event -> {
            if (event.getScreen() instanceof net.minecraft.client.gui.screens.PauseScreen pause) {
                var buttons = com.elfmcys.ysm.client.gui.AndroidCompat.addYsmSkinButton(pause);
                if (buttons != null)
                    buttons.forEach(event::addListener);
            }
        });
        // YSM's client setup blocks the render thread until its startup work is done: once, after the first frame (see
        // NeoForgeClientEventHooks: not inside mod loading, not before GameRenderer.render has set the Globals uniform).
        var setupDone = new java.util.concurrent.atomic.AtomicBoolean();
        net.minecraftforge.event.TickEvent.RenderTickEvent.Post.BUS.addListener(event -> {
            if (setupDone.compareAndSet(false, true)) {
                YsmEvents.post(new FMLClientSetupEvent());
                YsmEvents.post(new FMLLoadCompleteEvent());
            }
        });
    }

    /** YSM's GUI model previews (PictureInPictureRenderer for YsmGuiModelRenderState). */
    //#if MC >= 1.21.6
    public static void registerPictureRenderer() {
        net.minecraftforge.client.event.RegisterPictureInPictureRendererEvent.BUS.addListener(event -> {
            //#if MC >= 26.2
            event.register(new com.elfmcys.ysm.client.gui.pip.YsmGuiModelRenderer());
            //#else
            //$$ event.register(new com.elfmcys.ysm.client.gui.pip.YsmGuiModelRenderer(
            //$$     Minecraft.getInstance().renderBuffers().bufferSource()));
            //#endif
        });
    }
    //#endif
}
