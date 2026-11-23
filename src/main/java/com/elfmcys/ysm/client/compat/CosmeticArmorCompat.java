// Modified for the unofficial Fabric 26.2 port by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.compat;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import java.util.Optional;

/**
 * Fabric port stand-in: the CosmeticArmorCompat integration targets a Forge mod and is not ported; every method reports
 * "not installed". The original is kept in src/forge-compat-reference.
 */
@SuppressWarnings("unused")
public final class CosmeticArmorCompat {

    private CosmeticArmorCompat() {}

    public static void init() {
        
    }

    public static boolean isInstalled() {
        return false;
    }

    public static Optional<ItemStack> getSkinArmorItem(Player player, EquipmentSlot slot) {
        return Optional.empty();
    }
}
