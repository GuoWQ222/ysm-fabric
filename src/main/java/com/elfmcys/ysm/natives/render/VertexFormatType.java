// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.natives.render;

public enum VertexFormatType {
    FALLBACK(0), VANILLA(1), IRIS_56(2), IRIS_56_AR(3), IRIS_55(4), IRIS_54(5),
    /** Iris 1.11+ (Minecraft 26.x): four 16-bit entity values; native VertexKind::kIris56V2. */
    IRIS_56_V2(6);

    private final int id;

    VertexFormatType(int id) {
        this.id = id;
    }

    public int id() {
        return id;
    }
}
