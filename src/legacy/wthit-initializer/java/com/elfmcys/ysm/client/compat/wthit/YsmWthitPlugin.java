/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.client.compat.wthit;

import com.elfmcys.ysm.client.compat.ModelInfoTooltip;
import mcp.mobius.waila.api.IEntityAccessor;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IRegistrar;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.TooltipPosition;
import net.minecraft.world.entity.Entity;

/**
 * WTHIT tooltip line "YSM Model: name" for players and vehicles drawn with a YSM model, for WTHIT before its split
 * plugin API (the Forge builds for 1.20.2, 1.20.3 and 1.20.6: WTHIT 9, 10.0 and 11): one plugin class, named by "initializer" in
 * waila_plugins.json (build.gradle rewrites the file), registers the config toggle and the tooltip line that newer
 * WTHIT takes from YsmWthitCommonPlugin and the client plugin. Never loaded without WTHIT.
 */
public final class YsmWthitPlugin implements IWailaPlugin {
    @Override
    public void register(IRegistrar registrar) {
        registrar.addFeatureConfig(YsmWthitCommonPlugin.MODEL_INFO, true);
        registrar.addComponent(ModelInfoProvider.INSTANCE, TooltipPosition.BODY, Entity.class);
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
