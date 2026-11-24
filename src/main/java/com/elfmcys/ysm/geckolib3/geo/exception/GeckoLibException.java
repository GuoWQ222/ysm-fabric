// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.geckolib3.geo.exception;

import net.minecraft.resources.Identifier;

import java.io.Serial;

public class GeckoLibException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public GeckoLibException(Identifier fileLocation, String message) {
        super(fileLocation + ": " + message);
    }

    public GeckoLibException(Identifier fileLocation, String message, Throwable cause) {
        super(fileLocation + ": " + message, cause);
    }
}
