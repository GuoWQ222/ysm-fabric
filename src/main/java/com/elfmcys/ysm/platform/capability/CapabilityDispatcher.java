/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.platform.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/** Fabric port: the attached capability providers of one object, plus their saved state. */
public final class CapabilityDispatcher {
    private final List<ICapabilityProvider> providers;
    private final Map<Identifier, ICapabilityProvider> byKey;

    public CapabilityDispatcher(Map<Identifier, ICapabilityProvider> attached) {
        this.byKey = Map.copyOf(attached);
        this.providers = List.copyOf(attached.values());
    }

    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        for (var provider : providers) {
            var result = provider.getCapability(cap, side);
            if (result.isPresent())
                return result;
        }
        return LazyOptional.empty();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public CompoundTag serialize() {
        var tag = new CompoundTag();
        for (var entry : byKey.entrySet())
            if (entry.getValue() instanceof ICapabilitySerializable serializable) {
                Tag value = serializable.serializeNBT();
                if (value != null)
                    tag.put(entry.getKey().toString(), value);
            }
        return tag;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void deserialize(CompoundTag tag) {
        for (var entry : byKey.entrySet())
            if (entry.getValue() instanceof ICapabilitySerializable serializable) {
                var value = tag.get(entry.getKey().toString());
                if (value != null)
                    serializable.deserializeNBT(value);
            }
    }
}
