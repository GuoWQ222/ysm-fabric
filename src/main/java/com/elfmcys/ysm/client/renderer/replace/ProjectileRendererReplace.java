// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.renderer.replace;

import com.elfmcys.ysm.platform.capability.ICapabilityHolder;
import com.elfmcys.ysm.capability.ProjectileAnimatableCapabilityProvider;
import com.elfmcys.ysm.client.event.RegisterEntityRenderersEvent;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.Projectile;

/** Projectile model replacement (Fabric port: called from EntityRenderDispatcherMixin). */
public final class ProjectileRendererReplace {
    private ProjectileRendererReplace() {}

    /** @return true when vanilla should still submit this entity */
    public static boolean renderInMixin(Projectile entity, EntityRenderState state, float partialTick,
        PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        return ICapabilityHolder.of(entity).getCapability(ProjectileAnimatableCapabilityProvider.CAP).map(cap -> {
            if (!cap.isPresentationAllowed())
                return true;
            float yaw = Mth.lerp(partialTick, entity.yRotO, entity.getYRot());
            return !RegisterEntityRenderersEvent.getProjectRenderer().renderReplacement(cap, yaw, partialTick, state,
                poseStack, collector, camera);
        }).orElse(true);
    }
}
