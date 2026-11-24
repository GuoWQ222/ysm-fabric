/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.neoforge;

import com.elfmcys.ysm.YesSteveModel;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

/** NeoForge entry point (the counterpart of the Fabric port's YsmFabric / YsmFabricClient). */
@Mod(YesSteveModel.MOD_ID)
public final class YsmNeoForge {
    private static IEventBus modBus;
    private static ModContainer container;

    public YsmNeoForge(IEventBus modBus, ModContainer container) {
        YsmNeoForge.modBus = modBus;
        YsmNeoForge.container = container;
        YesSteveModel.init();
        if (com.elfmcys.ysm.platform.Platform.isClient())
            YesSteveModel.initClient();
    }

    public static IEventBus modBus() {
        return modBus;
    }

    public static ModContainer container() {
        return container;
    }

    /** A path inside this mod: a dev resource directory, or the jar opened as a file system. */
    public static java.nio.file.Path ownResource(String relative) {
        var file = net.neoforged.fml.ModList.get().getModFileById(YesSteveModel.MOD_ID).getFile();
        java.nio.file.Path fallback = null;
        for (var root : file.getContents().getContentRoots()) {
            var base = java.nio.file.Files.isDirectory(root) ? root : jarRoot(root);
            var candidate = base.resolve(relative);
            if (java.nio.file.Files.exists(candidate))
                return candidate;
            if (fallback == null)
                fallback = candidate;
        }
        if (fallback == null)
            throw new IllegalStateException("No content roots for " + YesSteveModel.MOD_ID);
        return fallback;
    }

    private static final java.util.Map<java.nio.file.Path, java.nio.file.FileSystem> JARS = new java.util.concurrent.ConcurrentHashMap<>();

    private static java.nio.file.Path jarRoot(java.nio.file.Path jar) {
        return JARS.computeIfAbsent(jar, path -> {
            try {
                return java.nio.file.FileSystems.newFileSystem(path);
            } catch (java.io.IOException failure) {
                throw new java.io.UncheckedIOException(failure);
            }
        }).getPath("/");
    }
}
