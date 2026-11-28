/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.legacy.input;

/** 1.21.9's mouse click record for older Minecraft (see InputWithModifiers). */
public record MouseButtonEvent(double x, double y, MouseButtonInfo buttonInfo) implements InputWithModifiers {
    /** A click as older Minecraft reports it (position and button), with the modifier keys held now. */
    public static MouseButtonEvent of(double x, double y, int button) {
        return new MouseButtonEvent(x, y, new MouseButtonInfo(button, InputWithModifiers.currentModifiers()));
    }

    public int button() {
        return buttonInfo.button();
    }

    @Override
    public int modifiers() {
        return buttonInfo.modifiers();
    }
}
