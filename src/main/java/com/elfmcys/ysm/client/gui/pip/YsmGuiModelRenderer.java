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
    @Override
    public Class<YsmGuiModelRenderState> getRenderStateClass() {
        return YsmGuiModelRenderState.class;
    }

    @Override
    protected void renderToTexture(YsmGuiModelRenderState state, PoseStack ignored, SubmitNodeCollector collector) {
        Minecraft.getInstance().gameRenderer.lighting().setupFor(Lighting.Entry.ENTITY_IN_UI);
        // prepare() pushed the model-view stack and pops it after the submitted draws ran.
        var view = RenderSystem.getModelViewStack();
        float pixelScale = state.pixelScale();
        view.scale(pixelScale, pixelScale, 1.0f).translate(-state.x0(), -state.y0(), 0.0f);
        try {
            state.painter().paint(view, collector);
        } catch (RuntimeException | Error failure) {
            YesSteveModel.LOGGER.error("YSM GUI model drawing failed", failure);
        }
    }

    @Override
    protected String getTextureLabel() {
        return "ysm_model";
    }
}
