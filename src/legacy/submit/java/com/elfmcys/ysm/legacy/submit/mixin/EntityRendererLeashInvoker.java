/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.legacy.submit.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Minecraft before 1.21.9: vanilla's private leash drawing, for entities whose whole renderer YSM replaces. */
@Mixin(EntityRenderer.class)
public interface EntityRendererLeashInvoker {
    @Invoker("renderLeash")
    static void ysm$renderLeash(PoseStack poseStack, MultiBufferSource buffers, EntityRenderState.LeashState leash) {
        throw new AssertionError("mixin");
    }
}
