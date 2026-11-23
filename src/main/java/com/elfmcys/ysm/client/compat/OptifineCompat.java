// Modified for the unofficial Fabric 26.2 port by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.compat;



/**
 * Fabric port stand-in: the OptifineCompat integration targets a Forge mod and is not ported; every method reports
 * "not installed". The original is kept in src/forge-compat-reference.
 */
@SuppressWarnings("unused")
public final class OptifineCompat {

    private OptifineCompat() {}

    public static void init() {
        
    }

    public static boolean isInstalled() {
        return false;
    }
}
