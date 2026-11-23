/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.platform.capability;

import net.minecraft.nbt.Tag;

/** Fabric port: a capability provider whose state is saved with its entity. */
public interface ICapabilitySerializable<T extends Tag> extends ICapabilityProvider {
    T serializeNBT();

    void deserializeNBT(T nbt);
}
