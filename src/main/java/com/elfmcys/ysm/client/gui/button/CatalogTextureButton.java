// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.gui.button;

import com.elfmcys.ysm.client.gui.GuiDraw;
import com.elfmcys.ysm.client.animation.AnimationRegister;
import com.elfmcys.ysm.client.event.RegisterEntityRenderersEvent;
import com.elfmcys.ysm.client.gui.CustomGuiPlayerEntity;
import com.elfmcys.ysm.client.gui.GuiModelFraming;
import com.elfmcys.ysm.client.lang.LanguageManager;
import com.elfmcys.ysm.client.demand.ModelResourceDemand;
import com.elfmcys.ysm.model.resource.client.ModelRenderTarget;
import com.elfmcys.ysm.model.domain.ModelId;
import com.elfmcys.ysm.util.RenderUtil;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class CatalogTextureButton extends Button
    implements com.elfmcys.ysm.client.demand.ClientDemandCoordinator.CardDisplay, AutoCloseable {
    private static final int CARD_OVERLAY_Z = 3500;

    private final ModelId modelHash;
    private final String path;
    private final String texture;
    private final CustomGuiPlayerEntity entity;
    private final SelectionHandler selection;
    private final com.elfmcys.ysm.client.demand.ClientDemandCoordinator.PageScope page;
    private final GuiModelFraming framing = new GuiModelFraming();
    private @Nullable ModelRenderTarget renderTarget;
    private @Nullable Throwable error;
    private boolean closed;
    private boolean displaying;

    public CatalogTextureButton(int x, int y, ModelId modelHash, String path, String texture,
        CustomGuiPlayerEntity entity, com.elfmcys.ysm.client.demand.ClientDemandCoordinator.PageScope page,
        SelectionHandler selection) {
        super(x, y, 54, 102, Component.literal(texture), ignored -> {
        }, DEFAULT_NARRATION);
        this.modelHash = modelHash;
        this.path = path;
        this.texture = texture;
        this.entity = entity;
        this.selection = selection;
        this.page = page;
        page.card(this, target());
    }

    public com.elfmcys.ysm.client.demand.ClientDemandCoordinator.Target target() {
        return new com.elfmcys.ysm.client.demand.ClientDemandCoordinator.Target(modelHash, texture);
    }

    @Override
    public void activate(ModelResourceDemand.Mode mode) {
        if (closed)
            return;
        entity.setResourceDemandMode(mode);
        if (!displaying) {
            entity.getPreviewInfo().setPreview(AnimationRegister.IDLE);
            entity.updateModelAndTexture(modelHash, texture);
            entity.demandScope();
            displaying = true;
        }
    }

    @Override
    public void deactivate() {
        if (!displaying)
            return;
        displaying = false;
        renderTarget = null;
        error = null;
        entity.revokeAssignment();
    }

    @Override
    //#if MC >= 1.21.9
    public void onPress(net.minecraft.client.input.InputWithModifiers input) {
    //#else
    //$$ public void onPress() {
    //$$     var input = com.elfmcys.ysm.legacy.input.InputWithModifiers.current();
    //#endif
        selection.select(modelHash, path, texture, renderTarget);
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        updateLazyFailure();
        graphics.fillGradient(getX(), getY(), getX() + width, getY() + height, 0xFF434242, 0xFF434242);
        if (renderTarget == null || error != null) {
            var text = error == null ? ".".repeat((int)((Util.getMillis() / 250L) % 4L)) : "!";
            GuiDraw.drawCenteredString(graphics, Minecraft.getInstance().font, text, getX() + width / 2, getY() + (height - 20) / 2, error == null ? 0xFFF3EFE0 : 0xFFFF5555);
        } else {
            renderEntity(graphics);
        }
        renderName(graphics);
        if (isHoveredOrFocused()) {
            graphics.fillGradient(getX(), getY() + 1, getX() + 1, getY() + height - 1, 0xFFF3EFE0, 0xFFF3EFE0);
            graphics.fillGradient(getX(), getY(), getX() + width, getY() + 1, 0xFFF3EFE0, 0xFFF3EFE0);
            graphics.fillGradient(getX() + width - 1, getY() + 1, getX() + width, getY() + height - 1, 0xFFF3EFE0, 0xFFF3EFE0);
            graphics.fillGradient(getX(), getY() + height - 1, getX() + width, getY() + height, 0xFFF3EFE0, 0xFFF3EFE0);
        }
    }

    private void updateLazyFailure() {
        if (closed)
            return;
        renderTarget = entity.displayedModel();
        error = entity.modelLoadFailure();
        if (error != null || renderTarget == null || renderTarget.playerResources() == null) {
            return;
        }
        var resources = renderTarget.playerResources();
        if (resources.animations().hasFailures() || resources.fpArmAnimations().hasFailures()) {
            error = new IllegalStateException("One or more preview animations failed to load");
        }
        if (error != null) {
            renderTarget = null;
        }
    }

    @Override
    public void close() {
        closed = true;
        try {
            page.removeCard(this);
            deactivate();
        } finally {
            entity.retire();
        }
    }

    private void renderEntity(GuiGraphicsExtractor graphics) {
        // Fabric port: the framed model is a GUI picture of the card above its footer (was a scissor).
        RenderUtil.renderFramedModelInGui(graphics, getX(), getY(), width, height - 20,
            Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false), entity,
            RegisterEntityRenderersEvent.getPlayerRenderer(), false, true, framing);
    }

    private void renderName(GuiGraphicsExtractor graphics) {
        Font font = Minecraft.getInstance().font;
        var name = renderTarget == null ? texture
            : LanguageManager.getI18n(renderTarget, "files.player.texture.%s".formatted(texture), texture);
        List<FormattedCharSequence> split = font.split(Component.literal(name), 50);
        if (split.size() > 1) {
            GuiDraw.drawCenteredString(graphics, font, split.get(0), getX() + width / 2, getY() + height - 19, 0xF3EFE0);
            GuiDraw.drawCenteredString(graphics, font, split.get(1), getX() + width / 2, getY() + height - 10, 0xF3EFE0);
        } else {
            GuiDraw.drawCenteredString(graphics, font, Component.literal(name), getX() + width / 2, getY() + height - 15, 0xF3EFE0);
        }
    }

    @FunctionalInterface
    public interface SelectionHandler {
        void select(ModelId hash, String path, String texture, @Nullable ModelRenderTarget renderTarget);
    }
}
