// Modified for the unofficial Fabric 26.2 port by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.compat.ironsspellbooks;

import com.elfmcys.ysm.api.molang.MolangContextView;
import com.elfmcys.ysm.expression.annotation.BuiltinComponent;
import com.elfmcys.ysm.expression.annotation.BuiltinFunction;
import com.elfmcys.ysm.expression.host.EntityComponent;
import net.minecraft.client.player.AbstractClientPlayer;

/** Fabric port: declaration kept so models using it still compile; admitted only with the mod installed. */
@BuiltinComponent(value = "ctrl",
    dependencies = @com.elfmcys.ysm.api.annotation.MolangDependency(modId = "irons_spellbooks"))
public final class IronsSpellBooksBuiltinComponent implements EntityComponent<AbstractClientPlayer> {
    public IronsSpellBooksBuiltinComponent(
        com.elfmcys.ysm.client.entity.AnimatableEntity<AbstractClientPlayer> owner) {}

    @BuiltinFunction("iss_animation")
    public static String animation(MolangContextView<AbstractClientPlayer> context) {
        // Fabric port: the mod integration is not ported; stand-in result.
        return "";
    }
}
