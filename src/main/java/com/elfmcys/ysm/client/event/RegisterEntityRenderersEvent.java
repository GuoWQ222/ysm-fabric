// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.event;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.client.compat.backpack.sophisticated.SophisticatedCompat;
import com.elfmcys.ysm.client.renderer.CustomFirstPersonArmRenderer;
import com.elfmcys.ysm.client.renderer.CustomPlayerRenderer;
import com.elfmcys.ysm.client.renderer.CustomProjectileRenderer;
import com.elfmcys.ysm.client.renderer.CustomVehicleRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/**
 * Holds YSM's renderers. Fabric port: built from the vanilla renderer context whenever the entity render dispatcher
 * reloads (EntityRenderDispatcherReloadMixin), so baked models and equipment assets are current.
 */
public final class RegisterEntityRenderersEvent {
    private static CustomPlayerRenderer CUSTOM_PLAYER_RENDERER;
    private static CustomProjectileRenderer CUSTOM_PROJECTILE_RENDERER;
    private static CustomFirstPersonArmRenderer CUSTOM_FIRST_PERSON_RENDERER;
    private static CustomVehicleRenderer CUSTOM_VEHICLE_RENDERER;
    //#if MC >= 26.1
    private static net.minecraft.client.renderer.block.BlockModelResolver BLOCK_MODEL_RESOLVER;
    //#endif

    private RegisterEntityRenderersEvent() {}

    /** Render thread, after the vanilla dispatcher rebuilt its renderers. */
    public static void init(EntityRendererProvider.Context context) {
        if (!YesSteveModel.isAvailable())
            return;
        //#if MC >= 26.1
        BLOCK_MODEL_RESOLVER = context.getBlockModelResolver();
        //#endif
        CUSTOM_PLAYER_RENDERER = new CustomPlayerRenderer(context);
        CUSTOM_PROJECTILE_RENDERER = new CustomProjectileRenderer();
        releaseFirstPersonArm();
        CUSTOM_FIRST_PERSON_RENDERER = new CustomFirstPersonArmRenderer();
        CUSTOM_VEHICLE_RENDERER = new CustomVehicleRenderer();
        SophisticatedCompat.addLayer();
    }

    private static void ensureInitialized() {
        if (CUSTOM_PLAYER_RENDERER == null)
            Minecraft.getInstance().getEntityRenderDispatcher()
                .onResourceManagerReload(Minecraft.getInstance().getResourceManager());
    }

    public static CustomPlayerRenderer getPlayerRenderer() {
        ensureInitialized();
        return CUSTOM_PLAYER_RENDERER;
    }

    public static CustomProjectileRenderer getProjectRenderer() {
        ensureInitialized();
        return CUSTOM_PROJECTILE_RENDERER;
    }

    public static CustomFirstPersonArmRenderer getFirstPersonArmRenderer() {
        ensureInitialized();
        return CUSTOM_FIRST_PERSON_RENDERER;
    }

    public static void releaseFirstPersonArm() {
        RenderFirstPlayerBackground.reset();
        if (CUSTOM_FIRST_PERSON_RENDERER != null)
            CUSTOM_FIRST_PERSON_RENDERER.release();
    }

    public static void releaseFirstPersonArm(LocalPlayer player) {
        RenderFirstPlayerBackground.reset();
        if (CUSTOM_FIRST_PERSON_RENDERER != null)
            CUSTOM_FIRST_PERSON_RENDERER.releaseForPlayer(player);
    }

    //#if MC >= 26.1
    /** Block models for GUI previews (bed, ground). */
    public static net.minecraft.client.renderer.block.BlockModelResolver getBlockModelResolver() {
        ensureInitialized();
        return BLOCK_MODEL_RESOLVER;
    }
    //#endif

    public static CustomVehicleRenderer getVehicleRenderer() {
        ensureInitialized();
        return CUSTOM_VEHICLE_RENDERER;
    }
}
