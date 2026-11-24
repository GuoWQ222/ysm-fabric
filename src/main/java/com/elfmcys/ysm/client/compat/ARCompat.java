// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.compat;



/**
 * Fabric port stand-in: the ARCompat integration targets a Forge mod and is not ported; every method reports
 * "not installed". The original is kept in src/forge-compat-reference.
 */
@SuppressWarnings("unused")
public final class ARCompat {

    private ARCompat() {}

    public static void init() {
        
    }

    public static boolean isInstalled() {
        return false;
    }
}
