/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.client.event;

import com.elfmcys.ysm.capability.ClientLazyCapabilityProvider;
import com.elfmcys.ysm.capability.PlayerAnimatableCapabilityProvider;
import com.elfmcys.ysm.capability.ProjectileAnimatableCapabilityProvider;
import com.elfmcys.ysm.capability.VehicleAnimatableCapabilityProvider;
import com.elfmcys.ysm.platform.capability.AttachCapabilitiesEvent;
import com.elfmcys.ysm.platform.capability.ICapabilityHolder;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;

/**
 * Client half of {@link com.elfmcys.ysm.event.CapabilityEvent}: attaches the client-only animatable capabilities to
 * client-side entities. It lives in its own client class because verifying the common class loads the types its
 * code assigns, and a dedicated server has no client classes (NeoForge/Forge servers refused to load YSM).
 */
public final class ClientCapabilityAttach {
    private ClientCapabilityAttach() {}

    public static void attach(AttachCapabilitiesEvent<Entity> event, Entity entity, Identifier animatableCap,
        Identifier projectileAnimatableCap, Identifier vehicleAnimatableCap, Identifier clientLazyCap) {
        // client player
        if (entity instanceof AbstractClientPlayer clientPlayer
            && !ICapabilityHolder.of(clientPlayer).getCapability(PlayerAnimatableCapabilityProvider.CAP).isPresent()
            && !event.getCapabilities().containsKey(animatableCap)) {
            event.addCapability(animatableCap, new PlayerAnimatableCapabilityProvider(clientPlayer));
            return;
        }

        // client lazily created capabilities
        if (!ICapabilityHolder.of(entity).getCapability(ClientLazyCapabilityProvider.CAP).isPresent()
            && !event.getCapabilities().containsKey(clientLazyCap)) {
            var vehicleAnimatable = new VehicleAnimatableCapabilityProvider(entity);
            event.addCapability(vehicleAnimatableCap, vehicleAnimatable);

            ProjectileAnimatableCapabilityProvider projectileAnimatable = null;
            if (entity instanceof Projectile projectile) {
                projectileAnimatable = new ProjectileAnimatableCapabilityProvider(projectile);
                event.addCapability(projectileAnimatableCap, projectileAnimatable);
            }

            event.addCapability(clientLazyCap, new ClientLazyCapabilityProvider(vehicleAnimatable, projectileAnimatable));
        }
    }
}
