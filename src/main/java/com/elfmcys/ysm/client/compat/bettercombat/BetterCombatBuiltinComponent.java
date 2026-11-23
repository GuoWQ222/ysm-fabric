// Modified for the unofficial Fabric 26.2 port by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.compat.bettercombat;

import com.elfmcys.ysm.expression.host.EntityComponent;
import com.elfmcys.ysm.api.molang.MolangContextView;
import com.elfmcys.ysm.expression.annotation.BuiltinComponent;
import com.elfmcys.ysm.expression.annotation.BuiltinFunction;
import net.minecraft.client.player.AbstractClientPlayer;

/** Fabric port: declaration kept so models using it still compile; admitted only with the mod installed. */
@BuiltinComponent(value = "ctrl",
    dependencies = @com.elfmcys.ysm.api.annotation.MolangDependency(modId = "bettercombat"))
public final class BetterCombatBuiltinComponent implements EntityComponent<AbstractClientPlayer> {
    public BetterCombatBuiltinComponent(com.elfmcys.ysm.client.entity.AnimatableEntity<AbstractClientPlayer> owner) {}

    @BuiltinFunction("bcombat_attack_animation")
    public static String attackAnimation(MolangContextView<AbstractClientPlayer> context) {
        // Fabric port: the mod integration is not ported; stand-in result.
        return "";
    }
}
