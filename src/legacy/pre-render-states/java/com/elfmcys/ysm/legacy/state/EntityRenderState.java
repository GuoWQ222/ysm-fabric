/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.legacy.state;

import com.elfmcys.ysm.platform.render.YsmRenderStateData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Minecraft before 1.21.2 renders entities straight from the entity; YSM's renderers are written against 1.21.2+
 * render states. This stand-in (mapped from net.minecraft.client.renderer.entity.state by versions/below-1.21.2.remap)
 * carries the fields YSM reads, filled by {@link StateExtractor} the way vanilla's extraction fills them, and YSM's own
 * per-state data (the 1.21.2+ builds add that with EntityRenderStateDataMixin).
 */
public class EntityRenderState implements YsmRenderStateData {
    /** 1.21.2's leash state: here the entity and its holder (the entity's renderer draws the leash from them). */
    public static final class LeashState {
        public Entity entity;
        public Entity holder;
        public float partialTick;
    }

    public double x;
    public double y;
    public double z;
    public float ageInTicks;
    public float boundingBoxWidth;
    public float boundingBoxHeight;
    public float eyeHeight;
    public double distanceToCameraSq;
    public boolean isInvisible;
    public boolean isDiscrete;
    public boolean displayFireAnimation;
    public @Nullable Component nameTag;
    public @Nullable Vec3 nameTagAttachment;
    public @Nullable LeashState leashState;

    private @Nullable Entity ysmEntity;
    private float ysmPartialTick = 1.0f;
    private boolean ysmReplaced;
    private int ysmLight = 0xF000F0;
    private int ysmOutlineColor;

    @Override
    public @Nullable Entity ysm$entity() {
        return ysmEntity;
    }

    @Override
    public void ysm$setEntity(@Nullable Entity entity) {
        ysmEntity = entity;
    }

    @Override
    public float ysm$partialTick() {
        return ysmPartialTick;
    }

    @Override
    public void ysm$setPartialTick(float partialTick) {
        ysmPartialTick = partialTick;
    }

    @Override
    public boolean ysm$replaced() {
        return ysmReplaced;
    }

    @Override
    public void ysm$setReplaced(boolean replaced) {
        ysmReplaced = replaced;
    }

    @Override
    public int ysm$light() {
        return ysmLight;
    }

    @Override
    public void ysm$setLight(int light) {
        ysmLight = light;
    }

    @Override
    public int ysm$outlineColor() {
        return ysmOutlineColor;
    }

    @Override
    public void ysm$setOutlineColor(int color) {
        ysmOutlineColor = color;
    }

    /** First-person Model flag at extraction (read on 1.21.9+ only; kept for the shared interface). */
    private boolean ysmFirstPersonModel;

    @Override
    public boolean ysm$firstPersonModel() {
        return ysmFirstPersonModel;
    }

    @Override
    public void ysm$setFirstPersonModel(boolean drawing) {
        ysmFirstPersonModel = drawing;
    }
}
