/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.platform;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Loader facts that YSM used to read from Forge's FML. Fabric port, multi-loader build: one branch per loader, picked
 * by the build's preprocessor (the checked-in code is the Fabric variant).
 */
public final class Platform {
    private Platform() {}

    /** One loaded mod: id, display name and version. */
    public record ModInfo(String id, String name, String version) {}

    public static Dist dist() {
        //#if LOADER == fabric
        return net.fabricmc.loader.api.FabricLoader.getInstance().getEnvironmentType() == net.fabricmc.api.EnvType.CLIENT
            ? Dist.CLIENT : Dist.DEDICATED_SERVER;
        //#elseif LOADER == neoforge
        //$$ return net.neoforged.fml.loading.FMLEnvironment.getDist().isClient() ? Dist.CLIENT : Dist.DEDICATED_SERVER;
        //#else
        //$$ return net.minecraftforge.fml.loading.FMLEnvironment.dist.isClient() ? Dist.CLIENT : Dist.DEDICATED_SERVER;
        //#endif
    }

    public static boolean isClient() {
        return dist() == Dist.CLIENT;
    }

    public static boolean isProduction() {
        //#if LOADER == fabric
        return !net.fabricmc.loader.api.FabricLoader.getInstance().isDevelopmentEnvironment();
        //#elseif LOADER == neoforge
        //$$ return net.neoforged.fml.loading.FMLEnvironment.isProduction();
        //#else
        //$$ return net.minecraftforge.fml.loading.FMLEnvironment.production;
        //#endif
    }

    public static boolean isModLoaded(String modId) {
        //#if LOADER == fabric
        return net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded(modId);
        //#elseif LOADER == neoforge
        //$$ return net.neoforged.fml.ModList.get().isLoaded(modId);
        //#else
        //#if MC >= 26.1
        //$$ return net.minecraftforge.fml.ModList.isLoaded(modId);
        //#else
        //$$ return net.minecraftforge.fml.ModList.get().isLoaded(modId);
        //#endif
        //#endif
    }

    public static Optional<String> modVersion(String modId) {
        //#if LOADER == fabric
        return net.fabricmc.loader.api.FabricLoader.getInstance().getModContainer(modId)
            .map(container -> container.getMetadata().getVersion().getFriendlyString());
        //#elseif LOADER == neoforge
        //$$ return net.neoforged.fml.ModList.get().getModContainerById(modId)
        //$$     .map(container -> versionOf(container.getModInfo()));
        //#else
        //#if MC >= 26.1
        //$$ return net.minecraftforge.fml.ModList.getModContainerById(modId)
        //#else
        //$$ return net.minecraftforge.fml.ModList.get().getModContainerById(modId)
        //#endif
        //$$     .map(container -> versionOf(container.getModInfo()));
        //#endif
    }

    //#if LOADER == neoforge
    //$$ private static final String MOD_INFO = "net.neoforged.neoforgespi.language.IModInfo";
    //#elseif LOADER == forge
    //$$ private static final String MOD_INFO = "net.minecraftforge.forgespi.language.IModInfo";
    //#endif
    //#if LOADER != fabric
    //$$ private static java.lang.reflect.Method modInfoVersion;
    //$$
    //$$ /**
    //$$  * IModInfo.getVersion() looked up by name: it returns Maven's ArtifactVersion, which the release jar relocates along
    //$$  * with YSM's own copy of Maven's library, so a direct call would name a method the loader does not have.
    //$$  */
    //$$ private static String versionOf(Object modInfo) {
    //$$     try {
    //$$         if (modInfoVersion == null)
    //$$             modInfoVersion = Class.forName(MOD_INFO).getMethod("getVersion");
    //$$         return String.valueOf(modInfoVersion.invoke(modInfo));
    //$$     } catch (ReflectiveOperationException failure) {
    //$$         return "unknown";
    //$$     }
    //$$ }
    //#endif

