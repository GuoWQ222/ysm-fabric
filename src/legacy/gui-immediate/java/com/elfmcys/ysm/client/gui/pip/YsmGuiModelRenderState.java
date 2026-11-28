/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.client.gui.pip;

import net.minecraft.client.renderer.SubmitNodeCollector;
import org.joml.Matrix4fStack;

/**
 * Minecraft before 1.21.6 (no picture-in-picture GUI rendering): YSM's 3D GUI drawings are painted right away by
 * {@link YsmGuiModels#submit}; this keeps the painter type the GUI code shares with 1.21.6+.
 */
public final class YsmGuiModelRenderState {
    private YsmGuiModelRenderState() {}

    @FunctionalInterface
    public interface Painter {
        /**
         * Render thread, GUI render. {@code view} is the model-view stack mapping absolute GUI coordinates (z as in
         * 1.20's GUI); the painter may multiply onto it (it is restored afterwards). Draws are submitted to
         * {@code collector} and executed right after the painter returns.
         */
        void paint(Matrix4fStack view, SubmitNodeCollector collector);
    }
}
