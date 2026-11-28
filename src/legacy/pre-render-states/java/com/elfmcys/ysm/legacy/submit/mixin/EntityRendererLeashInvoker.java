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
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Minecraft before 1.21.2: vanilla's private leash drawing (an instance method of the entity's renderer here), for
 * entities whose whole renderer YSM replaces.
 */
//#if MC >= 1.21
@Mixin(EntityRenderer.class)
//#else
//$$ // before 1.21 the leash is drawn by the mob renderer
//$$ @Mixin(net.minecraft.client.renderer.entity.MobRenderer.class)
//#endif
public interface EntityRendererLeashInvoker {
    @Invoker("renderLeash")
    //#if MC >= 1.21
    void ysm$renderLeash(Entity entity, float partialTick, PoseStack poseStack, MultiBufferSource buffers,
        Entity holder);
    //#else
    //$$ void ysm$renderLeash(net.minecraft.world.entity.Mob entity, float partialTick, PoseStack poseStack,
    //$$     MultiBufferSource buffers, Entity holder);
    //#endif
}
