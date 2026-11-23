/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.platform.event.forge;

import com.elfmcys.ysm.platform.event.Event;
import net.minecraft.server.packs.resources.PreparableReloadListener;

import java.util.ArrayList;
import java.util.List;

/** Fabric port: collects server data reload listeners (registered through Fabric's resource loader). */
public class AddReloadListenerEvent extends Event {
    private final List<PreparableReloadListener> listeners = new ArrayList<>();

    public void addListener(PreparableReloadListener listener) {
        listeners.add(listener);
    }

    public List<PreparableReloadListener> getListeners() {
        return listeners;
    }
}
