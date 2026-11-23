/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.platform.render;

import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

/**
 * Fabric port: 26.x renders from extracted render states; YSM needs the entity itself (its capabilities hold the
 * animation state), so extraction stores it here and submission reads it back.
 */
public final class YsmRenderStateKeys {
    public static final RenderStateDataKey<Entity> ENTITY = RenderStateDataKey.create(() -> "ysm:entity");
    /** Set when YSM drew this state's body, so vanilla skips its model and layers. */
    public static final RenderStateDataKey<Boolean> REPLACED = RenderStateDataKey.create(() -> "ysm:replaced");
    /** The partial tick the state was extracted with. */
    public static final RenderStateDataKey<Float> PARTIAL_TICK = RenderStateDataKey.create(() -> "ysm:partial_tick");

    private YsmRenderStateKeys() {}

    public static void setEntity(EntityRenderState state, @Nullable Entity entity, float partialTick) {
        ((FabricRenderState)state).setData(ENTITY, entity);
        ((FabricRenderState)state).setData(PARTIAL_TICK, partialTick);
        ((FabricRenderState)state).setData(REPLACED, null);
    }

    public static @Nullable Entity entity(EntityRenderState state) {
        return ((FabricRenderState)state).getData(ENTITY);
    }

    public static float partialTick(EntityRenderState state) {
        Float value = ((FabricRenderState)state).getData(PARTIAL_TICK);
        return value == null ? 1.0f : value;
    }

    public static void setReplaced(EntityRenderState state, boolean replaced) {
        ((FabricRenderState)state).setData(REPLACED, replaced ? Boolean.TRUE : null);
    }

    public static boolean isReplaced(EntityRenderState state) {
        return Boolean.TRUE.equals(((FabricRenderState)state).getData(REPLACED));
    }
}
