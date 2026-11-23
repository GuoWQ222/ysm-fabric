/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.mixin.fabric.client;

import com.elfmcys.ysm.fabric.FabricEventHooks;
import com.elfmcys.ysm.platform.event.forge.ScreenEvent;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Forge's ScreenEvent.Opening (cancelable, may replace the screen) fires before the old screen is removed. On 26.2
 * screens are set through Gui.setScreen, which calls removed() first, so the event wraps the whole method.
 */
@Mixin(Gui.class)
public abstract class GuiScreenOpeningMixin {
    @WrapMethod(method = "setScreen")
    private void ysm$screenOpening(@Nullable Screen screen, Operation<Void> original) {
        if (screen != null) {
            var event = new ScreenEvent.Opening(((Gui)(Object)this).screen(), screen);
            if (FabricEventHooks.post(event))
                return;
            screen = event.getNewScreen();
        }
        original.call(screen);
    }
}
