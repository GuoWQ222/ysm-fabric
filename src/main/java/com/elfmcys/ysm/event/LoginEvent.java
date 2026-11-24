// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.event;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.network.forge.SessionProtocolHandler;
import net.minecraft.server.level.ServerPlayer;
import com.elfmcys.ysm.platform.event.forge.PlayerEvent;
import com.elfmcys.ysm.platform.event.SubscribeEvent;
import com.elfmcys.ysm.platform.event.Mod;

@Mod.EventBusSubscriber
public final class LoginEvent {
    @SubscribeEvent
    public static void onLoggedInServer(PlayerEvent.PlayerLoggedInEvent event) {
        if (!YesSteveModel.isAvailable()) {
            return;
        }
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            SessionProtocolHandler.sendServerHello(serverPlayer);
        }
    }
}
