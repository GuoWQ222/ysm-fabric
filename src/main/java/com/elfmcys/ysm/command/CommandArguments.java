// Modified for the unofficial Fabric 26.2 port by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.command;

import com.elfmcys.ysm.YesSteveModel;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.resources.Identifier;

/** Fabric port: registers YSM's command argument types through Fabric's command API. */
public final class CommandArguments {
    private static boolean registered;

    private CommandArguments() {}

    public static void register() {
        if (registered)
            return;
        registered = true;
        ArgumentTypeRegistry.registerArgumentType(Identifier.fromNamespaceAndPath(YesSteveModel.MOD_ID, "model_selector"),
            ModelSelectorArgument.class, SingletonArgumentInfo.contextFree(ModelSelectorArgument::selector));
    }
}
