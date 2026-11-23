// Modified for the unofficial Fabric 26.2 port by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.compat;

import com.elfmcys.ysm.util.ResourceDiagnostics;
import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.client.texture.CustomPBRTextureSet;
import com.elfmcys.ysm.info.type.PBRTextureType;
import com.elfmcys.ysm.natives.render.VertexFormatType;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.server.packs.resources.ResourceManager;

/**
 * Fabric port stand-in: the IrisCompat integration targets a Forge mod and is not ported; every method reports
 * "not installed". The original is kept in src/forge-compat-reference.
 */
@SuppressWarnings("unused")
public final class IrisCompat {

    private IrisCompat() {}

    public static void init() {
        
    }

    public static Optional<VertexFormatType> determineVertexFormatType(VertexFormat vertexFormat) {
        return Optional.empty();
    }

    public static boolean isInstalled() {
        return false;
    }

    public static boolean isRenderingShadow() {
        return false;
    }

    public static void requirePbrTextures(CustomPBRTextureSet texture, Consumer<AbstractTexture> hostOwnership) {
        
    }

    public static long getEntityId() {
        return 0;
    }


    public static void register() {
        
    }
}
