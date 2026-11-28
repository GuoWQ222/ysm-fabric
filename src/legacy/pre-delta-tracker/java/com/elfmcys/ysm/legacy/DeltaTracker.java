/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.legacy;

/**
 * Minecraft before 1.21 passes the frame's partial tick as a float. This stand-in (mapped from
 * net.minecraft.client.DeltaTracker by versions/below-1.21.remap) carries it to YSM code written against 1.21's
 * DeltaTracker.
 */
public interface DeltaTracker {
    float getGameTimeDeltaTicks();

    float getGameTimeDeltaPartialTick(boolean ignoreFrozenGame);

    float getRealtimeDeltaTicks();

    /** The frame's partial tick (Minecraft.getFrameTime) and tick length (getDeltaFrameTime). */
    static DeltaTracker of(float partialTick) {
        float delta = net.minecraft.client.Minecraft.getInstance().getDeltaFrameTime();
        return new DeltaTracker() {
            @Override
            public float getGameTimeDeltaTicks() {
                return delta;
            }

            @Override
            public float getGameTimeDeltaPartialTick(boolean ignoreFrozenGame) {
                return partialTick;
            }

            @Override
            public float getRealtimeDeltaTicks() {
                return delta;
            }
        };
    }

    /** The current frame's values. */
    static DeltaTracker current() {
        return of(net.minecraft.client.Minecraft.getInstance().getFrameTime());
    }
}
