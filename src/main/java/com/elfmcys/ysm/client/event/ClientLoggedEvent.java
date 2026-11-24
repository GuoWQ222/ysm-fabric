// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.event;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.network.forge.ClientSessionRuntime;
import net.minecraft.network.Connection;
import com.elfmcys.ysm.platform.Dist;
import com.elfmcys.ysm.platform.event.forge.ClientPlayerNetworkEvent;
import com.elfmcys.ysm.platform.event.SubscribeEvent;
import com.elfmcys.ysm.platform.event.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class ClientLoggedEvent {
    private static Connection loggedIn;

    @SubscribeEvent
    public static void onPlayerLoggedIn(ClientPlayerNetworkEvent.LoggingIn event) {
        var connection = event.getConnection();
        if (loggedIn == connection) {
            return;
        }
        if (!YesSteveModel.isAvailable()) {
            loggedIn = connection;
            YesSteveModel.sendUnavailableMessage();
            return;
        }
        ClientSessionRuntime.beginConnection(connection);
        loggedIn = connection;
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        var connection = event.getConnection();
        if (loggedIn != connection) {
            return;
        }
        loggedIn = null;
        RegisterEntityRenderersEvent.releaseFirstPersonArm();
        if (YesSteveModel.isAvailable()) {
            ClientSessionRuntime.disconnect(connection);
        }
    }
}
