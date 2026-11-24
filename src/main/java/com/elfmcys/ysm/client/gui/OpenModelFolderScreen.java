// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.gui;

import com.elfmcys.ysm.model.catalog.source.ModelCatalogSources;
import net.minecraft.util.Util;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class OpenModelFolderScreen extends ModelScreen {
    private final PlayerModelScreen screen;

    protected OpenModelFolderScreen(PlayerModelScreen screen) {
        super(Component.literal("Open Model Folder"), screen);
        this.screen = screen;
    }

    @Override
    protected void init() {
        int x = (width - 310) / 2;
        int y = height / 2 + 60;
        this.clearWidgets();
        this.addRenderableWidget(
            Button.builder(Component.translatable("gui.yes_steve_model.open_model_folder.open"), b -> {
                com.elfmcys.ysm.platform.McCompat.openPath(ModelCatalogSources.modelsPath());
            }).bounds(x, y, 150, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.yes_steve_model.model.return"), b -> {
            showScreen(this.screen);
        }).bounds(x + 160, y, 150, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int pMouseX, int pMouseY, float pPartialTick) {
        GuiDraw.drawWordWrap(graphics, font, Component.translatable("gui.yes_steve_model.open_model_folder.tips"), (width - 400) / 2, height / 2 - 80, 400, 0XFFFFFF);
        super.extractRenderState(graphics, pMouseX, pMouseY, pPartialTick);
    }
}
