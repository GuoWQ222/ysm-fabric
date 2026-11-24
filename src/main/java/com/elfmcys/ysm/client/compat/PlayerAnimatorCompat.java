// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.compat;

import net.minecraft.client.player.AbstractClientPlayer;

/**
 * Player animation library. Upstream (Forge, 1.20) used KosmX's playerAnimator; on 26.2 Fabric its successor is
 * Player Animation Library (mod id {@code player_animation_library}), which Better Combat 3.x and others animate with.
 * As upstream: while an external animation asks for the third-person model in first person, YSM draws its full model
 * there (unless DisableExternalFirstPersonAnim). Library classes are only touched by {@link Inner}.
 */
public final class PlayerAnimatorCompat {
    private static final String MOD_ID = "player_animation_library";
    private static boolean installed;

    private PlayerAnimatorCompat() {}

    public static void init() {
        installed = com.elfmcys.ysm.platform.Platform.isModLoaded(MOD_ID);
    }

    public static boolean isInstalled() {
        return installed;
    }

    public static boolean hasThirdPersonModelAnim(AbstractClientPlayer player) {
        return installed && Inner.hasThirdPersonModelAnim(player);
    }

    private static final class Inner {
        static boolean hasThirdPersonModelAnim(AbstractClientPlayer player) {
            try {
                var animations = com.zigythebird.playeranim.api.PlayerAnimationAccess.getPlayerAnimManager(player);
                return animations != null && animations.isActive() && animations.getFirstPersonMode()
                    == com.zigythebird.playeranimcore.api.firstPerson.FirstPersonMode.THIRD_PERSON_MODEL;
            } catch (IllegalArgumentException notAnimatable) {
                return false;
            }
        }
    }
}
