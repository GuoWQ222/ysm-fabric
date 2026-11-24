// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.model.catalog.source;

import com.elfmcys.ysm.AssetPaths;
import com.elfmcys.ysm.model.ModelRuntime;

import java.nio.file.Path;
import java.util.List;

public final class ModelCatalogSources {
    private ModelCatalogSources() {}

    public static List<ModelCatalogSource> sources() {
        return sources(ModelRuntime.paths());
    }

    public static List<ModelCatalogSource> reloadableSources() {
        return reloadableSources(ModelRuntime.paths());
    }

    public static List<ModelCatalogSource> sources(AssetPaths paths) {
        var reloadable = reloadableSources(paths);
        var sources = new java.util.ArrayList<ModelCatalogSource>();
        sources.add(builtin(paths));
        sources.addAll(reloadable);
        return List.copyOf(sources);
    }

    public static ModelCatalogSource builtin(AssetPaths paths) {
        return new ModelCatalogSource(CatalogRootKind.BUILTIN, paths.builtinModelsResource(), false);
    }

    public static List<ModelCatalogSource> reloadableSources(AssetPaths paths) {
        var sources = new java.util.ArrayList<ModelCatalogSource>();
        paths.global().ifPresent(global -> {
            sources.add(new ModelCatalogSource(CatalogRoot.GLOBAL, global.modelsRoot(), false));
        });
        sources.add(new ModelCatalogSource(CatalogRoot.LOCAL, paths.modelsRoot(), true));
        // Fabric port (2.6.5 parity): 2.6.5's folders, only when present (never created or moved).
        if (java.nio.file.Files.isDirectory(paths.legacyCustomRoot()))
            sources.add(new ModelCatalogSource(CatalogRoot.CUSTOM, paths.legacyCustomRoot(), false));
        if (java.nio.file.Files.isDirectory(paths.legacyAuthRoot()))
            sources.add(new ModelCatalogSource(CatalogRoot.AUTH, paths.legacyAuthRoot(), false));
        return List.copyOf(sources);
    }

    public static Path modelsPath() {
        return ModelRuntime.paths().defaultModelsRoot();
    }
}
