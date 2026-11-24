// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.geckolib3.geo;

import com.elfmcys.ysm.client.entity.AnimatableEntity;
import com.elfmcys.ysm.geckolib3.core.util.Color;
import com.elfmcys.ysm.natives.render.NativeRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * Adapts one prepared entity result to the current host draw.
 *
 * <p>
 * Fabric port (26.x): the host draws later from submitted nodes. {@link #render} submits custom geometry whose
 * callback re-enters the animatable's consumption window ({@link AnimatableEntity#consumePrepared}) when the host
 * writes vertices, so the draw-time validity checks of the original contract still hold.
 *
 * <p>
 * Execution: callers invoke hooks synchronously on the host render thread and isolate pose-stack changes.
 * NativeRenderer's global serial, non-reentrant contract holds at vertex-write time (the host builds groups serially).
 */
public interface IGeoRenderer<T extends AnimatableEntity<?>> {
    /** Full-bright light used for outline passes, as vanilla. */
    int OUTLINE_LIGHT = 15728880;

    /** Dev diagnostics (YSM_DEBUG_PIP): log skipped deferred draws (first 40). */
    boolean DRAW_DEBUG = System.getenv("YSM_DEBUG_PIP") != null;
    int[] drawDebugBudget = {40};

    /** The default hook scales the current pose; the caller owns restoration of that pose-stack frame. */
    default void preRender(GeoRenderData data, T animatable, PoseStack poseStack, SubmitNodeCollector collector,
        int packedLight, int packedOverlay, Color color) {
        poseStack.scale(data.widthScale, data.heightScale, data.widthScale);
    }

    /**
     * Submits the model for drawing. Returns false when the data is no longer consumable at submit time; a later
     * revocation makes the deferred draw a no-op.
     */
    default boolean render(GeoRenderData data, T animatable, RenderType type, PoseStack poseStack,
        SubmitNodeCollector collector, int packedLight, int packedOverlay, Color color) {
        if (!animatable.canConsume(data, data.ctx)) {
            if (DRAW_DEBUG && drawDebugBudget[0]-- > 0)
                com.elfmcys.ysm.YesSteveModel.LOGGER.info("[draw] refused at submit: {}", animatable.consumptionDebug(data));
            return false;
        }
        int argb = color.getColor();
        collector.submitCustomGeometry(poseStack, type, (pose, buffer) -> {
            boolean drawn = animatable.consumePrepared(data, borrowed -> NativeRenderer.render(buffer, pose,
                borrowed.modelState.getNativeState(), packedLight, packedOverlay, argb, borrowed.ctx.nativeType()));
            if (!drawn && DRAW_DEBUG && drawDebugBudget[0]-- > 0)
                com.elfmcys.ysm.YesSteveModel.LOGGER.info("[draw] skipped deferred model draw: {}",
                    animatable.consumptionDebug(data));
        });
        return true;
    }

    /** Submits the glow outline pass (vanilla draws outlines from a second, outline-type submission). */
    default void renderOutline(GeoRenderData data, T animatable, RenderType type, PoseStack poseStack,
        SubmitNodeCollector collector, int outlineColor) {
        if (outlineColor == 0)
            return;
        var outline = type.isOutline() ? type : type.outline().orElse(null);
        if (outline == null)
            return;
        collector.submitCustomGeometry(poseStack, outline,
            (pose, buffer) -> animatable.consumePrepared(data, borrowed -> NativeRenderer.render(buffer, pose,
                borrowed.modelState.getNativeState(), OUTLINE_LIGHT, OverlayTexture.NO_OVERLAY, outlineColor,
                borrowed.ctx.nativeType())));
    }

    /** Records the render observation; this hook does not certify successful vertex consumption. */
    default void postRender(GeoRenderData data, T animatable, PoseStack poseStack, SubmitNodeCollector collector,
        int packedLight, int packedOverlay, Color color) {
        animatable.countRender();
    }

    /** @return the host render state, or null when the subject is neither visible nor glowing */
    @Nullable
    default RenderType getRenderType(Identifier texture, boolean visible, boolean glowing, boolean translucent) {
        if (visible) {
            return translucent ? CustomTranslucentRenderType.create(texture) : RenderTypes.entityCutout(texture);
        }
        return glowing ? RenderTypes.outline(texture) : null;
    }
}
