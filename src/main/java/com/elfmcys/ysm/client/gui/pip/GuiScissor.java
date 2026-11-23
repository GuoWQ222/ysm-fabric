/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.client.gui.pip;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.jetbrains.annotations.Nullable;

/**
 * The current GUI scissor rectangle. Fabric port: GuiGraphicsExtractor keeps it in a private nested class that a Mixin
 * accessor cannot name, and 26.x ships unobfuscated names, so it is read reflectively (handles cached once).
 */
final class GuiScissor {
    private static final Field STACK;
    private static final Method PEEK;

    static {
        try {
            STACK = GuiGraphicsExtractor.class.getDeclaredField("scissorStack");
            STACK.setAccessible(true);
            PEEK = STACK.getType().getDeclaredMethod("peek");
            PEEK.setAccessible(true);
        } catch (ReflectiveOperationException failure) {
            throw new ExceptionInInitializerError(failure);
        }
    }

    private GuiScissor() {}

    static @Nullable ScreenRectangle current(GuiGraphicsExtractor graphics) {
        try {
            return (ScreenRectangle)PEEK.invoke(STACK.get(graphics));
        } catch (ReflectiveOperationException failure) {
            return null;
        }
    }
}
