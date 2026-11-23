/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.platform.event;

/**
 * Fabric port: every class that carried Forge's {@code @Mod.EventBusSubscriber}. Fabric has no annotation scan, so
 * they are listed here and registered at init (generated from the sources; update when adding subscribers).
 */
public final class Subscribers {
    private Subscribers() {}

    public static void registerCommon(IEventBus bus) {
        bus.register(com.elfmcys.ysm.event.CapabilityEvent.class);
        bus.register(com.elfmcys.ysm.event.CommandRegistry.class);
        bus.register(com.elfmcys.ysm.event.CommonEvent.class);
        bus.register(com.elfmcys.ysm.event.LivingShieldBlockEvent.class);
        bus.register(com.elfmcys.ysm.event.LoggedOutEvent.class);
        bus.register(com.elfmcys.ysm.event.LoginEvent.class);
        bus.register(com.elfmcys.ysm.event.MobEffectSyncEvent.class);
        bus.register(com.elfmcys.ysm.event.ServerStartingEvent.class);
    }

    public static void registerClient(IEventBus bus) {
        bus.register(com.elfmcys.ysm.client.event.ClientLoggedEvent.class);
        bus.register(com.elfmcys.ysm.client.event.ClientSetupEvent.class);
        bus.register(com.elfmcys.ysm.client.event.ClientTickEvent.class);
        bus.register(com.elfmcys.ysm.client.event.EntityLoadEvent.class);
        bus.register(com.elfmcys.ysm.client.event.LocalPlayerRespawnEvent.class);
        bus.register(com.elfmcys.ysm.client.event.ModInputEvent.class);
        bus.register(com.elfmcys.ysm.client.event.PlayerMoveEvent.class);
        bus.register(com.elfmcys.ysm.client.event.VanillaPlayerRenderEvent.class);
        bus.register(com.elfmcys.ysm.client.gui.ModelScreen.class);
        bus.register(com.elfmcys.ysm.client.input.AnimationRouletteKey.class);
        bus.register(com.elfmcys.ysm.client.input.DebugAnimationKey.class);
        bus.register(com.elfmcys.ysm.client.input.ExtraAnimationKey.class);
        bus.register(com.elfmcys.ysm.client.input.ExtraPlayerConfigKey.class);
        bus.register(com.elfmcys.ysm.client.input.PlayerModelScreenKey.class);
    }
}
