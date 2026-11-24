// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.compat;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.client.texture.CustomPBRTextureSet;
import com.elfmcys.ysm.info.type.PBRTextureType;
import com.elfmcys.ysm.natives.render.VertexFormatType;
import com.elfmcys.ysm.util.ResourceDiagnostics;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;
import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.pbr.loader.PBRTextureLoader;
import net.irisshaders.iris.pbr.loader.PBRTextureLoaderRegistry;
import net.irisshaders.iris.pbr.texture.PBRTextureManager;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.irisshaders.iris.vertices.IrisVertexFormats;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.server.packs.resources.ResourceManager;

/**
 * Iris (Fabric, Minecraft 26.x) integration, ported from the Oculus one:
 * <ul>
 * <li>with a shader pack active, entity buffers use IrisVertexFormats.ENTITY; the native renderer writes that layout
 * directly when it is one it knows (otherwise the fallback writer goes through Iris's own vertex consumer);</li>
 * <li>the entity, block-entity and item ids Iris passes to shaders;</li>
 * <li>the shadow pass;</li>
 * <li>normal and specular maps of YSM models as Iris PBR textures.</li>
 * </ul>
 * Every Iris class is touched only after {@link #init()} found Iris, so the mod runs without it.
 */
public final class IrisCompat {
    private static final String MOD_ID = "iris";
    private static boolean installed;
    private static VertexFormat entityFormat;
    private static VertexFormatType entityType;

    private IrisCompat() {}

    public static void init() {
        if (!com.elfmcys.ysm.platform.Platform.isModLoaded(MOD_ID))
            return;
        try {
            var format = IrisVertexFormats.ENTITY;
            var type = nativeLayout(format);
            PBRLoader.register();
            entityFormat = format;
            entityType = type;
            installed = true;
            entityIdNow();
            IrisApi.getInstance().isRenderingShadowPass();
            YesSteveModel.LOGGER.info("Iris integration enabled; entity vertex layout {} ({} bytes)",
                type == null ? "unknown, using the fallback writer" : type, format.getVertexSize());
        } catch (Throwable error) {
            YesSteveModel.LOGGER.error("Failed to set up the Iris integration", error);
            installed = false;
            entityFormat = null;
            entityType = null;
        }
    }

    /**
     * The native layout matching Iris's entity format, or null when no native layout matches byte for byte (the
     * fallback writer is used then).
     */
    private static VertexFormatType nativeLayout(VertexFormat format) {
        //#if MC >= 26.2
        if (format.getVertexSize() != 56 || offset(format, "Position") != 0 || offset(format, "Color") != 12
            || offset(format, "UV0") != 16 || offset(format, "UV1") != 24 || offset(format, "UV2") != 28
            || offset(format, "Normal") != 32 || offset(format, IrisVertexFormats.ENTITY_ID_ATTRIBUTE) != 36)
            return null;
        int mid = offset(format, IrisVertexFormats.MID_TEXTURE_ATTRIBUTE);
        int tangent = offset(format, IrisVertexFormats.TANGENT_ATTRIBUTE);
        //#else
        //$$ // Iris for 26.1 names attributes by vanilla VertexFormatElement
        //$$ if (format.getVertexSize() != 56 || offset(format, com.mojang.blaze3d.vertex.VertexFormatElement.POSITION) != 0
        //$$     || offset(format, com.mojang.blaze3d.vertex.VertexFormatElement.COLOR) != 12
        //$$     || offset(format, com.mojang.blaze3d.vertex.VertexFormatElement.UV0) != 16
        //$$     || offset(format, com.mojang.blaze3d.vertex.VertexFormatElement.UV1) != 24
        //$$     || offset(format, com.mojang.blaze3d.vertex.VertexFormatElement.UV2) != 28
        //$$     || offset(format, com.mojang.blaze3d.vertex.VertexFormatElement.NORMAL) != 32
        //$$     || offset(format, IrisVertexFormats.ENTITY_ID_ELEMENT) != 36)
        //$$     return null;
        //$$ int mid = offset(format, IrisVertexFormats.MID_TEXTURE_ELEMENT);
        //$$ int tangent = offset(format, IrisVertexFormats.TANGENT_ELEMENT);
        //#endif
        if (mid == 44 && tangent == 52)
            return VertexFormatType.IRIS_56_V2; // Iris 1.11+: iris_Entity has four 16-bit values
        if (mid == 42 && tangent == 50)
            return VertexFormatType.IRIS_56;
        return null;
    }

