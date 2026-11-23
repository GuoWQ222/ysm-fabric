package com.elfmcys.ysm.mixin.fabric;

import com.mojang.blaze3d.platform.NativeImage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Raw pixel pointer and byte size of a NativeImage, handed to the native codec. */
@Mixin(NativeImage.class)
public interface NativeImageAccessor {
    @Accessor("pixels")
    long ysm$pixels();

    @Accessor("size")
    long ysm$size();
}
