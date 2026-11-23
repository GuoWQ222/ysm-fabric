/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.platform.event;

/**
 * Fabric port: minimal replacement for Forge's event base class. Cancellation is allowed only for
 * classes annotated {@link Cancelable}.
 */
public class Event {
    private boolean canceled;

    public boolean isCancelable() {
        for (Class<?> type = getClass(); type != null && type != Event.class; type = type.getSuperclass())
            if (type.isAnnotationPresent(Cancelable.class))
                return true;
        return false;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void setCanceled(boolean canceled) {
        if (canceled && !isCancelable())
            throw new UnsupportedOperationException("Event " + getClass().getName() + " is not cancelable");
        this.canceled = canceled;
    }
}
