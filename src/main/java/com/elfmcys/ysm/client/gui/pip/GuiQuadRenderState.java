/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.client.gui.pip;

import com.elfmcys.ysm.mixin.fabric.client.GuiGraphicsExtractorAccessor;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;

/**
 * Fabric port (26.x): one flat-coloured GUI quad with arbitrary corners (the animation roulette's fan segments; 1.20
 * drew them with an immediate POSITION_COLOR buffer).
 */
public record GuiQuadRenderState(Matrix3x2fc pose, float x0, float y0, float x1, float y1, float x2, float y2,
    float x3, float y3, int color, @Nullable ScreenRectangle scissorArea, @Nullable ScreenRectangle bounds)
    implements GuiElementRenderState {
    //#if MC >= 1.21.9
    @Override
    public void buildVertices(VertexConsumer consumer) {
        //#if MC >= 1.21.11
        consumer.addVertexWith2DPose(pose, x0, y0).setColor(color);
        //#else
        //$$ consumer.addVertexWith2DPose(new org.joml.Matrix3x2f(pose), x0, y0).setColor(color);
        //#endif
        //#if MC >= 1.21.11
        consumer.addVertexWith2DPose(pose, x1, y1).setColor(color);
        //#else
        //$$ consumer.addVertexWith2DPose(new org.joml.Matrix3x2f(pose), x1, y1).setColor(color);
        //#endif
        //#if MC >= 1.21.11
        consumer.addVertexWith2DPose(pose, x2, y2).setColor(color);
        //#else
        //$$ consumer.addVertexWith2DPose(new org.joml.Matrix3x2f(pose), x2, y2).setColor(color);
        //#endif
        //#if MC >= 1.21.11
        consumer.addVertexWith2DPose(pose, x3, y3).setColor(color);
        //#else
        //$$ consumer.addVertexWith2DPose(new org.joml.Matrix3x2f(pose), x3, y3).setColor(color);
        //#endif
    }
    //#else
    //$$ // 1.21.6 - 1.21.8: GUI elements are built with a depth
    //$$ @Override
    //$$ public void buildVertices(VertexConsumer consumer, float z) {
    //$$     consumer.addVertexWith2DPose(new org.joml.Matrix3x2f(pose), x0, y0, z).setColor(color);
    //$$     consumer.addVertexWith2DPose(new org.joml.Matrix3x2f(pose), x1, y1, z).setColor(color);
    //$$     consumer.addVertexWith2DPose(new org.joml.Matrix3x2f(pose), x2, y2, z).setColor(color);
    //$$     consumer.addVertexWith2DPose(new org.joml.Matrix3x2f(pose), x3, y3, z).setColor(color);
    //$$ }
    //#endif

    @Override
    public RenderPipeline pipeline() {
        return RenderPipelines.GUI;
    }

    @Override
    public TextureSetup textureSetup() {
        return TextureSetup.noTexture();
    }

    /** Adds the quad (corners in GUI coordinates, counter-clockwise as 1.20 emitted them) to this frame's GUI. */
    public static void submit(GuiGraphicsExtractor graphics, float x0, float y0, float x1, float y1, float x2,
        float y2, float x3, float y3, int color) {
        var accessor = (GuiGraphicsExtractorAccessor)graphics;
        var scissor = GuiScissor.current(graphics);
        var pose = new Matrix3x2f(graphics.pose());
        int minX = (int)Math.floor(Math.min(Math.min(x0, x1), Math.min(x2, x3)));
        int minY = (int)Math.floor(Math.min(Math.min(y0, y1), Math.min(y2, y3)));
        int maxX = (int)Math.ceil(Math.max(Math.max(x0, x1), Math.max(x2, x3)));
        int maxY = (int)Math.ceil(Math.max(Math.max(y0, y1), Math.max(y2, y3)));
        var bounds = new ScreenRectangle(minX, minY, maxX - minX, maxY - minY).transformMaxBounds(pose);
        if (scissor != null)
            bounds = scissor.intersection(bounds);
        accessor.ysm$guiRenderState().addGuiElement(
            new GuiQuadRenderState(pose, x0, y0, x1, y1, x2, y2, x3, y3, color, scissor, bounds));
    }
}
