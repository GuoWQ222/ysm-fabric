/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.client.gui.pip;

import com.elfmcys.ysm.mixin.fabric.client.GuiGraphicsExtractorAccessor;
//#if LOADER == fabric
import net.fabricmc.fabric.api.client.rendering.v1.PictureInPictureRendererRegistry;
//#endif
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix3x2f;

/** Entry points for YSM's GUI 3D drawing (Fabric port, 26.x). */
public final class YsmGuiModels {
    private YsmGuiModels() {}

    /** Dev diagnostics (YSM_DEBUG_PIP): logs the first submissions' bounds, clip and pose. */
    private static final boolean DEBUG = System.getenv("YSM_DEBUG_PIP") != null;
    private static int debugBudget = 40;

    /** Diagnostics (test harness): pictures drawn. */
    public static int draws() {
        return YsmGuiModelRenderer.draws;
    }

    /** Client init: registers the picture-in-picture renderer. */
    public static void register() {
        //#if LOADER == fabric && MC >= 26.2
        PictureInPictureRendererRegistry.register(context -> new YsmGuiModelRenderer());
        //#elseif LOADER == fabric
        //$$ PictureInPictureRendererRegistry.register(context -> new YsmGuiModelRenderer(context.bufferSource()));
        //#elseif LOADER == neoforge && MC >= 26.2
        //$$ com.elfmcys.ysm.neoforge.YsmNeoForge.modBus().addListener(
        //$$     (net.neoforged.neoforge.client.event.RegisterPictureInPictureRenderersEvent event) ->
        //$$         event.register(YsmGuiModelRenderState.class, () -> new YsmGuiModelRenderer()));
        //#elseif LOADER == neoforge
        //$$ com.elfmcys.ysm.neoforge.YsmNeoForge.modBus().addListener(
        //$$     (net.neoforged.neoforge.client.event.RegisterPictureInPictureRenderersEvent event) ->
        //$$         event.register(YsmGuiModelRenderState.class, YsmGuiModelRenderer::new));
        //#else
        //$$ com.elfmcys.ysm.forge.ForgeClientEventHooks.registerPictureRenderer();
        //#endif
    }

    /**
     * Submits a 3D drawing clipped to the GUI rectangle [x0, x1) x [y0, y1); the painter runs later in this frame's
     * GUI render.
     */
    public static void submit(GuiGraphicsExtractor graphics, int x0, int y0, int x1, int y1,
        YsmGuiModelRenderState.Painter painter) {
        // Rectangles reaching past the screen's top-left (the HUD player preview at its default position starts at
        // -30,-30) keep only their on-screen part: the renderer draws relative to x0/y0, so the model does not move
        // and the off-screen part of the texture is not allocated.
        x0 = Math.max(0, x0);
        y0 = Math.max(0, y0);
        if (x1 <= x0 || y1 <= y0)
            return;
        var accessor = (GuiGraphicsExtractorAccessor)graphics;
        var scissor = GuiScissor.current(graphics);
        if (DEBUG && debugBudget > 0) {
            debugBudget--;
            com.elfmcys.ysm.YesSteveModel.LOGGER.info("[pip] submit bounds=({},{})-({},{}) scissor={} pose={} screen={}",
                x0, y0, x1, y1, scissor, graphics.pose(), com.elfmcys.ysm.platform.McCompat.screen());
        }
        accessor.ysm$guiRenderState().addPicturesInPictureState(
            YsmGuiModelRenderState.create(new Matrix3x2f(graphics.pose()), x0, y0, x1, y1, scissor,
                (float)net.minecraft.client.Minecraft.getInstance().getWindow().getGuiScale(), painter));
    }
}
