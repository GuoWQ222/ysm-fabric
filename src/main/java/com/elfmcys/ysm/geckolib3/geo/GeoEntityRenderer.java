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
import net.minecraft.world.entity.Entity;

/**
 * Draws a YSM model for a whole non-player entity (vehicles). Fabric port (26.x): submitted in place of the vanilla
 * renderer; leashes and name display are submitted here as vanilla's EntityRenderer.submit would.
 */
public abstract class GeoEntityRenderer<TEntity extends Entity, T extends AnimatableEntity<TEntity>>
    implements IGeoRenderer<T> {
    public boolean renderReplacement(T animatable, org.joml.Quaternionf rotation, float angle, float partialTick,
        EntityRenderState state,
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
                    com.elfmcys.ysm.platform.McCompat.rotate(poseStack, rotation);
                    if (animatable instanceof com.elfmcys.ysm.client.entity.CustomVehicleEntity vehicle)
                        recordPassengerSeats(vehicle, data, rotation, angle);
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

    /** 2.6.5 parity: seat i = vehicle rotation * PassengerLocator[i], relative to the vehicle origin. */
    private static void recordPassengerSeats(com.elfmcys.ysm.client.entity.CustomVehicleEntity vehicle,
        GeoRenderData data, org.joml.Quaternionf rotation, float angle) {
        var seats = new java.util.ArrayList<org.joml.Matrix4f>();
        var seatStack = new PoseStack();
        com.elfmcys.ysm.platform.McCompat.rotate(seatStack, rotation);
        try {
            data.modelState.visitLocatorGroup(com.elfmcys.ysm.client.model.locator.VehicleLocator.get().passenger,
                seatStack, seat -> seats.add(new org.joml.Matrix4f(seat.last().pose())));
            vehicle.seatDiagnostic = "seats=" + seats.size();
        } catch (IllegalArgumentException notAVehicleModel) {
            seats.clear();
            vehicle.seatDiagnostic = "locator type mismatch: " + notAVehicleModel.getMessage();
        }
        vehicle.updatePassengerSeats(seats, angle);
    }

    public static int getPackedOverlay(Entity entity, float uIn) {
        return OverlayTexture.pack(OverlayTexture.u(uIn), OverlayTexture.v(false));
    }
}
