// Modified for the unofficial Fabric 26.2 port by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.compat.carryon;

import com.elfmcys.ysm.expression.host.EntityComponent;
import com.elfmcys.ysm.api.molang.MolangContextView;
import com.elfmcys.ysm.expression.annotation.BuiltinComponent;
import com.elfmcys.ysm.expression.annotation.BuiltinFunction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import java.util.Locale;

/** Fabric port: declaration kept so models using it still compile; admitted only with the mod installed. */
@BuiltinComponent(value = "ctrl", dependencies = @com.elfmcys.ysm.api.annotation.MolangDependency(modId = "carryon"))
public final class CarryOnBuiltinComponent implements EntityComponent<LivingEntity> {
    public CarryOnBuiltinComponent(com.elfmcys.ysm.client.entity.AnimatableEntity<LivingEntity> owner) {}

    @BuiltinFunction("carryon_is_princess")
    public static boolean isPrincess(MolangContextView<LivingEntity> context) {
        // Fabric port: the mod integration is not ported; stand-in result.
        return false;
    }

    @BuiltinFunction("carryon_type")
    public static String type(MolangContextView<LivingEntity> context) {
        // Fabric port: the mod integration is not ported; stand-in result.
        return "";
    }
}
