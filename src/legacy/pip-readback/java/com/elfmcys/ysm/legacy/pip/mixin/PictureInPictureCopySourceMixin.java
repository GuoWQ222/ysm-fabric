/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.legacy.pip.mixin;

import com.elfmcys.ysm.client.gui.pip.YsmGuiModelRenderer;
import com.mojang.blaze3d.textures.GpuTexture;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Minecraft 1.21.6 - 1.21.11 create a picture-in-picture renderer's colour texture without USAGE_COPY_SRC (26.1 added
 * it), and the GPU refuses to copy such a texture into a buffer: the offscreen model preview (PreviewRenderHost reads
 * its picture back) failed with "Texture needs USAGE_COPY_SRC to be a source for a copy". YSM's own pictures get the
 * flag; vanilla's are left as they are.
 */
@Mixin(PictureInPictureRenderer.class)
public abstract class PictureInPictureCopySourceMixin {
    @ModifyArg(method = "prepareTexturesAndProjection", at = @At(value = "INVOKE",
        target = "Lcom/mojang/blaze3d/systems/GpuDevice;createTexture(Ljava/util/function/Supplier;ILcom/mojang/blaze3d/textures/TextureFormat;IIII)Lcom/mojang/blaze3d/textures/GpuTexture;",
        ordinal = 0), index = 1)
    private int ysm$copySource(int usage) {
        return (Object)this instanceof YsmGuiModelRenderer ? usage | GpuTexture.USAGE_COPY_SRC : usage;
    }
}
