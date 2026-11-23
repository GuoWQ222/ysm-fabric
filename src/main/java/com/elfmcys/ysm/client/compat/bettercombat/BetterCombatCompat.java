// Modified for the unofficial Fabric 26.2 port by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.compat.bettercombat;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.player.AbstractClientPlayer;

/**
 * Better Combat (Fabric 26.2, mod id {@code bettercombat}, on Player Animation Library). As upstream's Forge
 * integration: a Better Combat attack starts a vanilla swing (synced, so YSM swing animations play), and the name of
 * the running attack animation is available to models. Better Combat classes are only touched by
 * {@link BetterCombatCompatInner}.
 */
public final class BetterCombatCompat {
    private static final String MOD_ID = "bettercombat";
    private static boolean installed;

    private BetterCombatCompat() {}

    public static void init() {
        if (FabricLoader.getInstance().isModLoaded(MOD_ID)) {
            BetterCombatCompatInner.innerInit();
            installed = true;
        }
    }

    public static boolean isInstalled() {
        return installed;
    }

    public static String getAttackAnimation(AbstractClientPlayer player) {
        return installed ? BetterCombatCompatInner.getAttackAnimation(player) : "";
    }

    /** Capability-facing form: "" for entities that are not client players, or without the mod. */
    public static String attackAnimation(net.minecraft.world.entity.Entity entity) {
        return entity instanceof AbstractClientPlayer player ? getAttackAnimation(player) : "";
    }
}
