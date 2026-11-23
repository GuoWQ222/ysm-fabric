package com.elfmcys.ysm.mixin.fabric.client;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.client.renderer.replace.EntityRendererReplace;
import com.elfmcys.ysm.client.renderer.replace.FishingHookRendererReplace;
import com.elfmcys.ysm.client.renderer.replace.ProjectileRendererReplace;
import com.elfmcys.ysm.config.ClientConfig;
import com.elfmcys.ysm.platform.render.YsmRenderStateKeys;
import com.elfmcys.ysm.util.RenderUtil;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.Projectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Fabric port of EntityRenderDispatcherMixin: projectile, fishing-hook and vehicle models replace the renderer. */
@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {
    @WrapWithCondition(method = "submit",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;submit(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V"))
    private boolean ysm$replaceRenderer(EntityRenderer<?, ?> renderer, EntityRenderState state, PoseStack poseStack,
        SubmitNodeCollector collector, CameraRenderState camera) {
        if (!YesSteveModel.isAvailable())
            return true;
        var entity = YsmRenderStateKeys.entity(state);
        if (entity == null)
            return true;
        float partialTick = YsmRenderStateKeys.partialTick(state);
        if (entity instanceof Projectile projectile && !ClientConfig.DISABLE_PROJECTILE_MODEL.get()) {
            return projectile instanceof FishingHook hook
                ? FishingHookRendererReplace.renderInMixin(hook, state, partialTick, poseStack, collector, camera)
                : ProjectileRendererReplace.renderInMixin(projectile, state, partialTick, poseStack, collector, camera);
        }
        if (!ClientConfig.DISABLE_VEHICLE_MODEL.get()) {
            RenderUtil.adjustPassengerPosition(entity, poseStack, partialTick);
            return EntityRendererReplace.renderInMixin(entity, state, partialTick, poseStack, collector, camera);
        }
        return true;
    }
}
