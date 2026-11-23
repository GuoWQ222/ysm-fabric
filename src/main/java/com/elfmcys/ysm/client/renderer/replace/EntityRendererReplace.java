// Modified for the unofficial Fabric 26.2 port by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.renderer.replace;

import com.elfmcys.ysm.capability.VehicleAnimatableCapabilityProvider;
import com.elfmcys.ysm.client.event.RegisterEntityRenderersEvent;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.MinecartRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/** Vehicle model replacement (Fabric port: called from EntityRenderDispatcherMixin with the extracted state). */
public final class EntityRendererReplace {
    private EntityRendererReplace() {}

    /** @return true when vanilla should still submit this entity */
    public static boolean renderInMixin(Entity entity, EntityRenderState state, float partialTick,
        PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        return entity.getCapability(VehicleAnimatableCapabilityProvider.CAP).map(cap -> {
            if (!cap.isPresentationAllowed())
                return true;
            float yaw = getYaw(entity, state, Mth.lerp(partialTick, entity.yRotO, entity.getYRot()), partialTick);
            return !RegisterEntityRenderersEvent.getVehicleRenderer().renderReplacement(cap, yaw, partialTick, state,
                poseStack, collector, camera);
        }).orElse(true);
    }

    public static float getYaw(Entity entity, float yawIn, float partialTick) {
        return getYaw(entity, null, yawIn, partialTick);
    }

    private static float getYaw(Entity entity, EntityRenderState state, float yawIn, float partialTick) {
        if (entity instanceof LivingEntity living)
            return getLivingEntityYaw(living, partialTick);
        if (state instanceof MinecartRenderState minecart)
            return getMinecartYaw(minecart, yawIn);
        return yawIn;
    }

    private static float getLivingEntityYaw(LivingEntity livingEntity, float partialTick) {
        float yaw = Mth.rotLerp(partialTick, livingEntity.yBodyRotO, livingEntity.yBodyRot);
        float headYaw = Mth.rotLerp(partialTick, livingEntity.yHeadRotO, livingEntity.yHeadRot);
        if (livingEntity.isPassenger() && livingEntity.getVehicle() instanceof LivingEntity vehicle) {
            yaw = Mth.rotLerp(partialTick, vehicle.yBodyRotO, vehicle.yBodyRot);
            float wrappedYawDiff = Mth.clamp(Mth.wrapDegrees(headYaw - yaw), -85.0F, 85.0F);
            yaw = headYaw - wrappedYawDiff;
            if (wrappedYawDiff * wrappedYawDiff > 2500.0F)
                yaw += wrappedYawDiff * 0.2F;
        }
        return yaw;
    }

    /** Rail direction as 1.20 YSM computed it; 26.x extraction already provides the two rail sample points. */
    private static float getMinecartYaw(MinecartRenderState state, float yaw) {
        if (state.frontPos != null && state.backPos != null) {
            var offset = state.backPos.subtract(state.frontPos);
            if (offset.length() != 0)
                return (float)(Math.atan2(offset.z, offset.x) * 180 / Math.PI);
        }
        return yaw;
    }
}
