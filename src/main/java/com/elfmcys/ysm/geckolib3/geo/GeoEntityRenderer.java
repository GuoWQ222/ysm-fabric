// Modified for the unofficial Fabric 26.2 port by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.geckolib3.geo;

import com.elfmcys.ysm.client.entity.AnimatableEntity;
import com.elfmcys.ysm.geckolib3.core.util.Color;
import com.elfmcys.ysm.platform.render.EntitySubmitHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.Entity;

/**
 * Draws a YSM model for a whole non-player entity (vehicles). Fabric port (26.x): submitted in place of the vanilla
 * renderer; leashes and name display are submitted here as vanilla's EntityRenderer.submit would.
 */
public abstract class GeoEntityRenderer<TEntity extends Entity, T extends AnimatableEntity<TEntity>>
    implements IGeoRenderer<T> {
    public boolean renderReplacement(T animatable, float yaw, float partialTick, EntityRenderState state,
        PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (!animatable.isPresentationAllowed())
            return false;
        var mc = Minecraft.getInstance();
        var data = animatable.update(partialTick);
        return animatable.consumePrepared(data, borrowed -> {
            if (mc.player == null)
                return false;
            var entity = animatable.getEntity();
            var bodyVisible = !entity.isInvisibleTo(mc.player);
            var glowing = state.appearsGlowing();
            var renderType = getRenderType(data.texture, bodyVisible, glowing, data.modelState.hasTranslucentVertices());
            if (renderType != null) {
                int light = state.lightCoords;
                int overlay = getPackedOverlay(entity, 0);
                poseStack.pushPose();
                try {
                    poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));
                    preRender(data, animatable, poseStack, collector, light, overlay, Color.WHITE);
                    if (!animatable.canConsume(data, data.ctx))
                        return false;
                    if (renderType.isOutline()) {
                        renderOutline(data, animatable, renderType, poseStack, collector, state.outlineColor);
                    } else {
                        if (!render(data, animatable, renderType, poseStack, collector, light, overlay, Color.WHITE))
                            return false;
                        renderOutline(data, animatable, renderType, poseStack, collector, state.outlineColor);
                    }
                    if (animatable.canConsume(data, data.ctx))
                        postRender(data, animatable, poseStack, collector, light, overlay, Color.WHITE);
                } finally {
                    poseStack.popPose();
                }
            }
            if (animatable.canConsume(data, data.ctx))
                EntitySubmitHelper.submitLeashesAndName(state, poseStack, collector, camera);
            return true;
        });
    }

    public static int getPackedOverlay(Entity entity, float uIn) {
        return OverlayTexture.pack(OverlayTexture.u(uIn), OverlayTexture.v(false));
    }
}
