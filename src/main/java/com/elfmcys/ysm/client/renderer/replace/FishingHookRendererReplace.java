// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.renderer.replace;

import com.elfmcys.ysm.platform.capability.ICapabilityHolder;
import com.elfmcys.ysm.capability.ProjectileAnimatableCapabilityProvider;
import com.elfmcys.ysm.client.event.RegisterEntityRenderersEvent;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.FishingHookRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.FishingHook;

/**
 * Fishing bobber model replacement plus the fishing line. Fabric port: the line starts at the rod-hand origin that
 * 26.x extraction computes for the vanilla bobber (FishingHookRenderState.lineOriginOffset).
 */
public final class FishingHookRendererReplace {
    private static final int LINE_SEGMENTS = 16;

    private FishingHookRendererReplace() {}

    /** @return true when vanilla should still submit this entity */
    public static boolean renderInMixin(FishingHook entity, EntityRenderState state, float partialTick,
        PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        return ICapabilityHolder.of(entity).getCapability(ProjectileAnimatableCapabilityProvider.CAP).map(cap -> {
            if (!cap.isPresentationAllowed())
                return true;
            // The bobber pitches erratically; YSM pins it level.
            entity.setXRot(0);
            entity.xRotO = 0;
            float yaw = Mth.lerp(partialTick, entity.yRotO, entity.getYRot());
            if (!RegisterEntityRenderersEvent.getProjectRenderer().renderReplacement(cap, yaw, partialTick, state,
                poseStack, collector, camera))
                return true;
            if (entity.getPlayerOwner() != null && state instanceof FishingHookRenderState hook)
                submitFishingLine(hook, poseStack, collector);
            return false;
        }).orElse(true);
    }

    private static void submitFishingLine(FishingHookRenderState state, PoseStack poseStack,
        SubmitNodeCollector collector) {
        float xa = (float)state.lineOriginOffset.x;
        float ya = (float)state.lineOriginOffset.y;
        float za = (float)state.lineOriginOffset.z;
        float width = com.elfmcys.ysm.platform.McCompat.gameRenderState().windowRenderState.appropriateLineWidth;
        collector.submitCustomGeometry(poseStack, RenderTypes.lines(), (pose, buffer) -> {
            for (int i = 0; i < LINE_SEGMENTS; i++) {
                float a0 = (float)i / LINE_SEGMENTS;
                float a1 = (float)(i + 1) / LINE_SEGMENTS;
                stringVertex(xa, ya, za, buffer, pose, a0, a1, width);
                stringVertex(xa, ya, za, buffer, pose, a1, a0, width);
            }
        });
    }

    private static void stringVertex(float xa, float ya, float za, VertexConsumer buffer, PoseStack.Pose pose,
        float aa, float nexta, float width) {
        float x = xa * aa;
        float y = ya * (aa * aa + aa) * 0.5F + 0.25F;
        float z = za * aa;
        float nx = xa * nexta - x;
        float ny = ya * (nexta * nexta + nexta) * 0.5F + 0.25F - y;
        float nz = za * nexta - z;
        float length = Mth.sqrt(nx * nx + ny * ny + nz * nz);
        nx /= length;
        ny /= length;
        nz /= length;
        buffer.addVertex(pose, x, y, z).setColor(0xFF000000).setNormal(pose, nx, ny, nz).setLineWidth(width);
    }
}
