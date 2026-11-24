// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.command;

import com.elfmcys.ysm.YesSteveModel;
//#if LOADER == fabric
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
//#endif
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
        //#if LOADER == fabric
        ArgumentTypeRegistry.registerArgumentType(Identifier.fromNamespaceAndPath(YesSteveModel.MOD_ID, "model_selector"),
            ModelSelectorArgument.class, SingletonArgumentInfo.contextFree(ModelSelectorArgument::selector));
        //#elseif LOADER == neoforge
        //$$ var info = SingletonArgumentInfo.contextFree(ModelSelectorArgument::selector);
        //$$ com.elfmcys.ysm.neoforge.YsmNeoForge.modBus().addListener((net.neoforged.neoforge.registries.RegisterEvent event) ->
        //$$     event.register(net.minecraft.core.registries.Registries.COMMAND_ARGUMENT_TYPE,
        //$$         Identifier.fromNamespaceAndPath(YesSteveModel.MOD_ID, "model_selector"),
        //$$         () -> net.minecraft.commands.synchronization.ArgumentTypeInfos.registerByClass(ModelSelectorArgument.class, info)));
        //#else
        //$$ com.elfmcys.ysm.forge.YsmForge.registerArgument("model_selector", ModelSelectorArgument.class,
        //$$     SingletonArgumentInfo.contextFree(ModelSelectorArgument::selector));
        //#endif
    }
}
