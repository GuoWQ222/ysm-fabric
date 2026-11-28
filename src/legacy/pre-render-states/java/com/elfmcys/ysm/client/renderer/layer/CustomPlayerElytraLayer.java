package com.elfmcys.ysm.client.renderer.layer;

import com.elfmcys.ysm.client.entity.CustomPlayerEntity;
import com.elfmcys.ysm.client.model.locator.PlayerLocator;
import com.elfmcys.ysm.geckolib3.geo.GeoLayerRenderer;
import com.elfmcys.ysm.geckolib3.geo.GeoRenderData;
import com.elfmcys.ysm.legacy.render.SubmitNodeCollector;
import com.elfmcys.ysm.legacy.state.LivingEntityRenderState;
import com.elfmcys.ysm.util.EquipmentUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.PlayerModelPart;

/**
 * Elytra at the elytra locator, Minecraft before 1.21.2 (replaces the main file there): the elytra is the Elytra item
 * with the 1.20-era ElytraModel, drawn as upstream YSM (1.20.1) and vanilla's ElytraLayer of these versions draw it.
 */
public class CustomPlayerElytraLayer extends GeoLayerRenderer<CustomPlayerEntity> {
    private static final ResourceLocation WINGS_LOCATION =
        ResourceLocation.withDefaultNamespace("textures/entity/elytra.png");
    private final ElytraModel<LivingEntity> elytraModel;

    public CustomPlayerElytraLayer(EntityModelSet modelSet) {
        this.elytraModel = new ElytraModel<>(modelSet.bakeLayer(ModelLayers.ELYTRA));
    }

    @Override
    public void render(PoseStack poseStack, SubmitNodeCollector collector, CustomPlayerEntity animatable,
        GeoRenderData renderData, LivingEntityRenderState state, int packedLight, int overlay) {
        var player = animatable.getEntity();
        var stack = EquipmentUtil.getEquippedElytraItem(player);
        if (stack.isEmpty())
            return;
        ResourceLocation texture = WINGS_LOCATION;
        if (player instanceof AbstractClientPlayer clientPlayer) {
            var skin = clientPlayer.getSkin();
            if (skin.elytraTexture() != null)
                texture = skin.elytraTexture();
            else if (skin.capeTexture() != null && player.isModelPartShown(PlayerModelPart.CAPE))
                texture = skin.capeTexture();
        }
        var wingsTexture = texture;
        var anim = renderData.animationData;
        renderData.modelState.visitLocatorGroup(PlayerLocator.get().elytra, poseStack, locatorPose -> {
            // as the 1.21.2+ layer: the model at the locator, only flipped (upstream 1.20.1's offset and 2x scale drew
            // the wings twice as large as 2.6.5 does)
            com.elfmcys.ysm.platform.McCompat.rotate(locatorPose, Axis.ZP.rotationDegrees(180));
            elytraModel.young = player.isBaby();
            elytraModel.setupAnim(player, anim.limbSwing, anim.limbSwingAmount, anim.lerpedAge, anim.netHeadYaw,
                anim.headPitch);
            //#if MC >= 1.21
            var buffer = ItemRenderer.getArmorFoilBuffer(collector.buffers(),
                RenderType.armorCutoutNoCull(wingsTexture), stack.hasFoil());
            //#else
            //$$ var buffer = ItemRenderer.getArmorFoilBuffer(collector.buffers(),
            //$$     RenderType.armorCutoutNoCull(wingsTexture), false, stack.hasFoil());
            //#endif
            //#if MC >= 1.21
            elytraModel.renderToBuffer(locatorPose, buffer, packedLight, OverlayTexture.NO_OVERLAY);
            //#else
            //$$ elytraModel.renderToBuffer(locatorPose, buffer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
            //#endif
        });
    }
}
