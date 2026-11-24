// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.info.type;

import net.minecraft.resources.Identifier;

public enum PBRTextureType {
    NORMAL("_n"), SPECULAR("_s");

    static final PBRTextureType[] VALUES = values();

    private final String suffix;

    PBRTextureType(String suffix) {
        this.suffix = suffix;
    }

    public Identifier getId(Identifier uvId) {
        return Identifier.fromNamespaceAndPath(uvId.getNamespace(), uvId.getPath() + suffix);
    }
}
