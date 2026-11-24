// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.event;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.model.service.ServerModelService;
import com.elfmcys.ysm.platform.event.forge.ServerAboutToStartEvent;
import com.elfmcys.ysm.platform.event.forge.ServerStoppingEvent;
import com.elfmcys.ysm.platform.event.SubscribeEvent;
import com.elfmcys.ysm.platform.event.Mod;

@Mod.EventBusSubscriber
public class ServerStartingEvent {
    @SubscribeEvent
    public static void onServerInit(final ServerAboutToStartEvent event) {
        if (!YesSteveModel.isAvailable()) {
            return;
        }
        ServerModelService.start(event.getServer());
    }

    @SubscribeEvent
    public static void onServerStopping(final ServerStoppingEvent event) {
        ServerModelService.current().ifPresent(ServerModelService::close);
    }
}
