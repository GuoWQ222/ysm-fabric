/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.platform.capability;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Fabric port: returns one {@link Capability} per token type name, as Forge does. */
public final class CapabilityManager {
    private static final Map<String, Capability<?>> CAPABILITIES = new ConcurrentHashMap<>();

    private CapabilityManager() {}

    @SuppressWarnings("unchecked")
    public static <T> Capability<T> get(CapabilityToken<T> token) {
        return (Capability<T>)CAPABILITIES.computeIfAbsent(token.typeName(), Capability::new);
    }
}
