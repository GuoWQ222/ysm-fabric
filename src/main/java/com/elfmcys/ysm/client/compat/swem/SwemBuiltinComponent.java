// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.compat.swem;

import com.elfmcys.ysm.api.molang.MolangContextView;
import com.elfmcys.ysm.expression.annotation.BuiltinComponent;
import com.elfmcys.ysm.expression.annotation.BuiltinFunction;
import com.elfmcys.ysm.expression.host.EntityComponent;
import net.minecraft.world.entity.LivingEntity;

/** Fabric port: declaration kept so models using it still compile; admitted only with the mod installed. */
@BuiltinComponent(value = "ctrl", dependencies = @com.elfmcys.ysm.api.annotation.MolangDependency(modId = "swem"))
public final class SwemBuiltinComponent implements EntityComponent<LivingEntity> {
    public SwemBuiltinComponent(com.elfmcys.ysm.client.entity.AnimatableEntity<LivingEntity> owner) {}

    @BuiltinFunction("swem_is_ride")
    public static boolean isRiding(MolangContextView<LivingEntity> context) {
        // Fabric port: the mod integration is not ported; stand-in result.
        return false;
    }

    @BuiltinFunction("swem_state")
    public static String state(MolangContextView<LivingEntity> context) {
        // Fabric port: the mod integration is not ported; stand-in result.
        return "";
    }
}
