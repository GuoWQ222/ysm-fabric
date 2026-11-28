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
import com.elfmcys.ysm.legacy.state.StateExtractor;
import com.elfmcys.ysm.platform.render.YsmRenderStateKeys;
import com.elfmcys.ysm.util.RenderUtil;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.Projectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Minecraft before 1.21.2 (the 1.21.2 - 1.21.8 mixin of the same name, for entity-based rendering): projectile,
 * fishing-hook and vehicle models replace the renderer's render(entity, yaw, partialTick, poseStack, buffers, light)
 * call. YSM's renderers read a render state, built here from the entity (StateExtractor).
 */
@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherLegacyMixin {
    @WrapWithCondition(method = "render",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;render(Lnet/minecraft/world/entity/Entity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"))
    private boolean ysm$replaceRenderer(EntityRenderer<?> renderer, Entity entity, float yaw, float partialTick,
        PoseStack poseStack, MultiBufferSource buffers, int light) {
        if (!YesSteveModel.isAvailable())
            return true;
        boolean projectile = entity instanceof Projectile && !ClientConfig.DISABLE_PROJECTILE_MODEL.get();
        if (!projectile && ClientConfig.DISABLE_VEHICLE_MODEL.get())
            return true;
        var state = StateExtractor.extract(entity, partialTick);
        YsmRenderStateKeys.setLight(state, light);
        var camera = CameraRenderState.current();
        try (var collector = new SubmitNodeCollector(buffers)) {
            if (projectile) {
                return entity instanceof FishingHook hook
                    ? FishingHookRendererReplace.renderInMixin(hook, state, partialTick, poseStack, collector, camera)
                    : ProjectileRendererReplace.renderInMixin((Projectile)entity, state, partialTick, poseStack,
                        collector, camera);
            }
            RenderUtil.adjustPassengerPosition(entity, poseStack, partialTick);
            return EntityRendererReplace.renderInMixin(entity, state, partialTick, poseStack, collector, camera);
        }
    }
}
