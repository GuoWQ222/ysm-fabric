/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.client.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
//#if MC >= 26.2
import com.mojang.blaze3d.GpuFormat;
//#else
//$$ import com.mojang.blaze3d.textures.TextureFormat;
//#endif
import java.io.IOException;
import java.nio.ByteBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.server.packs.resources.ResourceManager;
import org.lwjgl.system.MemoryUtil;

/**
 * Fabric port (26.x): textures are GpuTexture objects created through the GpuDevice. YSM textures keep 1.20's
 * load-on-register shape: {@link #register} adds the texture to the texture manager and then loads it.
 */
public abstract class YsmTexture extends AbstractTexture {
    /** Usage flags as vanilla's DynamicTexture: copy destination and texture binding. */
    private static final int USAGE = 5;
    private int width = -1;
    private int height = -1;

    /** Loads (or reloads) the pixels; render thread. */
    public abstract void load(ResourceManager resourceManager) throws IOException;

    /** Creates the GPU texture for this size if needed and uploads the image (RGBA). */
    protected final void uploadPixels(NativeImage pixels, String label) {
        RenderSystem.assertOnRenderThread();
        if (texture == null || width != pixels.getWidth() || height != pixels.getHeight()) {
            //#if MC >= 26.2
            releaseTextures();
            //#else
            //$$ if (texture != null) {
            //$$     texture.close();
            //$$     texture = null;
            //$$ }
            //#if MC >= 1.21.6
            //$$ if (textureView != null) {
            //$$     textureView.close();
            //$$     textureView = null;
            //$$ }
            //#endif
            //#endif
            var device = RenderSystem.getDevice();
            //#if MC >= 26.2
            texture = device.createTexture(() -> label, USAGE, GpuFormat.RGBA8_UNORM, pixels.getWidth(),
                pixels.getHeight(), 1, 1);
            //#else
            //#if MC >= 1.21.6
            //$$ texture = device.createTexture(() -> label, USAGE, TextureFormat.RGBA8, pixels.getWidth(),
            //$$     pixels.getHeight(), 1, 1);
            //#else
            //$$ // 1.21.5: no usage flags, no texture views
            //$$ texture = device.createTexture(() -> label, TextureFormat.RGBA8, pixels.getWidth(), pixels.getHeight(), 1);
            //#endif
            //#endif
            //#if MC >= 1.21.6
            textureView = device.createTextureView(texture);
            //#endif
            //#if MC >= 1.21.11
            sampler = RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST);
            //#else
            //$$ // before 1.21.11 the texture carries its own sampling state
            //$$ texture.setAddressMode(com.mojang.blaze3d.textures.AddressMode.REPEAT);
            //$$ texture.setTextureFilter(FilterMode.NEAREST, false);
            //#endif
            width = pixels.getWidth();
            height = pixels.getHeight();
        }
        RenderSystem.getDevice().createCommandEncoder().writeToTexture(texture, pixels);
    }

    /** Uploads tightly packed RGBA bytes (row length = width). */
    protected final void uploadRgba(ByteBuffer rgba, int width, int height, String label) {
        try (var image = new NativeImage(NativeImage.Format.RGBA, width, height, false)) {
            long bytes = (long)width * height * 4;
            if (rgba.remaining() < bytes)
                throw new IllegalArgumentException("Pixel buffer too small");
            MemoryUtil.memCopy(MemoryUtil.memAddress(rgba), image.getPointer(), bytes);
            uploadPixels(image, label);
        }
    }

    /** Model texture upload (label for GPU debuggers). */
    final void uploadModelPixels(NativeImage pixels) {
        uploadPixels(pixels, "ysm:model_texture");
    }

    public boolean isUploaded() {
        return texture != null;
    }

    /** Registers and loads, as 1.20's TextureManager.register did. */
    public static void register(net.minecraft.resources.Identifier id, YsmTexture texture) throws IOException {
        var manager = Minecraft.getInstance().getTextureManager();
        manager.register(id, texture);
        texture.load(Minecraft.getInstance().getResourceManager());
    }
}
