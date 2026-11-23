/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.client.compat.wthit;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.capability.PlayerAnimatableCapabilityProvider;
import com.elfmcys.ysm.capability.VehicleAnimatableCapabilityProvider;
import com.elfmcys.ysm.client.entity.AnimatableEntity;
import com.elfmcys.ysm.client.lang.LanguageManager;
import mcp.mobius.waila.api.IClientRegistrar;
import mcp.mobius.waila.api.IEntityAccessor;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.IWailaClientPlugin;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

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
            Entity entity = accessor.getEntity();
            AnimatableEntity<?> animatable = entity instanceof Player
                ? entity.getCapability(PlayerAnimatableCapabilityProvider.CAP).orElse(null)
                : entity.getCapability(VehicleAnimatableCapabilityProvider.CAP).orElse(null);
            if (animatable == null)
                return;
            var name = modelName(animatable);
            if (name != null)
                tooltip.addLine(Component.translatable("top.yes_steve_model.model_info.id").append(name));
        }

        private static String modelName(AnimatableEntity<?> animatable) {
            try {
                animatable.waitForAsyncUpdate();
                if (!animatable.isPresentationAllowed() || !animatable.hasMatchingInstallation())
                    return null;
                var target = animatable.getModelRenderTarget();
                if (target == null)
                    return null;
                var metadata = target.info().getMetadata();
                var fallback = animatable.getModelId();
                var name = metadata == null || metadata.name() == null || metadata.name().isBlank() ? fallback
                    : metadata.name();
                return LanguageManager.getI18n(target, "metadata.name", name);
            } catch (RuntimeException failure) {
                // A tooltip must never break the HUD; the entity may be mid-reload.
                YesSteveModel.LOGGER.debug("WTHIT model info unavailable", failure);
                return null;
            }
        }
    }
}
