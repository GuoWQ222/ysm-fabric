/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.legacy.item;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Minecraft before 1.21.4: an item to draw with ItemRenderer.renderStatic (see ItemModelResolver). */
public final class ItemStackRenderState {
    private ItemRenderer itemRenderer;
    private ItemStack stack = ItemStack.EMPTY;
    private ItemDisplayContext context = ItemDisplayContext.NONE;
    private boolean leftHand;
    private LivingEntity entity;

    void set(ItemRenderer itemRenderer, ItemStack stack, ItemDisplayContext context, boolean leftHand,
        LivingEntity entity) {
        this.itemRenderer = itemRenderer;
        this.stack = stack;
        this.context = context;
        this.leftHand = leftHand;
        this.entity = entity;
    }

    public boolean isEmpty() {
        return stack.isEmpty() || itemRenderer == null;
    }

    public void render(PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
        if (isEmpty())
            return;
        itemRenderer.renderStatic(entity, stack, context, leftHand, poseStack, buffers, entity.level(), light, overlay,
            entity.getId() + context.ordinal());
    }
}
