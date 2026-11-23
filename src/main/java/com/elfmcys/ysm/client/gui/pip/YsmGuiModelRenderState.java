/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.client.gui.pip;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Matrix4fStack;

/**
 * Fabric port (26.x): one YSM 3D drawing inside a GUI (model previews, paper doll). GUI rendering of 3D content goes
 * through picture-in-picture renderers; the painter runs at GUI render time with 1.20-style GUI coordinates.
 */
public record YsmGuiModelRenderState(Matrix3x2f pose, int x0, int y0, int x1, int y1,
    @Nullable ScreenRectangle scissorArea, @Nullable ScreenRectangle bounds, float pixelScale, Painter painter)
    implements PictureInPictureRenderState {
    @Override
    public float scale() {
        return 1.0f;
    }

    @FunctionalInterface
    public interface Painter {
        /**
         * Render thread, GUI render phase. {@code view} is the model-view stack mapping absolute GUI coordinates (z as
         * in 1.20's GUI) into the picture; the painter may multiply onto it (it is restored afterwards). Draws are
         * submitted to {@code collector} and executed right after the painter returns.
         */
        void paint(Matrix4fStack view, SubmitNodeCollector collector);
    }

    /** pixelScale: picture pixels per GUI unit (the GUI scale for on-screen pictures). */
    public static YsmGuiModelRenderState create(Matrix3x2f pose, int x0, int y0, int x1, int y1,
        @Nullable ScreenRectangle scissorArea, float pixelScale, Painter painter) {
        return new YsmGuiModelRenderState(pose, x0, y0, x1, y1, scissorArea,
            PictureInPictureRenderState.getBounds(x0, y0, x1, y1, scissorArea), pixelScale, painter);
    }
}
