// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.event;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.event.api.SpecialPlayerRenderEvent;
import com.elfmcys.ysm.platform.Dist;
import com.elfmcys.ysm.platform.event.Mod;
import com.elfmcys.ysm.platform.event.SubscribeEvent;
import net.minecraft.client.player.AbstractClientPlayer;

/**
 * The built-in Steve and Alex models wear the player's own skin, as in 2.6.5.
 *
 * <p>
 * Fabric port fix: 3.0-dev compared against "misc/2_steve" while installed ids are catalog addresses
 * ("builtin:misc/2_steve"), so the swap never happened. Both forms are accepted; the skin is the player's 26.x skin
 * (default skin when none is set).
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class VanillaPlayerRenderEvent {
    private static final String BUILTIN_PREFIX = "builtin:";
    private static final String STEVE = "misc/2_steve";
    private static final String ALEX = "misc/1_alex";

    @SubscribeEvent
    public static void onRenderPlayer(SpecialPlayerRenderEvent event) {
        if (!YesSteveModel.isAvailable())
            return;
        if (isVanillaPlayer(event.getModelId()) && event.getPlayer() instanceof AbstractClientPlayer clientPlayer)
            event.setTextureLocationOverride(clientPlayer.getSkin().body().texturePath());
    }

    static boolean isVanillaPlayer(String modelId) {
        if (modelId == null)
            return false;
        var id = modelId.startsWith(BUILTIN_PREFIX) ? modelId.substring(BUILTIN_PREFIX.length()) : modelId;
        return id.equals(STEVE) || id.equals(ALEX);
    }
}
