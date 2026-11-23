/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.platform.capability;

/** Fabric port: capability token, as in Forge. Identity is the token instance from {@link CapabilityManager}. */
public final class Capability<T> {
    private final String name;

    Capability(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /** Returns {@code instance} when {@code toCheck} is this capability, otherwise an empty optional. */
    @SuppressWarnings("unchecked")
    public <R> LazyOptional<R> orEmpty(Capability<R> toCheck, LazyOptional<T> instance) {
        return this == toCheck ? (LazyOptional<R>)instance : LazyOptional.empty();
    }

    @Override
    public String toString() {
        return "Capability[" + name + "]";
    }
}
