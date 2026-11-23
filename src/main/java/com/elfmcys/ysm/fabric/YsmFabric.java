/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.fabric;

import com.elfmcys.ysm.YesSteveModel;
import net.fabricmc.api.ModInitializer;

/** Fabric common entry point (local 26.2 port). */
public final class YsmFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        YesSteveModel.init();
    }
}
