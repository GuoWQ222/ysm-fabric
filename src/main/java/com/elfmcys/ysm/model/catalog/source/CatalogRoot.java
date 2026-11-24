// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.model.catalog.source;

/** Public region namespace; access policy is a separate catalog fact. */
public enum CatalogRoot {
    GLOBAL("global:", CatalogRootKind.MODELS), LOCAL("local:", CatalogRootKind.MODELS),
    BUILTIN("builtin:", CatalogRootKind.BUILTIN),
    /** Fabric port (2.6.5 parity): 2.6.5's config/yes_steve_model/custom folder, read in place. */
    CUSTOM("custom:", CatalogRootKind.MODELS),
    /** Fabric port (2.6.5 parity): 2.6.5's config/yes_steve_model/auth folder; its models are restricted. */
    AUTH("auth:", CatalogRootKind.MODELS);

    private final String prefix;
    private final CatalogRootKind kind;

    CatalogRoot(String prefix, CatalogRootKind kind) {
        this.prefix = prefix;
        this.kind = kind;
    }

    public String prefix() {
        return prefix;
    }

    public CatalogRootKind kind() {
        return kind;
    }

    public static CatalogRoot local(CatalogRootKind kind) {
        return kind == CatalogRootKind.BUILTIN ? BUILTIN : LOCAL;
    }
}
