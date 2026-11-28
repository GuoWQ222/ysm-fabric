/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.legacy.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.network.chat.Component;

/**
 * Minecraft before 1.20.3 has no Checkbox.builder; versions/below-1.20.3.remap points YSM's builder calls here. The
 * checkbox is the 1.20.2 one (20 px box), sized to its label, and reports changes like 1.20.3's OnValueChange.
 */
public final class CheckboxBuilder {
    /** 1.20.3's Checkbox.OnValueChange. */
    @FunctionalInterface
    public interface OnValueChange {
        void onValueChange(Checkbox checkbox, boolean selected);
    }

    private static final int BOX_SIZE = 20;
    private final Component message;
    private final Font font;
    private int x;
    private int y;
    private boolean selected;
    private OnValueChange onValueChange = (checkbox, value) -> {};

    private CheckboxBuilder(Component message, Font font) {
        this.message = message;
        this.font = font;
    }

    public static CheckboxBuilder builder(Component message, Font font) {
        return new CheckboxBuilder(message, font);
    }

    public CheckboxBuilder pos(int x, int y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public CheckboxBuilder selected(boolean selected) {
        this.selected = selected;
        return this;
    }

    public CheckboxBuilder onValueChange(OnValueChange onValueChange) {
        this.onValueChange = onValueChange;
        return this;
    }

    public Checkbox build() {
        var listener = onValueChange;
        return new Checkbox(x, y, BOX_SIZE + 4 + font.width(message), BOX_SIZE, message, selected) {
            @Override
            public void onPress() {
                super.onPress();
                listener.onValueChange(this, selected());
            }
        };
    }
}
