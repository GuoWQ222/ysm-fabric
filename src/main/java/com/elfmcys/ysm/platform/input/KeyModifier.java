/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.platform.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

/** Fabric port: Forge's key modifiers. Vanilla key mappings have none, so YSM checks them itself. */
public enum KeyModifier {
    CONTROL(GLFW.GLFW_KEY_LEFT_CONTROL, GLFW.GLFW_KEY_RIGHT_CONTROL),
    SHIFT(GLFW.GLFW_KEY_LEFT_SHIFT, GLFW.GLFW_KEY_RIGHT_SHIFT),
    ALT(GLFW.GLFW_KEY_LEFT_ALT, GLFW.GLFW_KEY_RIGHT_ALT),
    NONE();

    private final int[] keys;

    KeyModifier(int... keys) {
        this.keys = keys;
    }

    public boolean isActive() {
        if (this == NONE)
            return true;
        var window = Minecraft.getInstance().getWindow();
        for (int key : keys)
            if (InputConstants.isKeyDown(window, key))
                return true;
        return false;
    }

    /** The modifier currently held (CONTROL, then SHIFT, then ALT), or NONE. */
    public static KeyModifier getActiveModifier() {
        for (var modifier : new KeyModifier[] {CONTROL, SHIFT, ALT})
            if (modifier.isActive())
                return modifier;
        return NONE;
    }
}
