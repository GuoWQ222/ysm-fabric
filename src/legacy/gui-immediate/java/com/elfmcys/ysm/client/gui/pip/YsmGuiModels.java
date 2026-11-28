/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.client.gui.pip;

import com.elfmcys.ysm.YesSteveModel;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.SubmitNodeCollector;

/**
 * Entry points for YSM's GUI 3D drawing, Minecraft before 1.21.6: GuiGraphics draws immediately, so a drawing is
 * painted on the spot, clipped to its rectangle, under the GUI pose (as 1.20's YSM and vanilla's inventory entity did).
 */
public final class YsmGuiModels {
    private YsmGuiModels() {}

    /** Diagnostics (test harness): drawings painted. */
    private static int draws;

    public static int draws() {
        return draws;
    }

    /** Client init: nothing to register before 1.21.6. */
    public static void register() {}

    /** The GUI entity lights for YSM's GUI pose (its depth flip is in the pose, so normals are flipped too). */
    public static void setupEntityLighting() {
        //#if MC >= 1.20.5
        Lighting.setupForEntityInInventory();
        //#else
        //$$ // 1.20.5 moved vanilla's GUI depth flip into the pose (flipping normals, until then only positions were
        //$$ // flipped) and mirrored the first inventory light (z -1 -> 1) to keep the look. YSM's pose flips normals on
        //$$ // every version (McCompat.flipDepth), so it needs 1.20.5's lights: with the older ones its GUI models came
        //$$ // out darker than on 1.20.5+.
        //$$ RenderSystem.setShaderLights(new org.joml.Vector3f(0.2F, -1.0F, 1.0F).normalize(),
        //$$     new org.joml.Vector3f(-0.2F, -1.0F, 0.0F).normalize());
        //#endif
    }

    /** Paints a 3D drawing clipped to the GUI rectangle [x0, x1) x [y0, y1). */
    public static void submit(GuiGraphics graphics, int x0, int y0, int x1, int y1,
        YsmGuiModelRenderState.Painter painter) {
        x0 = Math.max(0, x0);
        y0 = Math.max(0, y0);
        if (x1 <= x0 || y1 <= y0)
            return;
        graphics.flush();
        graphics.enableScissor(x0, y0, x1, y1);
        //#if MC >= 1.20.5
        var view = RenderSystem.getModelViewStack();
        view.pushMatrix();
        //#else
        //$$ // before 1.20.5 the model-view stack is a PoseStack: the painter works on a matrix stack copied from it
        //$$ var poseView = RenderSystem.getModelViewStack();
        //$$ poseView.pushPose();
        //$$ var view = new org.joml.Matrix4fStack(16);
        //$$ view.set(poseView.last().pose());
        //#endif
        try {
            view.mul(graphics.pose().last().pose());
            setupEntityLighting();
            var buffers = graphics.getBufferSource();
            draws++;
            try (var collector = new SubmitNodeCollector(buffers)) {
                painter.paint(view, collector);
                //#if MC < 1.20.5
                //$$ // before the collector draws: the native writer reads the model-view
                //$$ poseView.last().pose().set(view);
                //#endif
            } catch (RuntimeException | Error failure) {
                YesSteveModel.LOGGER.error("YSM GUI model drawing failed", failure);
            }
            //#if MC < 1.21.2
            //$$ // before 1.21.2 the shaders see model-view changes (the GUI pose and the painter's own placement) only
            //$$ // once applied
            //$$ RenderSystem.applyModelViewMatrix();
            //#endif
            buffers.endBatch();
        } finally {
            //#if MC >= 1.20.5
            view.popMatrix();
            //#else
            //$$ poseView.popPose();
            //#endif
            //#if MC < 1.21.2
            //$$ RenderSystem.applyModelViewMatrix();
            //#endif
            Lighting.setupFor3DItems();
            graphics.disableScissor();
        }
    }
}
