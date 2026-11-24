// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.compat.create;

import com.elfmcys.ysm.api.molang.MolangContextView;
import com.elfmcys.ysm.expression.annotation.BuiltinComponent;
import com.elfmcys.ysm.expression.annotation.BuiltinFunction;
import com.elfmcys.ysm.expression.host.EntityComponent;
import net.minecraft.world.entity.player.Player;

/** Fabric port: declaration kept so models using it still compile; admitted only with the mod installed. */
@BuiltinComponent(value = "ctrl", dependencies = @com.elfmcys.ysm.api.annotation.MolangDependency(modId = "create"))
public final class CreateBuiltinComponent implements EntityComponent<Player> {
    public CreateBuiltinComponent(com.elfmcys.ysm.client.entity.AnimatableEntity<Player> owner) {}

    @BuiltinFunction("create_hanging_skyhook")
    public static boolean hangingSkyhook(MolangContextView<Player> context) {
        // Fabric port: the mod integration is not ported; stand-in result.
        return false;
    }
}
