package com.elfmcys.ysm.client.gui;

import com.elfmcys.ysm.client.animation.AnimationRegister;
import com.elfmcys.ysm.client.event.RegisterEntityRenderersEvent;
import com.elfmcys.ysm.model.resource.client.AcquireResult;
import com.elfmcys.ysm.model.resource.client.ModelRenderTarget;
import com.elfmcys.ysm.model.resource.client.ModelResourcePurpose;
import com.elfmcys.ysm.model.resource.client.ResourceLease;
import com.elfmcys.ysm.model.resource.client.ResourceRequest;
import com.elfmcys.ysm.proto.mixel.manifest.info.SettingsProto;
import com.elfmcys.ysm.util.RenderUtil;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.function.BiConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

/**
 * Draws one already-published player target into a private framebuffer, Minecraft before 1.21.6 (no
 * picture-in-picture GUI renderer): the model is painted right away, as the GUI paints it (same placement, scale 3),
 * into an offscreen framebuffer of the preview's size, and read back. While it is drawn, every render type draws into
 * that framebuffer (PreviewOutputMixin), Fabulous graphics' own framebuffers included.
 */
public final class PreviewRenderHost {
    public static final int WIDTH = 156;
    public static final int HEIGHT = 270;
    private static final int GUI_SCALE = 3;
    /** GUI-style depth range of the camera; the painter's drawing sits around z = 50 in front of it. */
    private static final float NEAR = 1000.0F;
    private static final float FAR = 21000.0F;
    private static TextureTarget framebuffer;
    @Nullable
    private static RenderTarget drawing;

    private PreviewRenderHost() {}

    /** Render thread: the framebuffer every draw goes to while a preview is drawn, otherwise null. */
    @Nullable
    public static RenderTarget drawingTarget() {
        return drawing;
    }

    /** Render thread. {@code done} receives the pixels (owned by the receiver) or the failure, exactly once. */
    public static void render(ResourceRequest request, ResourceLease lease, BiConsumer<NativeImage, Throwable> done) {
        RenderSystem.assertOnRenderThread();
        var acquired = lease.poll();
        if (!(acquired instanceof AcquireResult.Ready ready))
            throw new IllegalArgumentException("Preview rendering requires a ready target");
        var target = ready.target();
        if (target.playerResources() == null)
            throw new IllegalArgumentException("Preview rendering requires a player target");

        var entity = new CustomGuiPlayerEntity();
        try {
            entity.installPreviewResource(request, new BorrowedTargetLease(request, target));
            entity.getPreviewInfo().setPreview(AnimationRegister.IDLE);
            entity.getPreviewInfo().setHover(AnimationRegister.EMPTY);
            entity.getPreviewInfo().setFocus(AnimationRegister.EMPTY);
            drawEntity(entity, target);
        } finally {
            entity.reset();
        }
        readPixels(done);
    }

    private static TextureTarget framebuffer() {
        if (framebuffer == null) {
            //#if MC >= 1.21.5
            framebuffer = new TextureTarget("YSM preview", WIDTH, HEIGHT, true);
            //#elseif MC >= 1.21.2
            //$$ framebuffer = new TextureTarget(WIDTH, HEIGHT, true);
            //#else
            //$$ framebuffer = new TextureTarget(WIDTH, HEIGHT, true, Minecraft.ON_OSX);
            //#endif
            //#if MC < 1.21.5
            //$$ framebuffer.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
            //#endif
        }
        return framebuffer;
    }

    /** Transparent background, depth cleared. */
    private static void clear(TextureTarget target) {
        //#if MC >= 1.21.5
        RenderSystem.getDevice().createCommandEncoder()
            .clearColorAndDepthTextures(target.getColorTexture(), 0, target.getDepthTexture(), 1.0);
        //#elseif MC >= 1.21.2
        //$$ target.clear();
        //#else
        //$$ target.clear(Minecraft.ON_OSX);
        //#endif
    }