    /** All loaded mods. */
    public static List<ModInfo> mods() {
        //#if LOADER == fabric
        return net.fabricmc.loader.api.FabricLoader.getInstance().getAllMods().stream()
            .map(mod -> new ModInfo(mod.getMetadata().getId(), mod.getMetadata().getName(),
                mod.getMetadata().getVersion().getFriendlyString()))
            .toList();
        //#elseif LOADER == neoforge
        //$$ return net.neoforged.fml.ModList.get().getMods().stream()
        //$$     .map(mod -> new ModInfo(mod.getModId(), mod.getDisplayName(), versionOf(mod))).toList();
        //#else
        //#if MC >= 26.1
        //$$ return net.minecraftforge.fml.ModList.getMods().stream()
        //#else
        //$$ return net.minecraftforge.fml.ModList.get().getMods().stream()
        //#endif
        //$$     .map(mod -> new ModInfo(mod.getModId(), mod.getDisplayName(), versionOf(mod))).toList();
        //#endif
    }

    public static Optional<Path> modRoot(String modId) {
        //#if LOADER == fabric
        return net.fabricmc.loader.api.FabricLoader.getInstance().getModContainer(modId)
            .flatMap(container -> container.getRootPaths().stream().findFirst());
        //#elseif LOADER == neoforge
        //$$ var file = net.neoforged.fml.ModList.get().getModFileById(modId);
        //$$ return file == null ? Optional.empty() : Optional.of(file.getFile().getFilePath());
        //#else
        //#if MC >= 26.1
        //$$ var file = net.minecraftforge.fml.ModList.getModFileById(modId);
        //#else
        //$$ var file = net.minecraftforge.fml.ModList.get().getModFileById(modId);
        //#endif
        //$$ return file == null ? Optional.empty() : Optional.of(file.getFile().getFilePath());
        //#endif
    }

    /** This mod's own version string ("dev" when unknown). */
    public static String ownVersion() {
        try {
            return modVersion(com.elfmcys.ysm.YesSteveModel.MOD_ID).orElse("dev");
        } catch (Throwable notInGame) {
            return "dev"; // unit tests run without a mod loader
        }
    }

    /** A path inside this mod's jar (or dev resources), like Forge's ModFile.findResource. */
    public static Path ownResource(String... parts) {
        //#if LOADER == fabric
        var container = net.fabricmc.loader.api.FabricLoader.getInstance()
            .getModContainer(com.elfmcys.ysm.YesSteveModel.MOD_ID).orElseThrow();
        return container.findPath(String.join("/", parts)).orElseGet(() -> container.getRootPaths().get(0)
            .resolve(String.join("/", parts)));
        //#elseif LOADER == neoforge
        //$$ return com.elfmcys.ysm.neoforge.YsmNeoForge.ownResource(String.join("/", parts));
        //#else
        //#if MC >= 26.1
        //$$ return net.minecraftforge.fml.ModList.getModFileById(com.elfmcys.ysm.YesSteveModel.MOD_ID).getFile()
        //#else
        //$$ return net.minecraftforge.fml.ModList.get().getModFileById(com.elfmcys.ysm.YesSteveModel.MOD_ID).getFile()
        //#endif
        //$$     .findResource(parts);
        //#endif
    }

    public static Path gameDir() {
        //#if LOADER == fabric
        return net.fabricmc.loader.api.FabricLoader.getInstance().getGameDir();
        //#elseif LOADER == neoforge
        //$$ return net.neoforged.fml.loading.FMLPaths.GAMEDIR.get();
        //#else
        //$$ return net.minecraftforge.fml.loading.FMLPaths.GAMEDIR.get();
        //#endif
    }

    public static Path configDir() {
        //#if LOADER == fabric
        return net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir();
        //#elseif LOADER == neoforge
        //$$ return net.neoforged.fml.loading.FMLPaths.CONFIGDIR.get();
        //#else
        //$$ return net.minecraftforge.fml.loading.FMLPaths.CONFIGDIR.get();
        //#endif
    }
}
