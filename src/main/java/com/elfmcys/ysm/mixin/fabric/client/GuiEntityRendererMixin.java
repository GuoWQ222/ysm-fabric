/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.mixin.fabric.client;

import com.elfmcys.ysm.platform.render.YsmRenderStateKeys;
import com.elfmcys.ysm.util.RenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.render.pip.GuiEntityRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.gui.pip.GuiEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fabric port of InventoryScreenMixin: entities drawn in GUIs (inventory, YSM screens) animate with the inventory
 * render context. 26.x draws them in GuiEntityRenderer at GUI render time.
 *
 * <p>
 * 1.20's renderEntityInInventoryFollowsMouse posed the entity itself (body 180 + a*20, yaw and head 180 + a*40,
 * pitch -b*20) around the draw, and YSM computes its pose from those entity fields. 26.x writes the same angles only
 * into the render state (bodyRot, yRot relative to the body, xRot), so they are applied to the entity for the draw
 * and restored afterwards, as 1.20 did.
 */
@Mixin(GuiEntityRenderer.class)
public abstract class GuiEntityRendererMixin {
    @Unique
    private LivingEntity ysm$posed;
    @Unique
    private final float[] ysm$saved = new float[8];

    @Inject(method = "renderToTexture", at = @At("HEAD"))
    //#if MC >= 26.2
    private void ysm$beginInventory(GuiEntityRenderState entityState, PoseStack poseStack,
        SubmitNodeCollector collector, CallbackInfo ci) {
    //#else
    //$$ private void ysm$beginInventory(GuiEntityRenderState entityState, PoseStack poseStack, CallbackInfo ci) {
    //#endif
        RenderUtil.setRenderingInInventory(true);
        ysm$posed = null;
        if (entityState.renderState() instanceof LivingEntityRenderState state
            && YsmRenderStateKeys.entity(state) instanceof LivingEntity entity) {
            var s = ysm$saved;
            s[0] = entity.yBodyRot;
            s[1] = entity.yBodyRotO;
            s[2] = entity.getYRot();
            s[3] = entity.yRotO;
            s[4] = entity.getXRot();
            s[5] = entity.xRotO;
            s[6] = entity.yHeadRot;
            s[7] = entity.yHeadRotO;
            ysm$posed = entity;
            float head = state.bodyRot + state.yRot;
            entity.yBodyRot = entity.yBodyRotO = state.bodyRot;
            entity.setYRot(head);
            entity.yRotO = head;
            entity.setXRot(state.xRot);
            entity.xRotO = state.xRot;
            entity.yHeadRot = entity.yHeadRotO = head;
        }
    }

    @Inject(method = "renderToTexture", at = @At("RETURN"))
    //#if MC >= 26.2
    private void ysm$endInventory(GuiEntityRenderState entityState, PoseStack poseStack,
        SubmitNodeCollector collector, CallbackInfo ci) {
    //#else
    //$$ private void ysm$endInventory(GuiEntityRenderState entityState, PoseStack poseStack, CallbackInfo ci) {
    //#endif
        var entity = ysm$posed;
        if (entity != null) {
            var s = ysm$saved;
            entity.yBodyRot = s[0];
            entity.yBodyRotO = s[1];
            entity.setYRot(s[2]);
            entity.yRotO = s[3];
            entity.setXRot(s[4]);
            entity.xRotO = s[5];
            entity.yHeadRot = s[6];
            entity.yHeadRotO = s[7];
            ysm$posed = null;
        }
        RenderUtil.setRenderingInInventory(false);
    }
}
