// Modified for the unofficial Fabric 26.2 port by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.compat.realcamera;



/**
 * Fabric port stand-in: the RealCameraCompat integration targets a Forge mod and is not ported; every method reports
 * "not installed". The original is kept in src/forge-compat-reference.
 */
@SuppressWarnings("unused")
public final class RealCameraCompat {

    private RealCameraCompat() {}

    public static void init() {
        
    }

    public static boolean isActive() {
        return false;
    }
}
