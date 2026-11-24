/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.mixin.fabric.client;

import com.elfmcys.ysm.platform.event.forge.InputEvent;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Forge's InputEvent.Key is fired at the end of KeyboardHandler.keyPress for the game window; early returns (a screen
 * consumed the key) skip it. TAIL keeps that behaviour.
 */
@Mixin(KeyboardHandler.class)
public abstract class InputEventsMixin {
    @Inject(method = "keyPress", at = @At("TAIL"))
    private void ysm$afterKeyPress(long handle, int action, KeyEvent event, CallbackInfo ci) {
        if (handle == Minecraft.getInstance().getWindow().handle())
            com.elfmcys.ysm.platform.event.YsmEvents.post(new InputEvent.Key(event.key(), com.elfmcys.ysm.platform.McCompat.scancode(event), action, event.modifiers()));
    }
}
