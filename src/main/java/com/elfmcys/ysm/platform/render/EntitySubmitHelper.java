/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.platform.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;

/**
 * Fabric port: what vanilla's EntityRenderer.submit adds after the body (leashes and name display), for entities whose
 * whole vanilla renderer YSM replaces (1.20 called super.render for this).
 */
public final class EntitySubmitHelper {
    private EntitySubmitHelper() {}

    public static void submitLeashesAndName(EntityRenderState state, PoseStack poseStack,
        SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.leashStates != null) {
            for (var leash : state.leashStates)
                collector.submitLeash(poseStack, leash);
        }
        poseStack.pushPose();
        if (state.scoreText != null) {
            collector.submitNameTag(poseStack, state.nameTagAttachment, 0, state.scoreText, !state.isDiscrete,
                state.lightCoords, camera);
            poseStack.translate(0.0F, 9.0F * 1.15F * 0.025F, 0.0F);
        }
        if (state.nameTag != null)
            collector.submitNameTag(poseStack, state.nameTagAttachment, 0, state.nameTag, !state.isDiscrete,
                state.lightCoords, camera);
        poseStack.popPose();
    }
}
