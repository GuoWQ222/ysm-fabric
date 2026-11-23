/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.platform.capability;

import com.elfmcys.ysm.platform.event.Event;
import net.minecraft.resources.Identifier;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Fabric port: fired once per object, the first time a capability is requested from it (or when it loads). */
public final class AttachCapabilitiesEvent<T> extends Event {
    private final Class<T> type;
    private final T object;
    private final Map<Identifier, ICapabilityProvider> capabilities = new LinkedHashMap<>();
    private final Map<Identifier, ICapabilityProvider> view = Collections.unmodifiableMap(capabilities);

    public AttachCapabilitiesEvent(Class<T> type, T object) {
        this.type = type;
        this.object = object;
    }

    public Class<T> getGenericType() {
        return type;
    }

    public T getObject() {
        return object;
    }

    public void addCapability(Identifier key, ICapabilityProvider provider) {
        if (capabilities.putIfAbsent(key, provider) != null)
            throw new IllegalStateException("Duplicate capability key: " + key + " " + provider);
    }

    public Map<Identifier, ICapabilityProvider> getCapabilities() {
        return view;
    }
}
