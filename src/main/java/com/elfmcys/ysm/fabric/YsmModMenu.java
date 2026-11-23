/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.fabric;

import com.elfmcys.ysm.client.gui.ConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/** Mod Menu integration (2.6.5 parity): the "Configure" button opens YSM's config screen. Loaded only by Mod Menu. */
public final class YsmModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new ConfigScreen(parent, true);
    }
}
