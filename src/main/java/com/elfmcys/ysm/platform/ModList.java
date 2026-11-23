package com.elfmcys.ysm.platform;

import java.util.Optional;

/** Fabric port: the parts of Forge's ModList used by YSM. */
public final class ModList {
    private static final ModList INSTANCE = new ModList();

    private ModList() {}

    public static ModList get() {
        return INSTANCE;
    }

    public boolean isLoaded(String modId) {
        return Platform.isModLoaded(modId);
    }

    /** Version string of a loaded mod. */
    public Optional<String> getVersion(String modId) {
        return Platform.modVersion(modId);
    }
}
