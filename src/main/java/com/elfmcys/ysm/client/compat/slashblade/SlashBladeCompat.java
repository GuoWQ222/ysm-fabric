// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.compat.slashblade;

import com.elfmcys.ysm.client.entity.CustomHumanoidEntity;
import com.elfmcys.ysm.geckolib3.core.PlayState;
import com.elfmcys.ysm.geckolib3.core.builder.LoopType;
import com.elfmcys.ysm.geckolib3.core.event.predicate.AnimationEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Fabric port stand-in: the SlashBladeCompat integration targets a Forge mod and is not ported; every method reports
 * "not installed". The original is kept in src/forge-compat-reference.
 */
@SuppressWarnings("unused")
public final class SlashBladeCompat {

    private SlashBladeCompat() {}

    public static void init() {
        
    }

    public static boolean isSlashBladeLoaded() {
        return false;
    }

    public static boolean isSlashBladeItem(ItemStack stack) {
        return false;
    }

    public static String getAnimationName(AnimationEvent<? extends CustomHumanoidEntity<?>> event) {
        return null;
    }

    public static String getAnimationName(LivingEntity entity) {
        return null;
    }

    public static String animationName(net.minecraft.world.entity.Entity entity) {
        return null;
    }

    public static PlayState playMainAnimation(LivingEntity livingEntity, AnimationEvent<? extends CustomHumanoidEntity<?>> event, String animationName, LoopType loopType) {
        return null;
    }

    public static boolean isResharped() {
        return false;
    }
}
