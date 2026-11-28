/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.legacy.render;

import com.elfmcys.ysm.legacy.submit.mixin.EntityRendererLeashInvoker;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

/**
 * Minecraft before 1.21.9 draws entities straight into a MultiBufferSource. This stand-in (mapped from
 * net.minecraft.client.renderer.SubmitNodeCollector by versions/below-1.21.9.remap) offers the part of 1.21.9's
 * collector YSM uses. Like 1.21.9's collector it only records the submissions (with the pose of the moment); they are
 * drawn by {@link #flush()}, which whoever created the collector calls once YSM's draw call has returned. YSM's model
 * drawing relies on that: the geometry callback borrows the prepared model again, which it may not do while the
 * submitting draw still holds it.
 */
public final class SubmitNodeCollector implements AutoCloseable {
    /** 1.21.9's SubmitNodeCollector.CustomGeometryRenderer. */
    @FunctionalInterface
    public interface CustomGeometryRenderer {
        void render(PoseStack.Pose pose, VertexConsumer buffer);
    }

    private final MultiBufferSource buffers;
    private final List<Runnable> pending = new ArrayList<>();

    public SubmitNodeCollector(MultiBufferSource buffers) {
        this.buffers = buffers;
    }

    /** The buffers the submissions are drawn into (calls into vanilla renderers of these versions take them). */
    public MultiBufferSource buffers() {
        return buffers;
    }

    /** Draws everything submitted so far, in order (submissions made while drawing are drawn too). */
    public void flush() {
        while (!pending.isEmpty()) {
            var batch = new ArrayList<>(pending);
            pending.clear();
            batch.forEach(Runnable::run);
        }
    }

    @Override
    public void close() {
        flush();
    }

    private static PoseStack.Pose copyOf(PoseStack.Pose pose) {
        //#if MC >= 1.20.5
        return pose.copy();
        //#else
        //$$ // before 1.20.5 a pose has no copy(): set a fresh one
        //$$ var stack = new PoseStack();
        //$$ stack.last().pose().set(pose.pose());
        //$$ stack.last().normal().set(pose.normal());
        //$$ return stack.last();
        //#endif
    }

    private static PoseStack stackAt(PoseStack.Pose pose) {
        var stack = new PoseStack();
        stack.last().pose().set(pose.pose());
        stack.last().normal().set(pose.normal());
        return stack;
    }

    public void submitCustomGeometry(PoseStack poseStack, RenderType renderType, CustomGeometryRenderer renderer) {
        var pose = copyOf(poseStack.last());
        pending.add(() -> renderer.render(pose, buffers.getBuffer(renderType)));
    }

    /** Draws with the buffers at the pose of the moment, when flushed (models posed by the caller). */
    public void submitDeferred(PoseStack poseStack, java.util.function.BiConsumer<PoseStack, MultiBufferSource> draw) {
        var pose = copyOf(poseStack.last());
        pending.add(() -> draw.accept(stackAt(pose), buffers));
    }

    //#if MC >= 1.21.2
    /** An entity model posed by its render state (1.21.9's outline and crumbling arguments have no use here). */
    public <S> void submitModel(EntityModel<? super S> model, S state, PoseStack poseStack, RenderType renderType,
        int light, int overlay, int outlineColor, @Nullable Object crumbling) {
        var pose = copyOf(poseStack.last());
        pending.add(() -> {
            model.setupAnim(state);
            model.renderToBuffer(stackAt(pose), buffers.getBuffer(renderType), light, overlay);
        });
    }
    //#endif

    public void submitBlock(PoseStack poseStack, BlockState block, int light, int overlay, int outlineColor) {
        var pose = copyOf(poseStack.last());
        pending.add(() -> Minecraft.getInstance().getBlockRenderer().renderSingleBlock(block, stackAt(pose), buffers,
            light, overlay));
    }

    public void submitLeash(PoseStack poseStack, EntityRenderState.LeashState leash) {
        var pose = copyOf(poseStack.last());
        //#if MC >= 1.21.2
        pending.add(() -> EntityRendererLeashInvoker.ysm$renderLeash(stackAt(pose), buffers, leash));
        //#else
        //$$ // before 1.21.2 the leash is drawn by the entity's renderer from the entity and its holder
        //$$ var renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(leash.entity);
        //#if MC >= 1.21
        //$$ pending.add(() -> ((EntityRendererLeashInvoker)(Object)renderer).ysm$renderLeash(leash.entity, leash.partialTick,
        //$$     stackAt(pose), buffers, leash.holder));
        //#else
        //$$ // before 1.21 only mobs are leashed (MobRenderer draws it)
        //$$ pending.add(() -> ((EntityRendererLeashInvoker)(Object)renderer).ysm$renderLeash(
        //$$     (net.minecraft.world.entity.Mob)leash.entity, leash.partialTick, stackAt(pose), buffers, leash.holder));
        //#endif
        //#endif
    }

    /** A name tag as EntityRenderer.renderNameTag draws it in these versions (distance was checked when extracted). */
    public void submitNameTag(PoseStack poseStack, @Nullable Vec3 attachment, int offsetY, Component text,
        boolean seeThrough, int light, double distanceToCameraSq, CameraRenderState camera) {
        if (attachment == null)
            return;
        var pose = copyOf(poseStack.last());
        var orientation = new Quaternionf(camera.orientation);
        pending.add(() -> {
            var stack = stackAt(pose);
            stack.translate(attachment.x, attachment.y + 0.5, attachment.z);
            stack.mulPose(orientation);
            stack.scale(0.025F, -0.025F, 0.025F);
            var matrix = stack.last().pose();
            Font font = Minecraft.getInstance().font;
            float x = -font.width(text) / 2.0F;
            int background = (int)(Minecraft.getInstance().options.getBackgroundOpacity(0.25F) * 255.0F) << 24;
            font.drawInBatch(text, x, offsetY, 0x80FFFFFF, false, matrix, buffers,
                seeThrough ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL, background, light);
            if (seeThrough)
                //#if MC >= 1.21.2
                font.drawInBatch(text, x, offsetY, 0xFFFFFFFF, false, matrix, buffers, Font.DisplayMode.NORMAL, 0,
                    LightTexture.lightCoordsWithEmission(light, 2));
                //#else
                //$$ font.drawInBatch(text, x, offsetY, 0xFFFFFFFF, false, matrix, buffers, Font.DisplayMode.NORMAL, 0, light);
                //#endif
        });
    }
}
