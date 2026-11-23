// Modified for the unofficial Fabric 26.2 port by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.gui;

import com.elfmcys.ysm.client.animation.AnimationRegister;
import com.elfmcys.ysm.client.event.RegisterEntityRenderersEvent;
import com.elfmcys.ysm.client.gui.pip.YsmGuiModelRenderState;
import com.elfmcys.ysm.client.gui.pip.YsmGuiModelRenderer;
import com.elfmcys.ysm.mixin.fabric.client.PictureInPictureRendererAccessor;
import com.elfmcys.ysm.model.resource.client.AcquireResult;
import com.elfmcys.ysm.model.resource.client.ModelRenderTarget;
import com.elfmcys.ysm.model.resource.client.ModelResourcePurpose;
import com.elfmcys.ysm.model.resource.client.ResourceLease;
import com.elfmcys.ysm.model.resource.client.ResourceRequest;
import com.elfmcys.ysm.proto.mixel.manifest.info.SettingsProto;
import com.elfmcys.ysm.util.RenderUtil;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.function.BiConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import org.joml.Matrix3x2f;

/**
 * Draws one already-published player target into a private host framebuffer.
 *
 * <p>
 * Fabric port (26.x): drawn through a private picture-in-picture renderer; the pixels are copied back when the GPU has
 * finished the frame (26.x cannot wait on the current frame), so the result is delivered to a callback on the render
 * thread.
 */
public final class PreviewRenderHost {
    public static final int WIDTH = 156;
    public static final int HEIGHT = 270;
    private static final int GUI_SCALE = 3;
    private static PreviewRenderer renderer;

    private PreviewRenderHost() {}

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

    private static void drawEntity(CustomGuiPlayerEntity entity, ModelRenderTarget target) {
        var minecraft = Minecraft.getInstance();
        if (renderer == null)
            renderer = new PreviewRenderer();
        var painter = RenderUtil.modelPainter(WIDTH / (float)GUI_SCALE / 2.0F, HEIGHT / (float)GUI_SCALE / 2.0F + 20.0F,
            30.0F, minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false), entity,
            RegisterEntityRenderersEvent.getPlayerRenderer(), disablePreviewRotation(target.info().getSettings()), true);
        var state = YsmGuiModelRenderState.create(new Matrix3x2f(), 0, 0, WIDTH / GUI_SCALE, HEIGHT / GUI_SCALE, null,
            GUI_SCALE, painter);
        RenderSystem.backupProjectionMatrix();
        try {
            renderer.prepare(state, null, minecraft.gameRenderer.featureRenderDispatcher(), GUI_SCALE);
        } finally {
            RenderSystem.restoreProjectionMatrix();
        }
    }

    private static void readPixels(BiConsumer<NativeImage, Throwable> done) {
        var texture = ((PictureInPictureRendererAccessor)(Object)renderer).ysm$texture();
        if (texture == null)
            throw new IllegalStateException("Preview framebuffer was not created");
        var device = RenderSystem.getDevice();
        int blockSize = texture.getFormat().blockSize();
        var buffer = device.createBuffer(() -> "YSM preview readback", 9, (long)WIDTH * HEIGHT * blockSize);
        device.createCommandEncoder().copyTextureToBuffer(texture, buffer, 0L, () -> {
            NativeImage pixels = null;
            Throwable failure = null;
            try (var view = buffer.map(true, false)) {
                pixels = new NativeImage(WIDTH, HEIGHT, false);
                var data = view.data();
                // Framebuffer rows are bottom-up; alpha is kept (transparent background).
                for (int y = 0; y < HEIGHT; y++)
                    for (int x = 0; x < WIDTH; x++)
                        pixels.setPixelABGR(x, HEIGHT - 1 - y, data.getInt((x + y * WIDTH) * blockSize));
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
    }

    static boolean disablePreviewRotation(SettingsProto settings) {
        // An authored GUI pose may supply its own orientation; idle needs the ordinary view instead.
        var customPreview = settings.previewAnimation()
            .filter(name -> !name.isBlank() && !name.equals(AnimationRegister.IDLE)).isPresent();
        return settings.disablePreviewRotation() && !customPreview;
    }

    /** The GUI picture renderer without the blit into a GUI (the picture is read back instead). */
    private static final class PreviewRenderer extends YsmGuiModelRenderer {
        @Override
        protected void blitTexture(YsmGuiModelRenderState renderState, GuiRenderState guiRenderState) {}

        @Override
        protected String getTextureLabel() {
            return "ysm_preview";
        }
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
