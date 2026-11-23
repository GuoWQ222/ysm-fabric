// Modified for the unofficial Fabric 26.2 port by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.command.sub;

import com.elfmcys.ysm.command.ModelSelectorArgument;
import com.elfmcys.ysm.model.catalog.source.ModelSelector;
import com.elfmcys.ysm.model.service.ClientModelService;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.minecraft.network.chat.Component;

public final class RoamingCommand {
    private RoamingCommand() {}

    public static LiteralArgumentBuilder<FabricClientCommandSource> get() {
        return ClientCommands.literal("roaming").then(ClientCommands.literal("clear")
            .then(ClientCommands.argument("model", ModelSelectorArgument.selector()).suggests((context, builder) -> {
                for (var value : ModelSelector
                    .suggestions(ClientModelService.instance().catalog().catalog().byLocation()))
                    builder.suggest(value.indexOf(' ') >= 0
                        ? '"' + value.replace("\\", "\\\\").replace("\"", "\\\"") + '"' : value);
                return builder.buildFuture();
            }).executes(context -> {
                var models = ClientModelService.instance();
                var result = ModelSelector.resolveHistorical(context.getArgument("model", String.class),
                    models.catalog().catalog().byLocation());
                if (result.found().isEmpty()) {
                    context.getSource().sendError(Component.literal(result.message()));
                    return 0;
                }
                models.preferences().clear(result.found().orElseThrow());
                return 1;
            })));
    }
}
