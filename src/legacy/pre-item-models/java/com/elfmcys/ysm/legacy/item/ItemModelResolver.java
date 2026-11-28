/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.legacy.item;

import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Minecraft before 1.21.4 has no item model resolver (items are drawn by ItemRenderer.renderStatic). This stand-in
 * (mapped from net.minecraft.client.renderer.item.ItemModelResolver by versions/below-1.21.4.remap) fills YSM's
 * ItemStackRenderState stand-in with what renderStatic needs.
 */
public final class ItemModelResolver {
    private final ItemRenderer itemRenderer;

    public ItemModelResolver(ItemRenderer itemRenderer) {
        this.itemRenderer = itemRenderer;
    }

    public void updateForLiving(ItemStackRenderState state, ItemStack stack, ItemDisplayContext context,
        boolean leftHand, LivingEntity entity) {
        state.set(itemRenderer, stack, context, leftHand, entity);
    }
}
