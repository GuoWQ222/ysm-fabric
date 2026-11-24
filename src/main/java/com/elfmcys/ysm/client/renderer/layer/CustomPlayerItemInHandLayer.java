// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.renderer.layer;

import com.elfmcys.ysm.client.compat.slashblade.SlashBladeCompat;
import com.elfmcys.ysm.client.compat.slashblade.SlashBladeRender;
import com.elfmcys.ysm.client.compat.swarfare.SWarfareCompat;
import com.elfmcys.ysm.client.compat.tacz.TACZCompat;
import com.elfmcys.ysm.client.entity.CustomPlayerEntity;
import com.elfmcys.ysm.geckolib3.geo.GeoLayerRenderer;
import com.elfmcys.ysm.geckolib3.geo.GeoRenderData;
import com.elfmcys.ysm.client.model.locator.PlayerLocator;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Held items at the model's hand locators (Fabric port: 26.x item submission). */
public class CustomPlayerItemInHandLayer extends GeoLayerRenderer<CustomPlayerEntity> {
    private final ItemModelResolver itemModelResolver;

    public CustomPlayerItemInHandLayer(ItemModelResolver itemModelResolver) {
        this.itemModelResolver = itemModelResolver;
    }

    @Override
    public void render(PoseStack poseStack, SubmitNodeCollector collector, CustomPlayerEntity animatable,
        GeoRenderData renderData, LivingEntityRenderState state, int packedLight, int overlay) {
        var entity = animatable.getEntity();
        var mainHandItem = entity.getMainHandItem();
        if (!mainHandItem.isEmpty() && renderData.modelState.locatorGroupSize(PlayerLocator.get().rightHand) > 0) {
            if (SlashBladeCompat.isSlashBladeItem(mainHandItem)) {
                SlashBladeRender.renderMainhandSlashBlade(entity, renderData, poseStack, collector, packedLight,
                    mainHandItem);
            } else {
                TACZCompat.openFlashShellRender(entity, mainHandItem);
                renderArmWithItem(entity, mainHandItem, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, HumanoidArm.RIGHT,
                    renderData, state, poseStack, collector, packedLight);
                TACZCompat.stopFlashShellRender(mainHandItem);
            }
        }
        var offhandItem = entity.getOffhandItem();
        if (!offhandItem.isEmpty() && renderData.modelState.locatorGroupSize(PlayerLocator.get().leftHand) > 0) {
            if (SlashBladeCompat.isSlashBladeItem(offhandItem)) {
                SlashBladeRender.renderOffhandSlashBlade(renderData, poseStack, collector, packedLight, offhandItem);
            } else if (!SWarfareCompat.isGun(offhandItem)) {
                // Fabric port: upstream 3.0-dev drew the main-hand item here; 2.6.5 draws the off-hand item.
                renderArmWithItem(entity, offhandItem, ItemDisplayContext.THIRD_PERSON_LEFT_HAND, HumanoidArm.LEFT,
                    renderData, state, poseStack, collector, packedLight);
            }
            TACZCompat.renderOffsetHand(offhandItem, renderData, entity, poseStack, collector, packedLight);
            SWarfareCompat.renderOffsetHand(offhandItem, renderData, entity, poseStack, collector, packedLight);
        }
    }

    protected void renderArmWithItem(LivingEntity entity, ItemStack itemStack, ItemDisplayContext displayContext,
        HumanoidArm arm, GeoRenderData data, LivingEntityRenderState state, PoseStack poseStack,
        SubmitNodeCollector collector, int packedLight) {
        if (itemStack.isEmpty())
            return;
        boolean isLeftHand = arm == HumanoidArm.LEFT;
        var locator = isLeftHand ? PlayerLocator.get().leftHand : PlayerLocator.get().rightHand;
        var itemState = new ItemStackRenderState();
        itemModelResolver.updateForLiving(itemState, itemStack, displayContext, entity);
        if (itemState.isEmpty())
            return;
        data.modelState.visitLocatorGroup(locator, poseStack, locatorPose -> {
            locatorPose.translate(0, -0.0625, -0.1);
            com.elfmcys.ysm.platform.McCompat.rotate(locatorPose, Axis.XP.rotationDegrees(-90.0F));
            if (SWarfareCompat.isGun(itemStack)) {
                locatorPose.translate(0.1, 0, 0);
                locatorPose.scale(1.25f, 1.25f, 1.25f);
            }
            itemState.submit(locatorPose, collector, packedLight, OverlayTexture.NO_OVERLAY, state.outlineColor);
        });
    }
}
