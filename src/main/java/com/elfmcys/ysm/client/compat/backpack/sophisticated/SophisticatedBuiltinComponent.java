// Modified for the unofficial Fabric 26.2 port by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.compat.backpack.sophisticated;

import com.elfmcys.ysm.expression.host.EntityComponent;
import com.elfmcys.ysm.api.molang.MolangContextView;
import com.elfmcys.ysm.expression.annotation.BuiltinComponent;
import com.elfmcys.ysm.expression.annotation.BuiltinFunction;
import net.minecraft.world.entity.LivingEntity;

/** Fabric port: declaration kept so models using it still compile; admitted only with the mod installed. */
@BuiltinComponent(value = "ctrl",
    dependencies = @com.elfmcys.ysm.api.annotation.MolangDependency(modId = "sophisticatedbackpacks"))
public final class SophisticatedBuiltinComponent implements EntityComponent<LivingEntity> {
    public SophisticatedBuiltinComponent(com.elfmcys.ysm.client.entity.AnimatableEntity<LivingEntity> owner) {}

    @BuiltinFunction("has_sophisticated_backpack")
    public static boolean hasBackpack(MolangContextView<LivingEntity> context) {
        // Fabric port: the mod integration is not ported; stand-in result.
        return false;
    }
}
