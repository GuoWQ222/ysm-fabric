/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.client.gui.pip;

import com.elfmcys.ysm.mixin.fabric.client.GuiGraphicsExtractorAccessor;
import net.fabricmc.fabric.api.client.rendering.v1.PictureInPictureRendererRegistry;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix3x2f;

/** Entry points for YSM's GUI 3D drawing (Fabric port, 26.x). */
public final class YsmGuiModels {
    private YsmGuiModels() {}

    /** Client init: registers the picture-in-picture renderer. */
    public static void register() {
        PictureInPictureRendererRegistry.register(context -> new YsmGuiModelRenderer());
    }

    /**
     * Submits a 3D drawing clipped to the GUI rectangle [x0, x1) x [y0, y1); the painter runs later in this frame's
     * GUI render.
     */
    public static void submit(GuiGraphicsExtractor graphics, int x0, int y0, int x1, int y1,
        YsmGuiModelRenderState.Painter painter) {
        if (x1 <= x0 || y1 <= y0)
            return;
        var accessor = (GuiGraphicsExtractorAccessor)graphics;
        var scissor = GuiScissor.current(graphics);
        accessor.ysm$guiRenderState().addPicturesInPictureState(
            YsmGuiModelRenderState.create(new Matrix3x2f(graphics.pose()), x0, y0, x1, y1, scissor,
                (float)net.minecraft.client.Minecraft.getInstance().getWindow().getGuiScale(), painter));
    }
}
