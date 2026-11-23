/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.platform;

import java.nio.file.Path;
import java.util.function.Supplier;

/** Fabric port: Forge's FMLPaths constants. */
public enum FMLPaths {
    GAMEDIR(Platform::gameDir), CONFIGDIR(Platform::configDir);

    private final Supplier<Path> path;

    FMLPaths(Supplier<Path> path) {
        this.path = path;
    }

    public Path get() {
        return path.get();
    }
}
