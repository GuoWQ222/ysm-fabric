// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.command.sub;

import com.elfmcys.ysm.command.sub.CacheCommand;
import com.elfmcys.ysm.model.ModelRuntime;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.Minecraft;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;

/** Uses the local-player boundary of ClientRootCommand, independently of server permissions. */
public final class ClientCacheCommand {
    private ClientCacheCommand() {}

    public static LiteralArgumentBuilder<FabricClientCommandSource> get() {
        return CacheCommand.<FabricClientCommandSource>tree("ysmclient", com.elfmcys.ysm.client.command.ClientFeedback::success, (source, all) -> {
            var minecraft = Minecraft.getInstance();
            var player = minecraft.player;
            if (player == null)
                return 0;
            java.util.function.Consumer<Component> feedback = message -> {
                if (minecraft.player == player)
                    com.elfmcys.ysm.platform.McCompat.message(player, message);
            };
            try {
                return CacheCommand.schedule(ModelRuntime.system().storage(), all, minecraft::execute, feedback,
                    feedback);
            } catch (RuntimeException failure) {
                feedback.accept(Component.translatable("commands.ysm.cache.failure", "cache",
                    com.elfmcys.ysm.model.domain.PublicFailure.summary(failure)));
                return 0;
            }
        });
    }
}
