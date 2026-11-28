// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.renderer.layer;

import com.elfmcys.ysm.client.entity.CustomPlayerEntity;
import com.elfmcys.ysm.geckolib3.geo.GeoLayerRenderer;
import com.elfmcys.ysm.geckolib3.geo.GeoRenderData;
import com.elfmcys.ysm.client.model.locator.PlayerLocator;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.animal.parrot.ParrotModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ParrotRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
//#if MC >= 1.21.2
import net.minecraft.client.renderer.entity.state.ParrotRenderState;
//#endif
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.animal.parrot.Parrot;

/** Shoulder parrots at the model's shoulder locators (Fabric port: 26.x model submission). */
public class CustomParrotOnShoulderLayer extends GeoLayerRenderer<CustomPlayerEntity> {
    private final ParrotModel model;

    public CustomParrotOnShoulderLayer(EntityModelSet modelSet) {
        this.model = new ParrotModel(modelSet.bakeLayer(ModelLayers.PARROT));
    }

    @Override
    public void render(PoseStack poseStack, SubmitNodeCollector collector, CustomPlayerEntity animatable,
        GeoRenderData renderData, LivingEntityRenderState state, int packedLight, int overlay) {
        var player = animatable.getEntity();
        var anim = renderData.animationData;
        com.elfmcys.ysm.platform.McCompat.shoulderParrotLeft(player).ifPresent(variant -> renderData.modelState
            .visitLocatorGroup(PlayerLocator.get().leftShoulder, poseStack, locatorPose -> submitParrot(locatorPose,
                collector, packedLight, state, variant, anim.limbSwing, anim.limbSwingAmount, anim.netHeadYaw,
                anim.headPitch)));
        com.elfmcys.ysm.platform.McCompat.shoulderParrotRight(player).ifPresent(variant -> renderData.modelState
            .visitLocatorGroup(PlayerLocator.get().rightShoulder, poseStack, locatorPose -> submitParrot(locatorPose,
                collector, packedLight, state, variant, anim.limbSwing, anim.limbSwingAmount, anim.netHeadYaw,
                anim.headPitch)));
    }

    private void submitParrot(PoseStack poseStack, SubmitNodeCollector collector, int packedLight,
        LivingEntityRenderState state, Parrot.Variant variant, float limbSwing, float limbSwingAmount, float yRot,
        float xRot) {
        poseStack.translate(0, 1.5, 0);
        com.elfmcys.ysm.platform.McCompat.rotate(poseStack, Axis.ZP.rotationDegrees(180));
        //#if MC >= 1.21.2
        var parrotState = new ParrotRenderState();
        parrotState.pose = ParrotModel.Pose.ON_SHOULDER;
        parrotState.ageInTicks = state.ageInTicks;
        parrotState.walkAnimationPos = limbSwing;
        parrotState.walkAnimationSpeed = limbSwingAmount;
        parrotState.yRot = yRot;
        parrotState.xRot = xRot;
        //#endif
        //#if MC >= 26.3
        //$$ collector.submitModel(model, parrotState, poseStack, ParrotRenderer.getVariantTexture(variant), packedLight,
        //$$     OverlayTexture.NO_OVERLAY, com.elfmcys.ysm.platform.render.YsmRenderStateKeys.outlineColor(state));
        //#elseif MC >= 26.1
        collector.submitModel(model, parrotState, poseStack, ParrotRenderer.getVariantTexture(variant), packedLight,
            OverlayTexture.NO_OVERLAY, com.elfmcys.ysm.platform.render.YsmRenderStateKeys.outlineColor(state), null);
        //#elseif MC >= 1.21.2
        //$$ collector.submitModel(model, parrotState, poseStack, model.renderType(ParrotRenderer.getVariantTexture(variant)),
        //$$     packedLight, OverlayTexture.NO_OVERLAY, com.elfmcys.ysm.platform.render.YsmRenderStateKeys.outlineColor(state), null);
        //#else
        //$$ // before 1.21.2 the shoulder parrot model poses and draws itself
        //$$ var texture = ParrotRenderer.getVariantTexture(variant);
        //$$ int tickCount = (int)state.ageInTicks;
        //$$ collector.submitDeferred(poseStack, (stack, buffers) -> model.renderOnShoulder(stack,
        //$$     buffers.getBuffer(model.renderType(texture)), packedLight, OverlayTexture.NO_OVERLAY, limbSwing,
        //$$     limbSwingAmount, yRot, xRot, tickCount));
        //#endif
    }
}