    //#if MC >= 26.2
    private static int offset(VertexFormat format, String attribute) {
        try {
            return IrisVertexFormats.getOffset(format, attribute);
        } catch (RuntimeException missing) {
            return -1;
        }
    }
    //#else
    //$$ private static int offset(VertexFormat format, com.mojang.blaze3d.vertex.VertexFormatElement element) {
    //$$     return format.contains(element) ? format.getOffset(element) : -1;
    //$$ }
    //#endif

    public static Optional<VertexFormatType> determineVertexFormatType(VertexFormat vertexFormat) {
        if (installed && entityType != null && entityFormat == vertexFormat)
            return Optional.of(entityType);
        return Optional.empty();
    }

    public static boolean isInstalled() {
        return installed;
    }

    public static boolean isRenderingShadow() {
        return installed && IrisApi.getInstance().isRenderingShadowPass();
    }

    /** Entity, block-entity and item ids as Iris writes them (little-endian 16-bit values). */
    public static long getEntityId() {
        return installed ? entityIdNow() : 0;
    }

    private static long entityIdNow() {
        var state = CapturedRenderingState.INSTANCE;
        short entity = (short)state.getCurrentRenderedEntity();
        short blockEntity = (short)state.getCurrentRenderedBlockEntity();
        short item = (short)state.getCurrentRenderedItem();
        return (entity & 0xFFFFL) | ((blockEntity & 0xFFFFL) << 16) | ((item & 0xFFFFL) << 32);
    }

    /**
     * Makes Iris load (and own) the model's normal and specular maps now. Iris finds textures by GL id: asking the
     * texture for its GPU texture registers it with Iris's tracker.
     */
    public static void requirePbrTextures(CustomPBRTextureSet texture, Consumer<AbstractTexture> hostOwnership) {
        if (!installed)
            throw new IllegalStateException("Iris PBR integration is not installed");
        if (!(texture.getTexture() instanceof GlTexture glTexture))
            throw new IllegalStateException("Iris PBR textures need the OpenGL backend");
        var holder = PBRTextureManager.INSTANCE.getOrLoadHolder(glTexture.glId());
        IllegalStateException failure = null;
        if (texture.getNormal() != null) {
            if (texture.getNormal() == holder.normalTexture())
                hostOwnership.accept(texture.getNormal());
            else
                failure = new IllegalStateException("Iris did not adopt the expected normal texture");
        }
        if (texture.getSpecular() != null) {
            if (texture.getSpecular() == holder.specularTexture()) {
                hostOwnership.accept(texture.getSpecular());
            } else {
                var specular = new IllegalStateException("Iris did not adopt the expected specular texture");
                if (failure == null)
                    failure = specular;
                else
                    failure.addSuppressed(specular);
            }
        }
        if (failure != null)
            throw failure;
    }

    private static final class PBRLoader implements PBRTextureLoader<CustomPBRTextureSet> {
        private static final PBRLoader INSTANCE = new PBRLoader();

        static void register() {
            PBRTextureLoaderRegistry.INSTANCE.register(CustomPBRTextureSet.class, INSTANCE);
        }

        @Override
        public void load(CustomPBRTextureSet texture, ResourceManager resourceManager,
            PBRTextureLoader.PBRTextureConsumer consumer) {
            try {
                texture.loadPbr(resourceManager);
            } catch (IOException error) {
                YesSteveModel.LOGGER.error("Failed to load YSM PBR textures", error);
                return;
            }
            var normal = texture.getPBRTextures().get(PBRTextureType.NORMAL);
            if (normal != null) {
                consumer.acceptNormalTexture(normal);
                ResourceDiagnostics.track(ResourceDiagnostics.Kind.TEXTURE, normal, "pbr normal");
            }
            var specular = texture.getPBRTextures().get(PBRTextureType.SPECULAR);
            if (specular != null) {
                consumer.acceptSpecularTexture(specular);
                ResourceDiagnostics.track(ResourceDiagnostics.Kind.TEXTURE, specular, "pbr specular");
            }
        }
    }
}
