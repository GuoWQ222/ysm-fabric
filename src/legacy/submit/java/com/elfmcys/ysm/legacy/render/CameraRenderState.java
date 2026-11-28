/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.legacy.render;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

/**
 * Minecraft before 1.21.9 has no camera render state: this stand-in (mapped from
 * net.minecraft.client.renderer.state.CameraRenderState by versions/below-1.21.9.remap) carries the same fields,
 * filled from the game camera.
 */
public class CameraRenderState {
    public BlockPos blockPos = BlockPos.ZERO;
    public Vec3 pos = Vec3.ZERO;
    public boolean initialized;
    public Vec3 entityPos = Vec3.ZERO;
    public Quaternionf orientation = new Quaternionf();

    /** The main camera as the entity renderers see it this frame. */
    public static CameraRenderState current() {
        var state = new CameraRenderState();
        var minecraft = Minecraft.getInstance();
        Camera camera = minecraft.gameRenderer.getMainCamera();
        state.initialized = camera.isInitialized();
        state.pos = camera.getPosition();
        state.blockPos = camera.getBlockPosition();
        if (camera.getEntity() != null)
            state.entityPos = camera.getEntity().position();
        state.orientation = new Quaternionf(minecraft.getEntityRenderDispatcher().cameraOrientation());
        return state;
    }
}
