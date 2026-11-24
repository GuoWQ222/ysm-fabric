// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.compat.tacz;

import com.elfmcys.ysm.expression.host.EntityComponent;
import com.elfmcys.ysm.api.molang.MolangContextView;
import com.elfmcys.ysm.expression.annotation.BuiltinComponent;
import com.elfmcys.ysm.expression.annotation.BuiltinFunction;
import net.minecraft.world.entity.LivingEntity;

/** Fabric port: declaration kept so models using it still compile; admitted only with the mod installed. */
@BuiltinComponent(value = "ctrl", dependencies = @com.elfmcys.ysm.api.annotation.MolangDependency(modId = "tacz"))
public final class TaczBuiltinComponent implements EntityComponent<LivingEntity> {
    public TaczBuiltinComponent(com.elfmcys.ysm.client.entity.AnimatableEntity<LivingEntity> owner) {}

    @BuiltinFunction("tac_hold_gun")
    public static boolean holdsGun(MolangContextView<LivingEntity> context) {
        // Fabric port: the mod integration is not ported; stand-in result.
        return false;
    }

    @BuiltinFunction("tac_gun_type")
    public static String gunType(MolangContextView<LivingEntity> context) {
        // Fabric port: the mod integration is not ported; stand-in result.
        return "";
    }

    @BuiltinFunction("tac_gun_id")
    public static String gunId(MolangContextView<LivingEntity> context) {
        // Fabric port: the mod integration is not ported; stand-in result.
        return "";
    }

    @BuiltinFunction("tac_is_fire")
    public static boolean isFire(MolangContextView<LivingEntity> context) {
        // Fabric port: the mod integration is not ported; stand-in result.
        return false;
    }

    @BuiltinFunction("tac_is_aim")
    public static boolean isAim(MolangContextView<LivingEntity> context) {
        // Fabric port: the mod integration is not ported; stand-in result.
        return false;
    }

    @BuiltinFunction("tac_is_reload")
    public static boolean isReload(MolangContextView<LivingEntity> context) {
        // Fabric port: the mod integration is not ported; stand-in result.
        return false;
    }

    @BuiltinFunction("tac_is_melee")
    public static boolean isMelee(MolangContextView<LivingEntity> context) {
        // Fabric port: the mod integration is not ported; stand-in result.
        return false;
    }

    @BuiltinFunction("tac_is_draw")
    public static boolean isDraw(MolangContextView<LivingEntity> context) {
        // Fabric port: the mod integration is not ported; stand-in result.
        return false;
    }

    @BuiltinFunction("tac_fire_mode")
    public static String fireMode(MolangContextView<LivingEntity> context) {
        // Fabric port: the mod integration is not ported; stand-in result.
        return "";
    }
}
