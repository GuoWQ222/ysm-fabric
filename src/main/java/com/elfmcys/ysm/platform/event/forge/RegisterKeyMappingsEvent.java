/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.platform.event.forge;

import com.elfmcys.ysm.platform.event.Event;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;

/** Fabric port: posted during client init; registers through Fabric's key mapping API. */
public class RegisterKeyMappingsEvent extends Event implements IModBusEvent {
    public void register(KeyMapping mapping) {
        KeyMappingHelper.registerKeyMapping(mapping);
    }
}
