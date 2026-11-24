/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.forge.mixin;

import com.elfmcys.ysm.client.sound.instance.CustomSoundInstance;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Forge 26 has no stream hook on SoundInstance (Fabric: FabricSoundInstance, NeoForge: SoundInstance.getStream). YSM's
 * model sounds are declared streamed (sounds.json "stream": true), so they reach this call, the one Fabric API wraps.
 */
@Mixin(SoundEngine.class)
public abstract class SoundEngineStreamMixin {
    @WrapOperation(method = "play", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/sounds/SoundBufferLibrary;getStream(Lnet/minecraft/resources/Identifier;Z)Ljava/util/concurrent/CompletableFuture;"))
    private CompletableFuture<AudioStream> ysm$customStream(SoundBufferLibrary library, Identifier id, boolean looping,
        Operation<CompletableFuture<AudioStream>> original, @Local(argsOnly = true) SoundInstance instance) {
        if (instance instanceof CustomSoundInstance custom)
            return custom.ysmStream();
        return original.call(library, id, looping);
    }
}
