// Modified for the unofficial Fabric 26.2 port by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.compat.parcool;

import org.apache.commons.lang3.tuple.Pair;
import com.elfmcys.ysm.client.entity.CustomPlayerEntity;
import com.elfmcys.ysm.config.ClientConfig;
import com.elfmcys.ysm.geckolib3.core.controller.HybridAnimationController;
import com.elfmcys.ysm.geckolib3.core.controller.IAnimationController;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import java.util.Optional;
import java.util.function.BiFunction;

/**
 * Fabric port stand-in: the ParCoolCompat integration targets a Forge mod and is not ported; every method reports
 * "not installed". The original is kept in src/forge-compat-reference.
 */
@SuppressWarnings("unused")
public final class ParCoolCompat {

    private ParCoolCompat() {}

    public static void init() {
        
    }

    public static Optional<Pair<String, String>> getCompatibilityWarning() {
        return Optional.empty();
    }

    public static boolean isInstalled() {
        return false;
    }

    public static boolean hasAnimation(net.minecraft.world.entity.Entity entity) {
        return false;
    }

    public static String animationName(net.minecraft.world.entity.Entity entity) {
        return null;
    }

    public static Optional<BiFunction<String, CustomPlayerEntity, IAnimationController<CustomPlayerEntity>>> animationPredicate() {
        return Optional.empty();
    }

    public static boolean hasAnimation(Player player) {
        return false;
    }

    public static String getAnimation(Player player) {
        return null;
    }
}
