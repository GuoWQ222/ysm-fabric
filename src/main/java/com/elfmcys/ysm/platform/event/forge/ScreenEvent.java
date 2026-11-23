/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.platform.event.forge;

import com.elfmcys.ysm.platform.event.Cancelable;
import com.elfmcys.ysm.platform.event.Event;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

/** Fabric port: Forge screen events. */
public class ScreenEvent extends Event {
    @Cancelable
    public static class Opening extends ScreenEvent {
        private final @Nullable Screen currentScreen;
        private @Nullable Screen newScreen;

        public Opening(@Nullable Screen currentScreen, @Nullable Screen newScreen) {
            this.currentScreen = currentScreen;
            this.newScreen = newScreen;
        }

        public @Nullable Screen getCurrentScreen() {
            return currentScreen;
        }

        public @Nullable Screen getNewScreen() {
            return newScreen;
        }

        public void setNewScreen(@Nullable Screen newScreen) {
            this.newScreen = newScreen;
        }
    }
}
