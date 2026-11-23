/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.platform.event;

import java.util.function.Consumer;

/** Fabric port: the subset of Forge's event bus API used by YSM. */
public interface IEventBus {
    /** Registers static handlers of a {@link Class} target, or instance handlers of an object. */
    void register(Object target);

    void unregister(Object target);

    <T extends Event> void addListener(Class<T> type, Consumer<T> listener);

    <T extends Event> void addListener(EventPriority priority, Class<T> type, Consumer<T> listener);

    /** Posts an event; returns true when it was canceled. */
    boolean post(Event event);
}
