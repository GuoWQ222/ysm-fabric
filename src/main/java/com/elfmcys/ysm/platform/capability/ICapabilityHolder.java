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
 * Fabric port: Forge's entity capability methods. Injected into {@code Entity} at compile time by Loom
 * (fabric.mod.json loom:injected_interfaces) and implemented at runtime by {@code EntityCapabilityMixin}.
 */
public interface ICapabilityHolder {
    @NotNull
    default <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        throw new AssertionError("EntityCapabilityMixin not applied");
    }

    @NotNull
    default <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap) {
        return getCapability(cap, null);
    }

    default void invalidateCaps() {
        throw new AssertionError("EntityCapabilityMixin not applied");
    }

    default void reviveCaps() {
        throw new AssertionError("EntityCapabilityMixin not applied");
    }
}
