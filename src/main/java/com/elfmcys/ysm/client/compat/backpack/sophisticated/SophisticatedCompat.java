// Modified for the unofficial Fabric 26.2 port by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.compat.backpack.sophisticated;

import org.apache.commons.lang3.tuple.Pair;
import com.elfmcys.ysm.client.event.RegisterEntityRenderersEvent;
import com.elfmcys.ysm.config.ClientConfig;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import java.util.Optional;

/**
 * Fabric port stand-in: the SophisticatedCompat integration targets a Forge mod and is not ported; every method reports
 * "not installed". The original is kept in src/forge-compat-reference.
 */
@SuppressWarnings("unused")
public final class SophisticatedCompat {

    private SophisticatedCompat() {}

    public static void init() {
        
    }

    public static Optional<Pair<String, String>> getCompatibilityWarning() {
        return Optional.empty();
    }

    public static void addLayer() {
        
    }

    public static boolean isInstalled() {
        return false;
    }

    public static boolean hasBackpack(net.minecraft.world.entity.Entity entity) {
        return false;
    }

    public static ItemStack getBackpackItemStack(LivingEntity livingEntity) {
        return null;
    }
}
