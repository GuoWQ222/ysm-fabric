/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.client.gui;

import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

/**
 * Fabric port (26.x): 1.20 GuiGraphics drawing calls on top of GuiGraphicsExtractor. Text colours keep 1.20's rule
 * that a colour without alpha bits is opaque (26.x draws alpha 0 as invisible).
 */
public final class GuiDraw {
    private GuiDraw() {}

    /** 1.20 Font rule: alpha bits all clear means opaque. */
    public static int argb(int color) {
        return (color & 0xFC000000) == 0 ? color | 0xFF000000 : color;
    }

    // ---- text ----

    public static int drawString(GuiGraphicsExtractor g, Font font, String text, int x, int y, int color) {
        return drawString(g, font, text, x, y, color, true);
    }

    public static int drawString(GuiGraphicsExtractor g, Font font, String text, int x, int y, int color,
        boolean shadow) {
        if (text == null)
            return x;
        //#if MC >= 26.1
        g.text(font, text, x, y, argb(color), shadow);
        //#else
        //$$ g.drawString(font, text, x, y, argb(color), shadow);
        //#endif
        return x + font.width(text) + (shadow ? 1 : 0);
    }

    public static int drawString(GuiGraphicsExtractor g, Font font, String text, float x, float y, int color,
        boolean shadow) {
        return drawString(g, font, text, (int)x, (int)y, color, shadow);
    }

    public static int drawString(GuiGraphicsExtractor g, Font font, Component text, int x, int y, int color) {
        return drawString(g, font, text, x, y, color, true);
    }

    public static int drawString(GuiGraphicsExtractor g, Font font, Component text, int x, int y, int color,
        boolean shadow) {
        //#if MC >= 26.1
        g.text(font, text, x, y, argb(color), shadow);
        //#else
        //$$ g.drawString(font, text, x, y, argb(color), shadow);
        //#endif
        return x + font.width(text) + (shadow ? 1 : 0);
    }

    public static int drawString(GuiGraphicsExtractor g, Font font, FormattedCharSequence text, int x, int y,
        int color) {
        return drawString(g, font, text, x, y, color, true);
    }

    public static int drawString(GuiGraphicsExtractor g, Font font, FormattedCharSequence text, int x, int y,
        int color, boolean shadow) {
        //#if MC >= 26.1
        g.text(font, text, x, y, argb(color), shadow);
        //#else
        //$$ g.drawString(font, text, x, y, argb(color), shadow);
        //#endif
        return x + font.width(text) + (shadow ? 1 : 0);
    }

    public static int drawString(GuiGraphicsExtractor g, Font font, FormattedCharSequence text, float x, float y,
        int color, boolean shadow) {
        return drawString(g, font, text, (int)x, (int)y, color, shadow);
    }

    public static void drawCenteredString(GuiGraphicsExtractor g, Font font, String text, int x, int y, int color) {
        //#if MC >= 26.1
        g.centeredText(font, text, x, y, argb(color));
        //#else
        //$$ g.drawCenteredString(font, text, x, y, argb(color));
        //#endif
    }

    public static void drawCenteredString(GuiGraphicsExtractor g, Font font, Component text, int x, int y,
        int color) {
        //#if MC >= 26.1
        g.centeredText(font, text, x, y, argb(color));
        //#else
        //$$ g.drawCenteredString(font, text, x, y, argb(color));
        //#endif
    }

    public static void drawCenteredString(GuiGraphicsExtractor g, Font font, FormattedCharSequence text, int x, int y,
        int color) {
        //#if MC >= 26.1
        g.centeredText(font, text, x, y, argb(color));
        //#else
        //$$ g.drawCenteredString(font, text, x, y, argb(color));
        //#endif
    }

    public static void drawWordWrap(GuiGraphicsExtractor g, Font font, FormattedText text, int x, int y, int width,
        int color) {
        //#if MC >= 26.1
        g.textWithWordWrap(font, text, x, y, width, argb(color), false);
        //#else
        //#if MC >= 1.21.4
        //$$ g.drawWordWrap(font, text, x, y, width, argb(color), false);
        //#else
        //$$ g.drawWordWrap(font, text, x, y, width, argb(color));
        //#endif
        //#endif
    }

    // ---- tooltips (26.x: deferred to the end of the frame) ----

    public static void renderTooltip(GuiGraphicsExtractor g, Font font, Component text, int x, int y) {
        g.setTooltipForNextFrame(font, text, x, y);
    }

    public static void renderTooltip(GuiGraphicsExtractor g, Font font, List<? extends FormattedCharSequence> lines,
        int x, int y) {
        g.setTooltipForNextFrame(font, lines, x, y);
    }

