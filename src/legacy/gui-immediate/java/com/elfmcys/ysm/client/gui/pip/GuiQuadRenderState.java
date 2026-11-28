/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.client.gui.pip;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;

/**
 * Minecraft before 1.21.6: one flat-coloured GUI quad with arbitrary corners (the animation roulette's fan segments),
 * drawn at once into the GUI's buffers as 1.20 did.
 */
public final class GuiQuadRenderState {
    private GuiQuadRenderState() {}

    /** Draws the quad (corners in GUI coordinates, counter-clockwise as 1.20 emitted them). */
    public static void submit(GuiGraphics graphics, float x0, float y0, float x1, float y1, float x2, float y2,
        float x3, float y3, int color) {
        var matrix = graphics.pose().last().pose();
        //#if MC >= 1.21.2
        graphics.drawSpecial(buffers -> {
            var buffer = buffers.getBuffer(RenderType.gui());
            buffer.addVertex(matrix, x0, y0, 0).setColor(color);
            buffer.addVertex(matrix, x1, y1, 0).setColor(color);
            buffer.addVertex(matrix, x2, y2, 0).setColor(color);
            buffer.addVertex(matrix, x3, y3, 0).setColor(color);
        });
        //#else
        //$$ // before 1.21.2 GuiGraphics hands out its buffers directly
        //$$ var buffer = graphics.bufferSource().getBuffer(RenderType.gui());
        //#if MC >= 1.21
        //$$ buffer.addVertex(matrix, x0, y0, 0).setColor(color);
        //$$ buffer.addVertex(matrix, x1, y1, 0).setColor(color);
        //$$ buffer.addVertex(matrix, x2, y2, 0).setColor(color);
        //$$ buffer.addVertex(matrix, x3, y3, 0).setColor(color);
        //#else
        //$$ // before 1.21 each vertex is ended explicitly
        //$$ buffer.vertex(matrix, x0, y0, 0).color(color).endVertex();
        //$$ buffer.vertex(matrix, x1, y1, 0).color(color).endVertex();
        //$$ buffer.vertex(matrix, x2, y2, 0).color(color).endVertex();
        //$$ buffer.vertex(matrix, x3, y3, 0).color(color).endVertex();
        //#endif
        //$$ graphics.flush();
        //#endif
    }
}
