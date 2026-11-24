/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.client.compat.jade;

import com.elfmcys.ysm.client.compat.ModelInfoTooltip;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

/**
 * Jade tooltip line "YSM Model: name" for players and YSM vehicles, as 2.6.5's Jade plugin. The provider uid
 * yes_steve_model:model_info keeps 2.6.5's config key (config.jade.plugin_yes_steve_model.model_info). Registered as the
 * "jade" entrypoint in fabric.mod.json; never loaded without Jade.
 */
@WailaPlugin
public final class YsmJadePlugin implements IWailaPlugin {
    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerEntityComponent(ModelInfoProvider.INSTANCE, Entity.class);
    }

    private enum ModelInfoProvider implements IEntityComponentProvider {
        INSTANCE;

        @SuppressWarnings("removal")
        private static final Identifier UID = Identifier.fromNamespaceAndPath("yes_steve_model", "model_info");

        @Override
        public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
            var line = ModelInfoTooltip.line(accessor.getEntity());
            if (line != null)
                tooltip.add(line);
        }

        @Override
        public Identifier getUid() {
            return UID;
        }
    }
}