    public static void renderTooltip(GuiGraphicsExtractor g, Font font, List<Component> lines,
        Optional<TooltipComponent> image, int x, int y) {
        g.setTooltipForNextFrame(font, lines, image, x, y);
    }

    public static void renderComponentTooltip(GuiGraphicsExtractor g, Font font, List<Component> lines, int x, int y) {
        g.setComponentTooltipForNextFrame(font, lines, x, y);
    }

    // ---- lines ----

    public static void hLine(GuiGraphicsExtractor g, int x0, int x1, int y, int color) {
        //#if MC >= 26.1
        g.horizontalLine(x0, x1, y, color);
        //#else
        //$$ g.hLine(x0, x1, y, color);
        //#endif
    }

    public static void vLine(GuiGraphicsExtractor g, int x, int y0, int y1, int color) {
        //#if MC >= 26.1
        g.verticalLine(x, y0, y1, color);
        //#else
        //$$ g.vLine(x, y0, y1, color);
        //#endif
    }

    // ---- textures (1.20 overloads by argument count) ----

    /** blit(texture, x, y, u, v, w, h) on a 256x256 texture. */
    public static void blit(GuiGraphicsExtractor g, Identifier texture, int x, int y, int u, int v, int width,
        int height) {
        g.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, width, height, 256, 256);
    }

    /** blit(texture, x, y, u, v, w, h, textureWidth, textureHeight). */
    public static void blit(GuiGraphicsExtractor g, Identifier texture, int x, int y, float u, float v, int width,
        int height, int textureWidth, int textureHeight) {
        g.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, width, height, textureWidth, textureHeight);
    }

    /** blit(texture, x, y, z, u, v, w, h, textureWidth, textureHeight); z is ignored (26.x strata order GUI draws). */
    public static void blit(GuiGraphicsExtractor g, Identifier texture, int x, int y, int z, float u, float v,
        int width, int height, int textureWidth, int textureHeight) {
        g.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, width, height, textureWidth, textureHeight);
    }

    /** blit(texture, x, y, w, h, u, v, srcWidth, srcHeight, textureWidth, textureHeight). */
    public static void blit(GuiGraphicsExtractor g, Identifier texture, int x, int y, int width, int height, float u,
        float v, int srcWidth, int srcHeight, int textureWidth, int textureHeight) {
        //#if MC >= 1.21.2
        g.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, width, height, srcWidth, srcHeight, textureWidth,
            textureHeight);
        //#else
        //$$ // before 1.21.2 the size comes before the source position
        //$$ g.blit(texture, x, y, width, height, u, v, srcWidth, srcHeight, textureWidth, textureHeight);
        //#endif
    }

    /**
     * Nine-slice: corners drawn once, edges and centre tiled from the source region (u, v, srcWidth x srcHeight) of a
     * 256x256 texture. Replaces Forge's GuiGraphics#blitWithBorder.
     */
    public static void blitWithBorder(GuiGraphicsExtractor g, Identifier texture, int x, int y, int u, int v,
        int width, int height, int srcWidth, int srcHeight, int top, int bottom, int left, int right) {
        int srcMidW = srcWidth - left - right;
        int srcMidH = srcHeight - top - bottom;
        int dstMidW = width - left - right;
        int dstMidH = height - top - bottom;
        if (srcMidW <= 0 || srcMidH <= 0 || dstMidW < 0 || dstMidH < 0) {
            blit(g, texture, x, y, u, v, Math.min(width, srcWidth), Math.min(height, srcHeight));
            return;
        }
        // corners
        blit(g, texture, x, y, u, v, left, top);
        blit(g, texture, x + left + dstMidW, y, u + left + srcMidW, v, right, top);
        blit(g, texture, x, y + top + dstMidH, u, v + top + srcMidH, left, bottom);
        blit(g, texture, x + left + dstMidW, y + top + dstMidH, u + left + srcMidW, v + top + srcMidH, right, bottom);
        // top and bottom edges, centre
        for (int dx = 0; dx < dstMidW; dx += srcMidW) {
            int w = Math.min(srcMidW, dstMidW - dx);
            blit(g, texture, x + left + dx, y, u + left, v, w, top);
            blit(g, texture, x + left + dx, y + top + dstMidH, u + left, v + top + srcMidH, w, bottom);
            for (int dy = 0; dy < dstMidH; dy += srcMidH)
                blit(g, texture, x + left + dx, y + top + dy, u + left, v + top, w, Math.min(srcMidH, dstMidH - dy));
        }
        // left and right edges
        for (int dy = 0; dy < dstMidH; dy += srcMidH) {
            int h = Math.min(srcMidH, dstMidH - dy);
            blit(g, texture, x, y + top + dy, u, v + top, left, h);
            blit(g, texture, x + left + dstMidW, y + top + dy, u + left + srcMidW, v + top, right, h);
        }
    }
}
