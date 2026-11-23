/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.platform.capability;

import java.lang.reflect.ParameterizedType;

/** Fabric port: type token; create as {@code new CapabilityToken<Foo>() {}}. */
public abstract class CapabilityToken<T> {
    final String typeName() {
        var type = getClass().getGenericSuperclass();
        if (type instanceof ParameterizedType parameterized)
            return parameterized.getActualTypeArguments()[0].getTypeName();
        throw new IllegalStateException("CapabilityToken must be created as an anonymous subclass with a type argument");
    }
}
