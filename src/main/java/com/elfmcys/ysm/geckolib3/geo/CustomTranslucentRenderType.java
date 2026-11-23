// Modified for the unofficial Fabric 26.2 port by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.geckolib3.geo;

import com.elfmcys.ysm.mixin.fabric.client.RenderTypeInvoker;
import java.util.function.Function;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

/**
 * Vanilla entity_translucent without upload-time sorting: the native renderer already emits translucent quads in
 * depth order, which host sorting would undo.
 */
public final class CustomTranslucentRenderType {
    private static final Function<Identifier, RenderType> CUSTOM_TRANSLUCENT =
        Util.memoize(CustomTranslucentRenderType::build);

    private CustomTranslucentRenderType() {}

    public static RenderType create(Identifier textureLocation) {
        return CUSTOM_TRANSLUCENT.apply(textureLocation);
    }

    private static RenderType build(Identifier texture) {
        var setup = RenderSetup.builder(RenderPipelines.ENTITY_TRANSLUCENT).withTexture("Sampler0", texture)
            .useLightmap().useOverlay().affectsCrumbling().setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE)
            .createRenderSetup();
        return RenderTypeInvoker.ysm$create("entity_translucent_ysm", setup);
    }
}