    private static void drawEntity(CustomGuiPlayerEntity entity, ModelRenderTarget target) {
        var minecraft = Minecraft.getInstance();
        var painter = RenderUtil.modelPainter(WIDTH / (float)GUI_SCALE / 2.0F, HEIGHT / (float)GUI_SCALE / 2.0F + 20.0F,
            30.0F, minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false), entity,
            RegisterEntityRenderersEvent.getPlayerRenderer(), disablePreviewRotation(target.info().getSettings()), true);
        var output = framebuffer();
        clear(output);
        // the GUI's camera over the picture's GUI units (52 x 90 at scale 3)
        var projection = new Matrix4f().setOrtho(0.0F, WIDTH / (float)GUI_SCALE, HEIGHT / (float)GUI_SCALE, 0.0F,
            NEAR, FAR);
        RenderSystem.backupProjectionMatrix();
        //#if MC >= 1.21.2
        RenderSystem.setProjectionMatrix(projection, com.mojang.blaze3d.ProjectionType.ORTHOGRAPHIC);
        //#else
        //$$ RenderSystem.setProjectionMatrix(projection, com.mojang.blaze3d.vertex.VertexSorting.ORTHOGRAPHIC_Z);
        //#endif
        //#if MC >= 1.20.5
        var view = RenderSystem.getModelViewStack();
        view.pushMatrix();
        view.translation(0.0F, 0.0F, -(NEAR + FAR) / 2.0F);
        //#else
        //$$ // before 1.20.5 the model-view stack is a PoseStack: the painter works on a matrix stack copied from it
        //$$ var poseView = RenderSystem.getModelViewStack();
        //$$ poseView.pushPose();
        //$$ poseView.setIdentity();
        //$$ poseView.translate(0.0F, 0.0F, -(NEAR + FAR) / 2.0F);
        //$$ var view = new org.joml.Matrix4fStack(16);
        //$$ view.set(poseView.last().pose());
        //#endif
        var buffers = minecraft.renderBuffers().bufferSource();
        drawing = output;
        try {
            //#if MC < 1.21.5
            //$$ output.bindWrite(true);
            //#endif
            com.elfmcys.ysm.client.gui.pip.YsmGuiModels.setupEntityLighting();
            try (var collector = new SubmitNodeCollector(buffers)) {
                painter.paint(view, collector);
                //#if MC < 1.20.5
                //$$ // before the collector draws: the native writer reads the model-view
                //$$ poseView.last().pose().set(view);
                //#endif
            }
            //#if MC < 1.21.2
            //$$ // before 1.21.2 the shaders see model-view changes only once applied
            //$$ RenderSystem.applyModelViewMatrix();
            //#endif
        } finally {
            try {
                // also after a failure: nothing of the preview may stay batched for the next screen draw
                buffers.endBatch();
            } finally {
                drawing = null;
                //#if MC >= 1.20.5
                view.popMatrix();
                //#else
                //$$ poseView.popPose();
                //#endif
                //#if MC < 1.21.2
                //$$ RenderSystem.applyModelViewMatrix();
                //#endif
                RenderSystem.restoreProjectionMatrix();
                Lighting.setupFor3DItems();
                //#if MC < 1.21.5
                //$$ output.unbindWrite();
                //$$ minecraft.getMainRenderTarget().bindWrite(true);
                //#endif
            }
        }
    }

    private static void readPixels(BiConsumer<NativeImage, Throwable> done) {
        //#if MC >= 1.21.5
        // as the 1.21.5 screenshot: a GPU copy into a buffer, read when the GPU has finished it (render thread)
        var texture = framebuffer.getColorTexture();
        var device = RenderSystem.getDevice();
        int pixelSize = texture.getFormat().pixelSize();
        var buffer = device.createBuffer(() -> "YSM preview readback", com.mojang.blaze3d.buffers.BufferType.PIXEL_PACK,
            com.mojang.blaze3d.buffers.BufferUsage.STATIC_READ, WIDTH * HEIGHT * pixelSize);
        var encoder = device.createCommandEncoder();
        encoder.copyTextureToBuffer(texture, buffer, 0, () -> {
            NativeImage pixels = null;
            Throwable failure = null;
            try (var view = encoder.readBuffer(buffer)) {
                pixels = new NativeImage(WIDTH, HEIGHT, false);
                var data = view.data();
                // Framebuffer rows are bottom-up; alpha is kept (transparent background).
                for (int y = 0; y < HEIGHT; y++)
                    for (int x = 0; x < WIDTH; x++)
                        pixels.setPixelABGR(x, HEIGHT - 1 - y, data.getInt((x + y * WIDTH) * pixelSize));
            } catch (RuntimeException | Error error) {
                failure = error;
                if (pixels != null) {
                    pixels.close();
                    pixels = null;
                }
            } finally {
                buffer.close();
            }
            done.accept(pixels, failure);
        }, 0);
        //#else
        //$$ // OpenGL waits for the drawing: the texture is read right away
        //$$ var pixels = new NativeImage(WIDTH, HEIGHT, false);
        //$$ try {
        //$$     RenderSystem.bindTexture(framebuffer.getColorTextureId());
        //$$     // alpha kept (transparent background); framebuffer rows are bottom-up
        //$$     pixels.downloadTexture(0, false);
        //$$     pixels.flipY();
        //$$ } catch (RuntimeException | Error failure) {
        //$$     pixels.close();
        //$$     done.accept(null, failure);
        //$$     return;
        //$$ }
        //$$ done.accept(pixels, null);
        //#endif
    }

    static boolean disablePreviewRotation(SettingsProto settings) {
        // An authored GUI pose may supply its own orientation; idle needs the ordinary view instead.
        var customPreview = settings.previewAnimation()
            .filter(name -> !name.isBlank() && !name.equals(AnimationRegister.IDLE)).isPresent();
        return settings.disablePreviewRotation() && !customPreview;
    }

    private record BorrowedTargetLease(ResourceRequest request, ModelRenderTarget target) implements ResourceLease {
        @Override
        public ModelResourcePurpose purpose() {
            return request.purpose();
        }

        @Override
        public AcquireResult poll() {
            return new AcquireResult.Ready(target);
        }

        @Override
        public boolean isCurrent(ResourceRequest candidate) {
            return request.equals(candidate);
        }

        @Override
        public void cancelPending() {}

        @Override
        public void close() {}
    }
}
