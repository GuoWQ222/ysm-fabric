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
        data.ysm$setFirstPersonModel(false);
    }

    /**
     * First-person Model was drawing the local player when the state was extracted. Its builds for Minecraft 1.21.9 and
     * 1.21.10 raise their "rendering the player" flag only while entities are extracted, not while they are submitted.
     */
    public static void setFirstPersonModel(EntityRenderState state, boolean drawing) {
        data(state).ysm$setFirstPersonModel(drawing);
    }

    public static boolean firstPersonModel(EntityRenderState state) {
        return data(state).ysm$firstPersonModel();
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

    /** Packed light of the state (1.21.9: its lightCoords field; before: YSM's copy, see the state mixin). */
    public static int light(EntityRenderState state) {
        //#if MC >= 1.21.9
        return state.lightCoords;
        //#else
        //$$ return data(state).ysm$light();
        //#endif
    }

    public static void setLight(EntityRenderState state, int light) {
        //#if MC >= 1.21.9
        state.lightCoords = light;
        //#else
        //$$ data(state).ysm$setLight(light);
        //#endif
    }

    /** Outline colour of the state, 0 = none. */
    public static int outlineColor(EntityRenderState state) {
        //#if MC >= 1.21.9
        return state.outlineColor;
        //#else
        //$$ return data(state).ysm$outlineColor();
        //#endif
    }

    public static void setOutlineColor(EntityRenderState state, int color) {
        //#if MC >= 1.21.9
        state.outlineColor = color;
        //#else
        //$$ data(state).ysm$setOutlineColor(color);
        //#endif
    }

    public static boolean appearsGlowing(EntityRenderState state) {
        //#if MC >= 1.21.9
        return state.appearsGlowing();
        //#else
        //$$ return data(state).ysm$outlineColor() != 0;
        //#endif
    }
}
