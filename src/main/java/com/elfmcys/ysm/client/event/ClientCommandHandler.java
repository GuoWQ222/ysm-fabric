// Modified for the unofficial Fabric 26.2 port by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.event;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.client.command.ClientRootCommand;
import com.elfmcys.ysm.client.command.ResourceDebugCommand;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;

/** Client-only commands. Fabric port: registered through Fabric's client command API. */
public final class ClientCommandHandler {
    private ClientCommandHandler() {}

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, buildContext) -> {
            ResourceDebugCommand.register(dispatcher);
            if (YesSteveModel.isAvailable())
                ClientRootCommand.register(dispatcher);
        });
    }
}
