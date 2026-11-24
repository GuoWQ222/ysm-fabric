// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.compat.realcamera;


/**
 * Real Camera (Fabric 26.2, mod id {@code realcamera}). As upstream's Forge integration: while Real Camera binds the
 * camera to the body, YSM keeps drawing the full model in first person. API calls live in {@link Inner}.
 */
public final class RealCameraCompat {
    private static final String MOD_ID = "realcamera";
    private static boolean installed;

    private RealCameraCompat() {}

    public static void init() {
        installed = com.elfmcys.ysm.platform.Platform.isModLoaded(MOD_ID);
    }

    public static boolean isActive() {
        return installed && Inner.isActive();
    }

    private static final class Inner {
        static boolean isActive() {
            return com.xtracr.realcamera.RealCameraCore.isActive();
        }
    }
}
