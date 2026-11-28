// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.geckolib3.geo;

import com.elfmcys.ysm.mixin.fabric.client.RenderTypeInvoker;
import java.util.function.Function;
import net.minecraft.client.renderer.RenderPipelines;
//#if MC >= 1.21.11
import net.minecraft.client.renderer.rendertype.RenderSetup;
//#endif
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
        //#if MC >= 1.21.11
        var setup = RenderSetup.builder(RenderPipelines.ENTITY_TRANSLUCENT).withTexture("Sampler0", texture)
            .useLightmap().useOverlay().affectsCrumbling().setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE)
            .createRenderSetup();
        return RenderTypeInvoker.ysm$create("entity_translucent_ysm", setup);
        //#else
        //$$ // 1.21.5 - 1.21.10: vanilla entity_translucent's composite state, without sorting on upload
        //$$ var state = RenderType.CompositeState.builder()
        //#if MC >= 1.21.6
        //$$     .setTextureState(new net.minecraft.client.renderer.RenderStateShard.TextureStateShard(texture, false))
        //#else
        //$$     .setTextureState(new net.minecraft.client.renderer.RenderStateShard.TextureStateShard(texture,
        //$$         net.minecraft.util.TriState.FALSE, false))
        //#endif
        //$$     .setLightmapState(net.minecraft.client.renderer.RenderStateShard.LIGHTMAP)
        //$$     .setOverlayState(net.minecraft.client.renderer.RenderStateShard.OVERLAY).createCompositeState(true);
        //$$ return RenderType.create("entity_translucent_ysm", 1536, true, false, RenderPipelines.ENTITY_TRANSLUCENT, state);
        //#endif
    }
}
