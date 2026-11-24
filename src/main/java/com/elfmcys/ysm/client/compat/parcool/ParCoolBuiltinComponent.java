// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.compat.parcool;

import com.elfmcys.ysm.expression.host.EntityComponent;
import com.elfmcys.ysm.api.molang.MolangContextView;
import com.elfmcys.ysm.expression.annotation.BuiltinComponent;
import com.elfmcys.ysm.expression.annotation.BuiltinFunction;
import net.minecraft.world.entity.player.Player;

/** Fabric port: declaration kept so models using it still compile; admitted only with the mod installed. */
@BuiltinComponent(value = "ctrl", dependencies = @com.elfmcys.ysm.api.annotation.MolangDependency(modId = "parcool"))
public final class ParCoolBuiltinComponent implements EntityComponent<Player> {
    public ParCoolBuiltinComponent(com.elfmcys.ysm.client.entity.AnimatableEntity<Player> owner) {}

    @BuiltinFunction("parcool_state")
    public static String state(MolangContextView<Player> context) {
        // Fabric port: the mod integration is not ported; stand-in result.
        return "";
    }
}
