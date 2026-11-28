package com.elfmcys.ysm.geckolib3.geo;

import java.util.Optional;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Minecraft before 1.21.5 (no render pipelines): vanilla entity_translucent without upload-time sorting, made the way
 * the upstream 1.20.1 YSM made it (a render type that reuses the vanilla one's state with sorting off). The native
 * renderer already emits translucent quads in depth order, which host sorting would undo.
 */
public class CustomTranslucentRenderType extends RenderType {
    private static final Function<ResourceLocation, CustomTranslucentRenderType> CUSTOM_TRANSLUCENT =
        Util.memoize(CustomTranslucentRenderType::new);

    private final boolean isOutline;
    private final Optional<RenderType> outline;

    private CustomTranslucentRenderType(ResourceLocation textureLocation) {
        this(RenderType.entityTranslucent(textureLocation));
    }

    private CustomTranslucentRenderType(RenderType inner) {
        super("entity_translucent_ysm", inner.format(), inner.mode(), inner.bufferSize(), inner.affectsCrumbling(),
            false, inner::setupRenderState, inner::clearRenderState);
        this.isOutline = inner.isOutline();
        this.outline = inner.outline();
    }

    @Override
    public boolean isOutline() {
        return isOutline;
    }

    @Override
    @NotNull
    public Optional<RenderType> outline() {
        return outline;
    }

    public static CustomTranslucentRenderType create(ResourceLocation textureLocation) {
        return CUSTOM_TRANSLUCENT.apply(textureLocation);
    }
}
