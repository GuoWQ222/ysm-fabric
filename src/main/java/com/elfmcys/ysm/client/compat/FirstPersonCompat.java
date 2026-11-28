// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.compat;


/**
 * First-person Model (tr7zw, Fabric 26.2: mod id {@code firstperson}). As upstream's Forge integration: the camera
 * offset follows the YSM model's head height, and YSM knows when First-person Model is drawing the local body.
 * API calls live in {@link Inner}, which is loaded only when the mod is installed.
 */
public final class FirstPersonCompat {
    private static final String MOD_ID = "firstperson";
    private static final String LEGACY_MOD_ID = "firstpersonmod";
    private static volatile float headPos;
    private static boolean installed;

    private FirstPersonCompat() {}

    public static void init() {
        installed = com.elfmcys.ysm.platform.Platform.isModLoaded(MOD_ID) || com.elfmcys.ysm.platform.Platform.isModLoaded(LEGACY_MOD_ID);
        if (installed) {
            Inner.registerOffset();
            setHeadPos(24);
        }
    }

    public static boolean isInstalled() {
        return installed;
    }

    public static boolean isRenderingPlayer() {
        return installed && (submittingExtracted || Inner.isRenderingPlayer());
    }

    /** Render thread: YSM is submitting a state extracted while First-person Model drew the local player. */
    private static boolean submittingExtracted;

    /**
     * Runs a player's submit as First-person Model drawing when its state was extracted that way: its 1.21.9 / 1.21.10
     * builds raise their flag only during extraction, and YSM poses the model (hides the head, places the camera) at
     * submit.
     */
    public static <T> T submitExtracted(boolean extractedWhileDrawing, java.util.function.Supplier<T> submit) {
        if (!extractedWhileDrawing || submittingExtracted)
            return submit.get();
        submittingExtracted = true;
        try {
            return submit.get();
        } finally {
            submittingExtracted = false;
        }
    }

    public static boolean shouldHideHead() {
        return isRenderingPlayer();
    }

    /** Head height of the current YSM model in pixels (16 per block). */
    public static void setHeadPos(float pixels) {
        headPos = pixels / 16f;
    }

    public static boolean isEnabled() {
        return installed && Inner.isEnabled();
    }

    private static final class Inner {
        static void registerOffset() {
            dev.tr7zw.firstperson.api.FirstPersonAPI.registerPlayerHandler(
                (dev.tr7zw.firstperson.api.PlayerOffsetHandler)(player, delta, original, current) ->
                    new net.minecraft.world.phys.Vec3(current.x(), 1.5f - headPos, current.z()));
        }

        static boolean isRenderingPlayer() {
            return dev.tr7zw.firstperson.api.FirstPersonAPI.isRenderingPlayer();
        }

        static boolean isEnabled() {
            return dev.tr7zw.firstperson.api.FirstPersonAPI.isEnabled();
        }
    }
}
