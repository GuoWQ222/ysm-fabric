/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.legacy.submit.mixin;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.client.renderer.replace.EntityRendererReplace;
import com.elfmcys.ysm.client.renderer.replace.FishingHookRendererReplace;
import com.elfmcys.ysm.client.renderer.replace.ProjectileRendererReplace;
import com.elfmcys.ysm.config.ClientConfig;
import com.elfmcys.ysm.legacy.render.CameraRenderState;
import com.elfmcys.ysm.legacy.render.SubmitNodeCollector;
import com.elfmcys.ysm.platform.render.YsmRenderStateKeys;
import com.elfmcys.ysm.util.RenderUtil;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.Projectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Minecraft before 1.21.9 (EntityRenderDispatcherMixin's counterpart): projectile, fishing-hook and vehicle models
 * replace the renderer's render(state, poseStack, buffers, light) call.
 */
@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherLegacyMixin {
    // the private overload that calls the renderer takes a state (1.21.5 - 1.21.8) or the entity (1.21.2 - 1.21.4)
    //#if MC >= 1.21.5
    @WrapWithCondition(method = "render(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;DDDLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/EntityRenderer;)V",
    //#else
    //$$ @WrapWithCondition(method = "render(Lnet/minecraft/world/entity/Entity;DDDFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/EntityRenderer;)V",
    //#endif
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;render(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"))
    private boolean ysm$replaceRenderer(EntityRenderer<?, ?> renderer, EntityRenderState state, PoseStack poseStack,
        MultiBufferSource buffers, int light) {
        if (!YesSteveModel.isAvailable())
            return true;
        var entity = YsmRenderStateKeys.entity(state);
        if (entity == null)
            return true;
        YsmRenderStateKeys.setLight(state, light);
        float partialTick = YsmRenderStateKeys.partialTick(state);
        var camera = CameraRenderState.current();
        try (var collector = new SubmitNodeCollector(buffers)) {
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
}
