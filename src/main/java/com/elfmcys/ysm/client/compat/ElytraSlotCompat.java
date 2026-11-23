// Modified for the unofficial Fabric 26.2 port by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.compat;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Fabric port stand-in: the ElytraSlotCompat integration targets a Forge mod and is not ported; every method reports
 * "not installed". The original is kept in src/forge-compat-reference.
 */
@SuppressWarnings("unused")
public final class ElytraSlotCompat {

    private ElytraSlotCompat() {}

    public static void init() {
        
    }

    public static boolean isInstalled() {
        return false;
    }

    public static ItemStack getEquippedElytraItem(LivingEntity entity) {
        return null;
    }
}
