/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.client.command;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;

/**
 * Feedback to the player running a client command. The client command source is Fabric's FabricClientCommandSource on
 * Fabric and vanilla's CommandSourceStack on NeoForge/Forge (versions/loader-*.remap renames the type there).
 */
public final class ClientFeedback {
    private ClientFeedback() {}

    public static void success(FabricClientCommandSource source, Component message) {
        //#if LOADER == fabric
        source.sendFeedback(message);
        //#else
        //$$ source.sendSystemMessage(message);
        //#endif
    }

    public static void error(FabricClientCommandSource source, Component message) {
        //#if LOADER == fabric
        source.sendError(message);
        //#else
        //$$ source.sendFailure(message);
        //#endif
    }
}
