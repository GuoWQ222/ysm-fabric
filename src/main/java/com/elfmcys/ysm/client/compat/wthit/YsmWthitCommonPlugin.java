/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.client.compat.wthit;

import mcp.mobius.waila.api.ICommonRegistrar;
import mcp.mobius.waila.api.IWailaCommonPlugin;
import net.minecraft.resources.Identifier;

/** Registers the WTHIT config toggle (2.6.5 Jade: "Show YSM Model Info", on by default). */
public final class YsmWthitCommonPlugin implements IWailaCommonPlugin {
    @SuppressWarnings("removal")
    static final Identifier MODEL_INFO = Identifier.fromNamespaceAndPath("ysm", "model_info");

    @Override
    public void register(ICommonRegistrar registrar) {
        registrar.featureConfig(MODEL_INFO, true);
    }
}
