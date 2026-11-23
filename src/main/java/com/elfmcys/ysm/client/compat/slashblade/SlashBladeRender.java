// Modified for the unofficial Fabric 26.2 port by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.compat.slashblade;

import com.elfmcys.ysm.client.model.locator.PlayerLocator;
import com.elfmcys.ysm.geckolib3.geo.GeoRenderData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Fabric port stand-in: the SlashBladeRender integration targets a Forge mod and is not ported; every method reports
 * "not installed". The original is kept in src/forge-compat-reference.
 */
@SuppressWarnings("unused")
public final class SlashBladeRender {

    private SlashBladeRender() {}

    public static void renderSlashBlade(PoseStack matrixStack, SubmitNodeCollector bufferIn, int lightIn, ItemStack stack) {
        
    }

    public static void renderMainhandSlashBlade(LivingEntity livingEntity, GeoRenderData data, PoseStack matrixStack, SubmitNodeCollector bufferIn, int lightIn, ItemStack stack) {
        
    }

    public static void renderOffhandSlashBlade(GeoRenderData data, PoseStack matrixStack, SubmitNodeCollector bufferIn, int lightIn, ItemStack stack) {
        
    }
}
