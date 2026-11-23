/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.platform;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;
import java.util.Optional;

/** Fabric port: loader facts that YSM used to read from Forge's FML. */
public final class Platform {
    private Platform() {}

    public static Dist dist() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT ? Dist.CLIENT : Dist.DEDICATED_SERVER;
    }

    public static boolean isClient() {
        return dist() == Dist.CLIENT;
    }

    public static boolean isProduction() {
        return !FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    public static boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    public static Optional<String> modVersion(String modId) {
        return FabricLoader.getInstance().getModContainer(modId)
            .map(container -> container.getMetadata().getVersion().getFriendlyString());
    }

    public static Optional<Path> modRoot(String modId) {
        return FabricLoader.getInstance().getModContainer(modId).flatMap(container -> container.getRootPaths().stream().findFirst());
    }

    /** This mod's own version string ("dev" when unknown). */
    public static String ownVersion() {
        try {
            return modVersion(com.elfmcys.ysm.YesSteveModel.MOD_ID).orElse("dev");
        } catch (Throwable notInGame) {
            return "dev"; // unit tests run without a Fabric loader
        }
    }

    /** A path inside this mod's jar (or dev resources), like Forge's ModFile.findResource. */
    public static Path ownResource(String... parts) {
        var container = FabricLoader.getInstance().getModContainer(com.elfmcys.ysm.YesSteveModel.MOD_ID).orElseThrow();
        return container.findPath(String.join("/", parts)).orElseGet(() -> container.getRootPaths().get(0)
            .resolve(String.join("/", parts)));
    }

    public static Path gameDir() {
        return FabricLoader.getInstance().getGameDir();
    }

    public static Path configDir() {
        return FabricLoader.getInstance().getConfigDir();
    }
}
