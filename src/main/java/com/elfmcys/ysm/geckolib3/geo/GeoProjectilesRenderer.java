// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;

/** Draws a YSM projectile model (Fabric port: submitted in place of the vanilla renderer). */
public abstract class GeoProjectilesRenderer<TEntity extends Projectile, T extends AnimatableEntity<TEntity>>
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
            var glowing = com.elfmcys.ysm.platform.render.YsmRenderStateKeys.appearsGlowing(state);
            var renderType = getRenderType(data.texture, bodyVisible, glowing, data.modelState.hasTranslucentVertices());
            if (renderType != null) {
                int light = com.elfmcys.ysm.platform.render.YsmRenderStateKeys.light(state);
                int overlay = getPackedOverlay(entity, 0);
                poseStack.pushPose();
                try {
                    com.elfmcys.ysm.platform.McCompat.rotate(poseStack, 
                        Axis.YP.rotationDegrees(Mth.lerp(partialTick, entity.yRotO, entity.getYRot()) - 90));
                    com.elfmcys.ysm.platform.McCompat.rotate(poseStack, Axis.ZP.rotationDegrees(Mth.lerp(partialTick, entity.xRotO, entity.getXRot())));
                    preRender(data, animatable, poseStack, collector, light, overlay, Color.WHITE);
                    if (!animatable.canConsume(data, data.ctx))
                        return false;
                    if (renderType.isOutline()) {
                        renderOutline(data, animatable, renderType, poseStack, collector, com.elfmcys.ysm.platform.render.YsmRenderStateKeys.outlineColor(state));
                    } else {
                        if (!render(data, animatable, renderType, poseStack, collector, light, overlay, Color.WHITE))
                            return false;
                        renderOutline(data, animatable, renderType, poseStack, collector, com.elfmcys.ysm.platform.render.YsmRenderStateKeys.outlineColor(state));
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
