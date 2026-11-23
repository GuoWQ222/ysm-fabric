// Modified for the unofficial Fabric 26.2 port by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.compat.create;

import net.minecraft.world.entity.player.Player;

/**
 * Fabric port stand-in: the CreateCompat integration targets a Forge mod and is not ported; every method reports
 * "not installed". The original is kept in src/forge-compat-reference.
 */
@SuppressWarnings("unused")
public final class CreateCompat {

    private CreateCompat() {}

    public static void init() {
        
    }

    public static boolean isInstalled() {
        return false;
    }

    public static boolean isHangingSkyhook(Player player) {
        return false;
    }
}
