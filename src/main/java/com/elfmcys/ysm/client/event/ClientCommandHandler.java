// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.event;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.client.command.ClientRootCommand;
import com.elfmcys.ysm.client.command.ResourceDebugCommand;
//#if LOADER == fabric
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
//#endif

/**
 * Client-only commands (/ysmclient, resource diagnostics). Registered through each loader's client command hook;
 * on NeoForge/Forge the command classes are typed against vanilla's CommandSourceStack (versions/loader-*.remap).
 */
public final class ClientCommandHandler {
    private ClientCommandHandler() {}

    public static void register() {
        //#if LOADER == fabric
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, buildContext) -> {
            ResourceDebugCommand.register(dispatcher);
            if (YesSteveModel.isAvailable())
                ClientRootCommand.register(dispatcher);
        });
        //#elseif LOADER == neoforge
        //$$ net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
        //$$     (net.neoforged.neoforge.client.event.RegisterClientCommandsEvent event) -> {
        //$$         ResourceDebugCommand.register(event.getDispatcher());
        //$$         if (YesSteveModel.isAvailable())
        //$$             ClientRootCommand.register(event.getDispatcher());
        //$$     });
        //#else
        //$$ net.minecraftforge.client.event.RegisterClientCommandsEvent.BUS.addListener(event -> {
        //$$     ResourceDebugCommand.register(event.getDispatcher());
        //$$     if (YesSteveModel.isAvailable())
        //$$         ClientRootCommand.register(event.getDispatcher());
        //$$ });
        //#endif
    }
}
