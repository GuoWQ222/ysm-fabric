/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.platform.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.resources.Identifier;

/**
 * Fabric port: a vanilla key mapping that also carries Forge's conflict context and modifier. All YSM keys
 * share the {@code ysm:main} category (translation key {@code key.category.ysm.main}, as in 2.6.5).
 */
public class YsmKeyMapping extends KeyMapping {
    //#if MC >= 1.21.9
    public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("ysm", "main"));
    //#else
    //$$ // before 1.21.9 a key category is its translation key
    //$$ public static final String CATEGORY = "key.category.ysm.main";
    //#endif

    private final KeyConflictContext context;
    private final KeyModifier modifier;

    public YsmKeyMapping(String name, KeyConflictContext context, KeyModifier modifier, InputConstants.Type type,
        int keyCode, String category) {
        super(name, type, keyCode, CATEGORY);
        this.context = context;
        this.modifier = modifier;
    }

    /** Named apart from NeoForge/Forge's KeyMapping extension (which returns their own types). */
    public KeyConflictContext ysmConflictContext() {
        return context;
    }

    public KeyModifier ysmKeyModifier() {
        return modifier;
    }

    //#if MC >= 1.21.9
    /** Forge's {@code matches(keyCode, scanCode)} (vanilla has it before 1.21.9). */
    public boolean matches(int keyCode, int scanCode) {
        return matches(new KeyEvent(keyCode, scanCode, 0));
    }
    //#endif
}
