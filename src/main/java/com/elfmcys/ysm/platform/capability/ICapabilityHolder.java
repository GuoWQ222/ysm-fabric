/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.platform.capability;

import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Fabric port: Forge's entity capability methods, added to {@code Entity} at runtime by {@code EntityCapabilityMixin}.
 * Call sites go through {@link #of} (no compile-time interface injection, which ForgeGradle 7 lacks). The lifecycle
 * methods carry YSM in their names so they never replace Forge's own {@code Entity.invalidateCaps/reviveCaps}.
 */
public interface ICapabilityHolder {
    /** The capability view of an entity (every entity implements this interface at runtime). */
    static ICapabilityHolder of(net.minecraft.world.entity.Entity entity) {
        return (ICapabilityHolder) entity;
    }

    @NotNull
    default <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        throw new AssertionError("EntityCapabilityMixin not applied");
    }

    @NotNull
    default <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap) {
        return getCapability(cap, null);
    }

    default void invalidateYsmCaps() {
        throw new AssertionError("EntityCapabilityMixin not applied");
    }

    default void reviveYsmCaps() {
        throw new AssertionError("EntityCapabilityMixin not applied");
    }
}
