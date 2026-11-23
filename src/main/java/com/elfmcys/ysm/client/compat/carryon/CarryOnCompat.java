// Modified for the unofficial Fabric 26.2 port by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.compat.carryon;

import com.elfmcys.ysm.client.entity.CustomPlayerEntity;
import com.elfmcys.ysm.geckolib3.core.controller.HybridAnimationController;
import com.elfmcys.ysm.geckolib3.core.controller.IAnimationController;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import java.util.Optional;
import java.util.function.BiFunction;

/**
 * Fabric port stand-in: the CarryOnCompat integration targets a Forge mod and is not ported; every method reports
 * "not installed". The original is kept in src/forge-compat-reference.
 */
@SuppressWarnings("unused")
public final class CarryOnCompat {

    private CarryOnCompat() {}

    public static void init() {
        
    }

    public static boolean isInstalled() {
        return false;
    }

    public static Optional<BiFunction<String, CustomPlayerEntity, IAnimationController<CustomPlayerEntity>>> animationPredicate() {
        return Optional.empty();
    }

    public static boolean isCarryOnPrincess(Player player) {
        return false;
    }

    public static boolean isCarryOnPrincess(LivingEntity entity) {
        return false;
    }

    public static boolean carryOnPrincess(net.minecraft.world.entity.Entity entity) {
        return false;
    }
}
