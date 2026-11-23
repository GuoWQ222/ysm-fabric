/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.mixin.fabric.client;

import com.elfmcys.ysm.fabric.FabricEventHooks;
import com.elfmcys.ysm.platform.event.forge.InputEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Forge's InputEvent.MouseButton.Post: end of MouseHandler.onButton for the game window, with the raw button. */
@Mixin(MouseHandler.class)
public abstract class MouseButtonEventsMixin {
    @Inject(method = "onButton", at = @At("TAIL"))
    private void ysm$afterButton(long handle, MouseButtonInfo rawButtonInfo, int action, CallbackInfo ci) {
        if (handle == Minecraft.getInstance().getWindow().handle())
            FabricEventHooks.post(
                new InputEvent.MouseButton.Post(rawButtonInfo.button(), action, rawButtonInfo.modifiers()));
    }
}
