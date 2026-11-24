/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.client.compat.wthit;

import com.elfmcys.ysm.client.compat.ModelInfoTooltip;
import mcp.mobius.waila.api.IClientRegistrar;
import mcp.mobius.waila.api.IEntityAccessor;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.IWailaClientPlugin;
import net.minecraft.world.entity.Entity;

/**
 * WTHIT tooltip line "YSM Model: name" for players and vehicles drawn with a YSM model (2.6.5 did the same through
 * Jade and The One Probe). Discovered by WTHIT through waila_plugins.json; never loaded without WTHIT.
 */
public final class YsmWthitPlugin implements IWailaClientPlugin {
    @Override
    public void register(IClientRegistrar registrar) {
        registrar.body(ModelInfoProvider.INSTANCE, Entity.class);
    }

    private enum ModelInfoProvider implements IEntityComponentProvider {
        INSTANCE;

        @Override
        public void appendBody(ITooltip tooltip, IEntityAccessor accessor, IPluginConfig config) {
            if (!config.getBoolean(YsmWthitCommonPlugin.MODEL_INFO))
                return;
            var line = ModelInfoTooltip.line(accessor.getEntity());
            if (line != null)
                tooltip.addLine(line);
        }
    }
}
