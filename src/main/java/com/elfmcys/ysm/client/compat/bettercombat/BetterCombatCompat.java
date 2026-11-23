// Modified for the unofficial Fabric 26.2 port by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.compat.bettercombat;

import net.minecraft.client.player.AbstractClientPlayer;

/**
 * Fabric port stand-in: the BetterCombatCompat integration targets a Forge mod and is not ported; every method reports
 * "not installed". The original is kept in src/forge-compat-reference.
 */
@SuppressWarnings("unused")
public final class BetterCombatCompat {

    private BetterCombatCompat() {}

    public static void init() {
        
    }

    public static boolean isInstalled() {
        return false;
    }

    public static String getAttackAnimation(AbstractClientPlayer player) {
        return null;
    }

    public static String attackAnimation(net.minecraft.world.entity.Entity entity) {
        return null;
    }
}
