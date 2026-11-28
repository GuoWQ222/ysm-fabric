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
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;

/** Renders {@link YsmGuiModelRenderState} pictures (Fabric port, 26.x picture-in-picture GUI rendering). */
public class YsmGuiModelRenderer extends PictureInPictureRenderer<YsmGuiModelRenderState> {
    /** Diagnostics (test harness): pictures drawn. */
    public static volatile int draws;

    //#if MC < 26.2
    //$$ private final net.minecraft.client.renderer.MultiBufferSource.BufferSource buffers;
    //$$
    //$$ public YsmGuiModelRenderer(net.minecraft.client.renderer.MultiBufferSource.BufferSource buffers) {
    //$$     super(buffers);
    //$$     this.buffers = buffers;
    //$$ }
    //$$
    //#endif
    @Override
    public Class<YsmGuiModelRenderState> getRenderStateClass() {
        return YsmGuiModelRenderState.class;
    }

    @Override
    //#if MC >= 26.2
    protected void renderToTexture(YsmGuiModelRenderState state, PoseStack ignored, SubmitNodeCollector collector) {
    //#else
    //$$ protected void renderToTexture(YsmGuiModelRenderState state, PoseStack ignored) {
    //#if MC >= 1.21.9
    //$$     // 26.1: the renderer collects and draws its own submissions (as vanilla GuiEntityRenderer)
    //$$     var dispatcher = com.elfmcys.ysm.platform.McCompat.featureRenderDispatcher();
    //$$     SubmitNodeCollector collector = dispatcher.getSubmitNodeStorage();
    //#else
    //$$     // before 1.21.9: drawn straight into the picture's buffers
    //$$     SubmitNodeCollector collector = new SubmitNodeCollector(buffers);
    //#endif
    //#endif
        draws++;
        com.elfmcys.ysm.platform.McCompat.lighting().setupFor(Lighting.Entry.ENTITY_IN_UI);
        // 26.2: prepare() pushed the model-view stack and pops it after the submitted draws ran; 26.1 does not,
        // so the renderer brackets its own change (else every later world draw keeps the GUI transform).
        var view = RenderSystem.getModelViewStack();
        //#if MC < 26.2
        //$$ view.pushMatrix();
        //#endif
        float pixelScale = state.pixelScale();
        view.scale(pixelScale, pixelScale, 1.0f).translate(-state.x0(), -state.y0(), 0.0f);
        try {
            state.painter().paint(view, collector);
        } catch (RuntimeException | Error failure) {
            YesSteveModel.LOGGER.error("YSM GUI model drawing failed", failure);
        }
        //#if MC < 26.2
        //$$ try {
        //#if MC >= 1.21.9
        //$$     dispatcher.renderAllFeatures();
        //#else
        //$$     collector.flush();
        //#endif
        //$$     // Draw what is still batched while the GUI transform is on the model-view stack: prepare() flushes the
        //$$     // shared buffers only after this method returns, when the stack is popped, and a model left in the batch
        //$$     // (third-person view) would be drawn without the GUI transform, off the picture.
        //$$     buffers.endBatch();
        //$$ } finally {
        //$$     view.popMatrix();
        //$$ }
        //#endif
    }

    @Override
    protected String getTextureLabel() {
        return "ysm_model";
    }
}
