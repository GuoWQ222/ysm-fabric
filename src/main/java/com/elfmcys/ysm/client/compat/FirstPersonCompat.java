// Modified for the unofficial Fabric 26.2 port by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.compat;

import net.minecraft.world.phys.Vec3;

/**
 * Fabric port stand-in: the FirstPersonCompat integration targets a Forge mod and is not ported; every method reports
 * "not installed". The original is kept in src/forge-compat-reference.
 */
@SuppressWarnings("unused")
public final class FirstPersonCompat {

    private FirstPersonCompat() {}

    public static void init() {
        
    }

    public static boolean isInstalled() {
        return false;
    }

    public static boolean isRenderingPlayer() {
        return false;
    }

    public static boolean shouldHideHead() {
        return false;
    }

    public static void setHeadPos(float headPos) {
        
    }

    public static boolean isEnabled() {
        return false;
    }
}
