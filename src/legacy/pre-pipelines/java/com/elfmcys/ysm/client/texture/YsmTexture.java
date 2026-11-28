/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.client.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.IOException;
import java.nio.ByteBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.server.packs.resources.ResourceManager;

/**
 * Minecraft before 1.21.5 (no GpuDevice): YSM textures are plain OpenGL textures of the AbstractTexture's id, sized
 * with TextureUtil.prepareImage and filled with NativeImage.upload, as 1.20's YSM did. Same API as the 1.21.5+ class:
 * {@link #register} adds the texture to the texture manager and then loads it.
 */
public abstract class YsmTexture extends AbstractTexture {
    private int width = -1;
    private int height = -1;
    private boolean uploaded;

    /** Loads (or reloads) the pixels; render thread. */
    public abstract void load(ResourceManager resourceManager) throws IOException;

    /** Sizes the texture if needed and uploads the image (RGBA), nearest filtering, repeat (as 1.20's YSM). */
    protected final void uploadPixels(NativeImage pixels, String label) {
        RenderSystem.assertOnRenderThread();
        if (!uploaded || width != pixels.getWidth() || height != pixels.getHeight()) {
            TextureUtil.prepareImage(getId(), 0, pixels.getWidth(), pixels.getHeight());
            width = pixels.getWidth();
            height = pixels.getHeight();
            uploaded = true;
        }
        bind();
        setFilter(false, false);
        //#if MC >= 1.21.4
        setClamp(false);
        //#endif
        pixels.upload(0, 0, 0, false);
    }

    /** Uploads tightly packed RGBA bytes (row length = width). */
    protected final void uploadRgba(ByteBuffer rgba, int width, int height, String label) {
        try (var image = new NativeImage(NativeImage.Format.RGBA, width, height, false)) {
            long bytes = (long)width * height * 4;
            if (rgba.remaining() < bytes)
                throw new IllegalArgumentException("Pixel buffer too small");
            // NativeImage keeps its pixel pointer private here: copy pixel by pixel (RGBA bytes = ABGR ints)
            var source = rgba.duplicate().order(java.nio.ByteOrder.LITTLE_ENDIAN);
            int base = source.position();
            for (int y = 0; y < height; y++)
                for (int x = 0; x < width; x++) {
                    int abgr = source.getInt(base + (y * width + x) * 4);
                    //#if MC >= 1.21.2
                    image.setPixel(x, y, (abgr & 0xFF00FF00) | ((abgr & 0xFF) << 16) | ((abgr >>> 16) & 0xFF));
                    //#else
                    //$$ // before 1.21.2 setPixelRGBA takes the native ABGR int
                    //$$ image.setPixelRGBA(x, y, abgr);
                    //#endif
                }
            uploadPixels(image, label);
        }
    }

    /** Model texture upload (label for GPU debuggers). */
    final void uploadModelPixels(NativeImage pixels) {
        uploadPixels(pixels, "ysm:model_texture");
    }

    public boolean isUploaded() {
        return uploaded;
    }

    @Override
    public void releaseId() {
        super.releaseId();
        uploaded = false;
        width = -1;
        height = -1;
    }

    /** Registers and loads, as 1.20's TextureManager.register did. */
    public static void register(net.minecraft.resources.Identifier id, YsmTexture texture) throws IOException {
        var manager = Minecraft.getInstance().getTextureManager();
        manager.register(id, texture);
        texture.load(Minecraft.getInstance().getResourceManager());
    }
}
