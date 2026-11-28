// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.gui.button;

import com.elfmcys.ysm.client.gui.GuiDraw;
import com.elfmcys.ysm.config.LoadingStateScreenConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class PositionButton extends Button {
    public PositionButton(int x, int y) {
        super(x, y, 100, 20, Component.empty(), b -> {
        }, DEFAULT_NARRATION);
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Fabric port: the vanilla button look (AbstractButton.extractContents is abstract in 26.x).
        //#if MC >= 1.21.11
        this.extractDefaultSprite(guiGraphics);
        this.extractDefaultLabel(guiGraphics.textRendererForWidget(this, net.minecraft.client.gui.GuiGraphicsExtractor.HoveredTextEffects.NONE));
        //#else
        //$$ // before 1.21.11 the vanilla button draws its sprite and label in renderWidget
        //$$ super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        //#endif

        Font font = Minecraft.getInstance().font;
        MutableComponent desc = Component.translatable("gui.yes_steve_model.config.loading_state_position");
        GuiDraw.drawString(guiGraphics, font, desc, this.getX() + 105, this.getY() + 6, 0xFFFFFF, false);
    }

    @Override
    public Component getMessage() {
        LoadingStateScreenConfig.Position position = LoadingStateScreenConfig.LOADING_STATE_POSITION.get();
        return Component.literal(position.name());
    }

    @Override
    //#if MC >= 1.21.9
    public void onPress(net.minecraft.client.input.InputWithModifiers input) {
    //#else
    //$$ public void onPress() {
    //$$     var input = com.elfmcys.ysm.legacy.input.InputWithModifiers.current();
    //#endif
        LoadingStateScreenConfig.Position position = LoadingStateScreenConfig.LOADING_STATE_POSITION.get();
        LoadingStateScreenConfig.Position nextPosition = switch (position) {
            case TOP_LEFT -> LoadingStateScreenConfig.Position.TOP_CENTER;
            case TOP_CENTER -> LoadingStateScreenConfig.Position.TOP_RIGHT;
            case TOP_RIGHT -> LoadingStateScreenConfig.Position.BOTTOM_RIGHT;
            case BOTTOM_RIGHT -> LoadingStateScreenConfig.Position.BOTTOM_CENTER;
            case BOTTOM_CENTER -> LoadingStateScreenConfig.Position.BOTTOM_LEFT;
            case BOTTOM_LEFT -> LoadingStateScreenConfig.Position.TOP_LEFT;
        };
        LoadingStateScreenConfig.LOADING_STATE_POSITION.set(nextPosition);
        LoadingStateScreenConfig.LOADING_STATE_POSITION.save();
    }
}
