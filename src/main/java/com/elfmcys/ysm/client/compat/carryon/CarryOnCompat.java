// Modified for the unofficial Fabric 26.2 port by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.compat.carryon;

import com.elfmcys.ysm.client.entity.CustomPlayerEntity;
import com.elfmcys.ysm.geckolib3.core.controller.HybridAnimationController;
import com.elfmcys.ysm.geckolib3.core.controller.IAnimationController;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;
import java.util.function.BiFunction;

/**
 * Carry On (Fabric 26.2, mod id {@code carryon}). As upstream's Forge integration: while a player carries a block,
 * an entity or another player, the model plays {@code carryon:block}, {@code carryon:entity} or
 * {@code carryon:player}; a carried player counts as the "princess" passenger. Carry On classes are only touched by
 * {@link CarryOnInnerCompat} and {@link CarryOnPredicate}, which load only when the mod is installed.
 */
public final class CarryOnCompat {
    private static final String CARRY_ON_ID = "carryon";
    private static boolean installed;

    private CarryOnCompat() {}

    public static void init() {
        installed = FabricLoader.getInstance().isModLoaded(CARRY_ON_ID);
    }

    public static boolean isInstalled() {
        return installed;
    }

    public static Optional<BiFunction<String, CustomPlayerEntity, IAnimationController<CustomPlayerEntity>>>
        animationPredicate() {
        if (!installed)
            return Optional.empty();
        return Optional.of((name, entity) -> new HybridAnimationController<>(entity, name, 0.1f, new CarryOnPredicate()));
    }

    public static boolean isCarryOnPrincess(Player player) {
        return isCarryOnPrincess((LivingEntity)player);
    }

    public static boolean isCarryOnPrincess(LivingEntity entity) {
        return installed && CarryOnInnerCompat.isCarryOnPrincess(entity);
    }

    /** Capability-facing form: false for non-living entities or without the mod. */
    public static boolean carryOnPrincess(net.minecraft.world.entity.Entity entity) {
        return entity instanceof LivingEntity living && isCarryOnPrincess(living);
    }
}
