/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.legacy.input;

import net.minecraft.client.gui.screens.Screen;

/**
 * Minecraft before 1.21.9 hands GUI input over as loose numbers; 1.21.9 wraps it in event records. These stand-ins
 * (com.elfmcys.ysm.legacy.input, mapped from net.minecraft.client.input by versions/below-1.21.9.remap) give YSM's GUI
 * code the 1.21.9 shape on older versions. Modifier bits are GLFW's (shift 1, control 2, alt 4).
 */
public interface InputWithModifiers {
    int modifiers();

    default boolean hasShiftDown() {
        return (modifiers() & 1) != 0;
    }

    default boolean hasControlDown() {
        return (modifiers() & 2) != 0;
    }

    default boolean hasAltDown() {
        return (modifiers() & 4) != 0;
    }

    /** The modifier keys held right now (older Minecraft does not pass them along with clicks). */
    static int currentModifiers() {
        return (Screen.hasShiftDown() ? 1 : 0) | (Screen.hasControlDown() ? 2 : 0) | (Screen.hasAltDown() ? 4 : 0);
    }

    /** A button press that came without its input (Button.onPress()). */
    static InputWithModifiers current() {
        int modifiers = currentModifiers();
        return () -> modifiers;
    }
}
