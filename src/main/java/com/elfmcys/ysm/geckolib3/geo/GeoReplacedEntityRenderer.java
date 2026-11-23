// Modified for the unofficial Fabric 26.2 port by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.geckolib3.geo;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.api.rendering.event.RenderLayerEvent;
import com.elfmcys.ysm.api.rendering.event.RenderModelEvent;
import com.elfmcys.ysm.capability.VehicleAnimatableCapabilityProvider;
import com.elfmcys.ysm.client.entity.CustomHumanoidEntity;
import com.elfmcys.ysm.client.gui.GuiModelFraming;
import com.elfmcys.ysm.geckolib3.core.util.Color;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

/**
 * Draws a YSM humanoid in place of the vanilla body.
 *
 * <p>
 * Fabric port (26.x): no longer a vanilla LivingEntityRenderer. The vanilla renderer still runs for name tag, leash,
 * shadow and fire; YSM submits the body and its layers to the frame's collector, and vanilla skips its own model and
 * layers for that render state (LivingEntityRendererMixin).
 */
public abstract class GeoReplacedEntityRenderer<TEntity extends LivingEntity, T extends CustomHumanoidEntity<TEntity>>
    implements IGeoRenderer<T> {
    protected final List<GeoLayerRenderer<T>> layerRenderers = new ObjectArrayList<>();

    public static int getPackedOverlay(LivingEntity entity, float u) {
        return OverlayTexture.pack(OverlayTexture.u(u), OverlayTexture.v(entity.hurtTime > 0 || entity.deathTime > 0));
    }

    /**
     * Updates and submits the replacement. handoff runs once YSM owns this draw (vanilla must then skip its body).
     *
     * @return true when YSM took over the draw
     */
    public boolean submitReplacement(T animatableEntity, LivingEntityRenderState state,
        @Nullable Identifier textureOverride, float partialTick, PoseStack poseStack, SubmitNodeCollector collector,
        @Nullable GuiModelFraming framing, Runnable handoff) {
        if (!animatableEntity.isPresentationAllowed())
            return false;
        var data = animatableEntity.update(partialTick);
        return submitPreparedReplacement(animatableEntity, data, state, textureOverride, partialTick, poseStack,
            collector, framing, handoff);
    }

    public boolean submitPreparedReplacement(T animatableEntity, GeoRenderData data, LivingEntityRenderState state,
        @Nullable Identifier textureOverride, float partialTick, PoseStack poseStack, SubmitNodeCollector collector,
        @Nullable GuiModelFraming framing, Runnable handoff) {
        if (!animatableEntity.isPresentationAllowed())
            return false;
        return animatableEntity.consumePrepared(data, borrowed -> {
            handoff.run();
            submitPrepared(animatableEntity, borrowed, state, textureOverride, partialTick, poseStack, collector,
                framing);
            return true;
        });
    }

    protected void submitPrepared(T animatableEntity, GeoRenderData data, LivingEntityRenderState state,
        @Nullable Identifier textureOverride, float partialTick, PoseStack poseStack, SubmitNodeCollector collector,
        @Nullable GuiModelFraming framing) {
        final TEntity entity = animatableEntity.getEntity();
        var mc = Minecraft.getInstance();
        if (data == null || mc.player == null)
            return;
        poseStack.pushPose();
        try {
            if (entity.getPose() == Pose.SLEEPING) {
                Direction direction = entity.getBedOrientation();
                if (direction != null) {
                    float eyeOffset = entity.getEyeHeight(Pose.STANDING) - 0.1f;
                    poseStack.translate(-direction.getStepX() * eyeOffset, 0, -direction.getStepZ() * eyeOffset);
                }
            }
            // 26.x: the scale attribute scales the whole entity, as vanilla does before rotations.
            float entityScale = state.scale;
            poseStack.scale(entityScale, entityScale, entityScale);
            setupRotations(entity, state, poseStack, data.animationData.lerpedAge, data.animationData.lerpBodyRot,
                entityScale);

            var vehicle = entity.getVehicle();
            if (vehicle != null) {
                vehicle.getCapability(VehicleAnimatableCapabilityProvider.CAP).ifPresent(cap -> {
                    cap.waitForRenderUpdate();
                    var rot = cap.getRotation();
                    if (rot != null)
                        poseStack.mulPose(new Quaternionf().rotateZYX(rot.z, 0, rot.x).invert());
                });
            }

            poseStack.translate(0, 0.01f, 0);

            var texture = textureOverride == null ? data.texture : textureOverride;
            var bodyVisible = !state.isInvisible && !entity.isInvisibleTo(mc.player);
            var glowing = state.appearsGlowing();
            var renderType = getRenderType(texture, bodyVisible, glowing, data.modelState.hasTranslucentVertices());
            var packedLight = state.lightCoords;
            var packedOverlay = getPackedOverlay(entity, getOverlayProgress(entity, partialTick));
            if (renderType == null)
                return;

            preRender(data, animatableEntity, poseStack, collector, packedLight, packedOverlay, Color.WHITE);
            if (framing != null && !framing.apply(data.installation, data.modelState, poseStack))
                return;
            boolean layers = !entity.isSpectator();
            if (data.renderLayersFirst && layers && animatableEntity.canConsume(data, data.ctx))
                renderLayer(poseStack, collector, animatableEntity, data, state, packedLight, packedOverlay);

            if (animatableEntity.canConsume(data, data.ctx)) {
                if (renderType.isOutline()) {
                    renderOutline(data, animatableEntity, renderType, poseStack, collector, state.outlineColor);
                } else {
                    var event = new RenderModelEvent(entity, data, collector, renderType, poseStack, packedLight,
                        packedOverlay, Color.WHITE.getColor());
                    if (!YesSteveModel.postEvent(event) && animatableEntity.canConsume(data, data.ctx)) {
                        render(data, animatableEntity, renderType, poseStack, collector, packedLight, packedOverlay,
                            Color.WHITE);
                        renderOutline(data, animatableEntity, renderType, poseStack, collector, state.outlineColor);
                    }
                }
            }

            if (!data.renderLayersFirst && layers && animatableEntity.canConsume(data, data.ctx))
                renderLayer(poseStack, collector, animatableEntity, data, state, packedLight, packedOverlay);
            if (animatableEntity.canConsume(data, data.ctx))
                postRender(data, animatableEntity, poseStack, collector, packedLight, packedOverlay, Color.WHITE);
        } finally {
            poseStack.popPose();
        }
    }

    protected void renderLayer(PoseStack poseStack, SubmitNodeCollector collector, T animatable,
        GeoRenderData renderData, LivingEntityRenderState state, int packedLight, int overlay) {
        var event = new RenderLayerEvent(animatable.getEntity(), renderData, poseStack, collector, packedLight, overlay);
        if (YesSteveModel.postEvent(event))
            return;
        for (GeoLayerRenderer<T> layerRenderer : layerRenderers) {
            if (!animatable.canConsume(renderData, renderData.ctx))
                break;
            poseStack.pushPose();
            try {
                layerRenderer.render(poseStack, collector, animatable, renderData, state, packedLight, overlay);
            } finally {
                poseStack.popPose();
            }
        }
    }

    protected float getOverlayProgress(TEntity entity, float partialTicks) {
        return 0.0F;
    }

    /**
     * Vanilla 26.x body rotations without the death fall and riptide spin, which YSM models animate themselves; a
     * climbing entity faces its ladder.
     */
    protected void setupRotations(TEntity entity, LivingEntityRenderState state, PoseStack poseStack,
        float ageInTicks, float bodyRot, float entityScale) {
        if (entity.onClimbable()) {
            var climbablePos = entity.getLastClimbablePos();
            if (climbablePos.isPresent()) {
                var blockState = entity.level().getBlockState(climbablePos.get());
                var facing = blockState.getOptionalValue(HorizontalDirectionalBlock.FACING);
                if (facing.isPresent())
                    bodyRot = facing.get().getOpposite().get2DDataValue() * 90;
            }
        }
        if (state.isFullyFrozen)
            bodyRot += (float)(Math.cos(Mth.floor(ageInTicks) * 3.25F) * Math.PI * 0.4F);
        if (!state.hasPose(Pose.SLEEPING))
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - bodyRot));
        if (state.hasPose(Pose.SLEEPING)) {
            Direction bedOrientation = state.bedOrientation;
            float angle = bedOrientation != null ? sleepDirectionToRotation(bedOrientation) : bodyRot;
            poseStack.mulPose(Axis.YP.rotationDegrees(angle));
            poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(270.0F));
        } else if (state.isUpsideDown) {
            poseStack.translate(0.0F, (state.boundingBoxHeight + 0.1F) / entityScale, 0.0F);
            poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        }
    }

    private static float sleepDirectionToRotation(Direction direction) {
        return switch (direction) {
            case SOUTH -> 90.0F;
            case NORTH -> 270.0F;
            case EAST -> 180.0F;
            default -> 0.0F;
        };
    }

    public final boolean addLayer(GeoLayerRenderer<T> layer) {
        return layerRenderers.add(layer);
    }
}
