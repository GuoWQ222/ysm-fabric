// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.compat.tacz;

import com.elfmcys.ysm.client.entity.CustomHumanoidEntity;
import com.elfmcys.ysm.geckolib3.core.PlayState;
import com.elfmcys.ysm.geckolib3.core.builder.LoopType;
import com.elfmcys.ysm.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.ysm.geckolib3.geo.GeoRenderData;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Fabric port stand-in: the TACZCompat integration targets a Forge mod and is not ported; every method reports
 * "not installed". The original is kept in src/forge-compat-reference.
 */
@SuppressWarnings("unused")
public final class TACZCompat {

    private TACZCompat() {}

    public static void init() {
        
    }

    public static void renderOffsetHand(ItemStack offhandItem, GeoRenderData data, LivingEntity livingEntity, PoseStack poseStack, SubmitNodeCollector buffer, int packedLight) {
        
    }

    public static PlayState playGunMainAnimation(LivingEntity livingEntity, AnimationEvent<? extends CustomHumanoidEntity<?>> event, String animationName, LoopType loopType) {
        return null;
    }

    public static PlayState playGunHoldAnimation(ItemStack mainHandItem, AnimationEvent<? extends CustomHumanoidEntity<?>> event) {
        return null;
    }

    public static PlayState playGunOnceAnimation(ItemStack mainHandItem, AnimationEvent<? extends CustomHumanoidEntity<?>> event) {
        return null;
    }

    public static void openFlashShellRender(LivingEntity livingEntity, ItemStack mainHandItem) {
        
    }

    public static void stopFlashShellRender(ItemStack mainHandItem) {
        
    }

    public static Identifier getGunId(ItemStack stack) {
        return null;
    }

    public static boolean isInstalled() {
        return false;
    }

    public static boolean isMainhandHoldingGun(LivingEntity entity) {
        return false;
    }

    public static boolean mainhandHoldingGun(net.minecraft.world.entity.Entity entity) {
        return false;
    }
}
