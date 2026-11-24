/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.mixin.fabric.client;

import com.elfmcys.ysm.platform.render.YsmRenderStateData;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** Stores YSM's per-state data on vanilla render states (works on every loader; replaced Fabric's data keys). */
@Mixin(EntityRenderState.class)
public abstract class EntityRenderStateDataMixin implements YsmRenderStateData {
    @Unique
    private @Nullable Entity ysm$entity;
    @Unique
    private float ysm$partialTick = 1.0f;
    @Unique
    private boolean ysm$replaced;

    @Override
    public @Nullable Entity ysm$entity() {
        return ysm$entity;
    }

    @Override
    public void ysm$setEntity(@Nullable Entity entity) {
        ysm$entity = entity;
    }

    @Override
    public float ysm$partialTick() {
        return ysm$partialTick;
    }

    @Override
    public void ysm$setPartialTick(float partialTick) {
        ysm$partialTick = partialTick;
    }

    @Override
    public boolean ysm$replaced() {
        return ysm$replaced;
    }

    @Override
    public void ysm$setReplaced(boolean replaced) {
        ysm$replaced = replaced;
    }
}
