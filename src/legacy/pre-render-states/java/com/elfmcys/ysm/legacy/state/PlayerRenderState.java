/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.legacy.state;

import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

/** 1.21.2's player render state for older Minecraft (see EntityRenderState). */
public class PlayerRenderState extends HumanoidRenderState {
    public PlayerSkin skin;
    public boolean showCape;
    public @Nullable Component scoreText;
}
