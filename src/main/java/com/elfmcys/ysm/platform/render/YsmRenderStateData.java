/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.platform.render;

import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.Nullable;

/** YSM's data on vanilla EntityRenderState (added by EntityRenderStateDataMixin; loader-neutral). */
public interface YsmRenderStateData {
    @Nullable
    Entity ysm$entity();

    void ysm$setEntity(@Nullable Entity entity);

    float ysm$partialTick();

    void ysm$setPartialTick(float partialTick);

    boolean ysm$replaced();

    void ysm$setReplaced(boolean replaced);

    /** Packed light (Minecraft before 1.21.9: the state has no lightCoords field). */
    int ysm$light();

    void ysm$setLight(int light);

    /** Outline colour, 0 = none (Minecraft before 1.21.9: no outlineColor field). */
    int ysm$outlineColor();

    void ysm$setOutlineColor(int color);

    /** First-person Model was drawing the local player when this state was extracted. */
    boolean ysm$firstPersonModel();

    void ysm$setFirstPersonModel(boolean drawing);
}
