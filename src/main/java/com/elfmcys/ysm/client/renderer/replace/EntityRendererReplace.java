// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.renderer.replace;

import com.elfmcys.ysm.platform.capability.ICapabilityHolder;
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
        return ICapabilityHolder.of(entity).getCapability(VehicleAnimatableCapabilityProvider.CAP).map(cap -> {
            if (!cap.isPresentationAllowed())
                return true;
            var rotation = rotation(entity, state, partialTick);
            return !RegisterEntityRenderersEvent.getVehicleRenderer().renderReplacement(cap, rotation.rotation(),
                rotation.angle(), partialTick, state,
                poseStack, collector, camera);
        }).orElse(true);
    }

    /** The vehicle model's rotation, and the yaw angle passengers undo after taking their seat. */
    public record VehicleRotation(org.joml.Quaternionf rotation, float angle) {
    }

    /**
     * 2.6.5 (26.x): living vehicles turn by 180 - body yaw; the rideable minecart follows the new minecart renderer's yaw
     * and pitch, or the rail direction with the classic one; other vehicles turn by 180 - yaw.
     */
    public static VehicleRotation rotation(Entity entity, EntityRenderState state, float partialTick) {
        if (entity instanceof LivingEntity living) {
            float angle = 180.0F - getLivingEntityYaw(living, partialTick);
            return new VehicleRotation(com.mojang.math.Axis.YP.rotationDegrees(angle), angle);
        }
        if (entity instanceof net.minecraft.world.entity.vehicle.minecart.Minecart
            && state instanceof MinecartRenderState minecart) {
            if (minecart.isNewRender) {
                var rotation = com.mojang.math.Axis.YP.rotationDegrees(minecart.yRot)
                    .mul(com.mojang.math.Axis.ZP.rotationDegrees(-minecart.xRot));
                return new VehicleRotation(rotation, minecart.yRot);
            }
            float yaw = minecart.yRot;
            float pitch = minecart.xRot;
            if (minecart.posOnRail != null && minecart.frontPos != null && minecart.backPos != null) {
                var direction = minecart.backPos.subtract(minecart.frontPos);
                if (direction.length() != 0) {
                    direction = direction.normalize();
                    yaw = (float)(Math.atan2(direction.z, direction.x) * 180.0 / Math.PI);
                    pitch = (float)(Math.atan(direction.y) * 73.0);
                }
            }
            float angle = 180.0F - yaw;
            var rotation = com.mojang.math.Axis.YP.rotationDegrees(angle)
                .mul(com.mojang.math.Axis.ZP.rotationDegrees(-pitch));
            return new VehicleRotation(rotation, angle);
        }
        float angle = 180.0F - entity.getYRot(partialTick);
        return new VehicleRotation(com.mojang.math.Axis.YP.rotationDegrees(angle), angle);
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
