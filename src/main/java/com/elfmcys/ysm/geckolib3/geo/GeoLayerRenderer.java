// Modified for the unofficial Fabric 26.2 port by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.geckolib3.geo;

import com.elfmcys.ysm.client.entity.AnimatableEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

/** Fabric port (26.x): layers submit to the frame's collector and may read the host render state. */
public abstract class GeoLayerRenderer<T extends AnimatableEntity<?>> {
    public GeoLayerRenderer() {}

    public abstract void render(PoseStack poseStack, SubmitNodeCollector collector, T animatable,
        GeoRenderData renderData, LivingEntityRenderState state, int packedLight, int overlay);
}
