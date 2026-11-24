/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.platform.render;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.Nullable;

/**
 * Fabric port: 26.x renders from extracted render states; YSM needs the entity itself (its capabilities hold the
 * animation state), so extraction stores it here and submission reads it back. Stored by EntityRenderStateDataMixin
 * (loader-neutral; was Fabric's render state data keys).
 */
public final class YsmRenderStateKeys {
    private YsmRenderStateKeys() {}

    private static YsmRenderStateData data(EntityRenderState state) {
        return (YsmRenderStateData)state;
    }

    public static void setEntity(EntityRenderState state, @Nullable Entity entity, float partialTick) {
        var data = data(state);
        data.ysm$setEntity(entity);
        data.ysm$setPartialTick(partialTick);
        data.ysm$setReplaced(false);
    }

    public static @Nullable Entity entity(EntityRenderState state) {
        return data(state).ysm$entity();
    }

    /** The partial tick the state was extracted with. */
    public static float partialTick(EntityRenderState state) {
        return data(state).ysm$partialTick();
    }

    /** Set when YSM drew this state's body, so vanilla skips its model and layers. */
    public static void setReplaced(EntityRenderState state, boolean replaced) {
        data(state).ysm$setReplaced(replaced);
    }

    public static boolean isReplaced(EntityRenderState state) {
        return data(state).ysm$replaced();
    }
}
