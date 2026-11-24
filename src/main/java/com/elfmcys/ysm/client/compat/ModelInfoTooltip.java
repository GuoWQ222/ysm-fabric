/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.client.compat;

import com.elfmcys.ysm.platform.capability.ICapabilityHolder;
import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.capability.PlayerAnimatableCapabilityProvider;
import com.elfmcys.ysm.capability.VehicleAnimatableCapabilityProvider;
import com.elfmcys.ysm.client.entity.AnimatableEntity;
import com.elfmcys.ysm.client.lang.LanguageManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

/**
 * The "YSM Model: name" tooltip line shared by the WTHIT and Jade plugins (2.6.5 had it for Jade and The One Probe):
 * shown for players and vehicles drawn with an installed YSM model.
 */
public final class ModelInfoTooltip {
    private ModelInfoTooltip() {}

    public static @Nullable Component line(Entity entity) {
        var name = modelName(entity);
        return name == null ? null : Component.translatable("top.yes_steve_model.model_info.id").append(name);
    }

    public static @Nullable String modelName(Entity entity) {
        AnimatableEntity<?> animatable = entity instanceof Player
            ? ICapabilityHolder.of(entity).getCapability(PlayerAnimatableCapabilityProvider.CAP).orElse(null)
            : ICapabilityHolder.of(entity).getCapability(VehicleAnimatableCapabilityProvider.CAP).orElse(null);
        if (animatable == null)
            return null;
        try {
            animatable.waitForAsyncUpdate();
            if (!animatable.isPresentationAllowed() || !animatable.hasMatchingInstallation())
                return null;
            var target = animatable.getModelRenderTarget();
            if (target == null)
                return null;
            var metadata = target.info().getMetadata();
            var name = metadata == null || metadata.name() == null || metadata.name().isBlank()
                ? animatable.getModelId() : metadata.name();
            return LanguageManager.getI18n(target, "metadata.name", name);
        } catch (RuntimeException failure) {
            // A tooltip must never break the HUD; the entity may be mid-reload.
            YesSteveModel.LOGGER.debug("Model info tooltip unavailable", failure);
            return null;
        }
    }
}
