/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.mixin.fabric.client;

import com.mojang.blaze3d.textures.GpuTexture;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** The colour texture a picture-in-picture renderer drew into (model preview readback). */
@Mixin(PictureInPictureRenderer.class)
public interface PictureInPictureRendererAccessor {
    @Accessor("texture")
    @Nullable
    GpuTexture ysm$texture();
}
