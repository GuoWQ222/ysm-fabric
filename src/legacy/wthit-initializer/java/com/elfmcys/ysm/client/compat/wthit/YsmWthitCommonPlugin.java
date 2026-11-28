/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.client.compat.wthit;

import net.minecraft.resources.Identifier;

/**
 * WTHIT before its split plugin API (1.20.2, 1.20.3 and 1.20.6 Forge builds) has no common plugins: YsmWthitPlugin registers
 * the config toggle (2.6.5 Jade: "Show YSM Model Info", on by default); only its key stays here.
 */
public final class YsmWthitCommonPlugin {
    @SuppressWarnings("removal")
    static final Identifier MODEL_INFO = Identifier.fromNamespaceAndPath("ysm", "model_info");

    private YsmWthitCommonPlugin() {}
}
