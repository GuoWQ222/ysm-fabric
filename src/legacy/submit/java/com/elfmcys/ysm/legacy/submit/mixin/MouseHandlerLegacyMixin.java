/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.legacy.submit.mixin;

import com.elfmcys.ysm.platform.event.YsmEvents;
import com.elfmcys.ysm.platform.event.forge.InputEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Minecraft before 1.21.9 (MouseButtonEventsMixin's counterpart): Forge's InputEvent.MouseButton.Post at the end of
 * MouseHandler.onPress, which these versions call with the button numbers instead of a MouseButtonInfo.
 */
@Mixin(MouseHandler.class)
public abstract class MouseHandlerLegacyMixin {
    @Inject(method = "onPress", at = @At("TAIL"))
    private void ysm$afterButton(long handle, int button, int action, int modifiers, CallbackInfo ci) {
        if (handle == Minecraft.getInstance().getWindow().getWindow())
            YsmEvents.post(new InputEvent.MouseButton.Post(button, action, modifiers));
    }
}
