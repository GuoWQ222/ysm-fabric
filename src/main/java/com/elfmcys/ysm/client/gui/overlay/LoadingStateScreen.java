// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.gui.overlay;

import net.minecraft.client.Minecraft;

import com.elfmcys.ysm.client.gui.GuiDraw;
import com.elfmcys.ysm.client.gui.CatalogProgressText;
import com.elfmcys.ysm.config.LoadingStateScreenConfig;
import com.elfmcys.ysm.model.catalog.CatalogScanProgress;
import com.elfmcys.ysm.model.service.ClientModelService;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public class LoadingStateScreen implements HudLayer {
    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, net.minecraft.client.DeltaTracker deltaTracker) {
        int screenWidth = graphics.guiWidth(), screenHeight = graphics.guiHeight();
        var font = Minecraft.getInstance().font;
        if (LoadingStateScreenConfig.DISABLE_LOADING_STATE_SCREEN.get())
            return;
        var service = ClientModelService.current().orElse(null);
        if (service == null)
            return;
        var progress = service.catalogProgress();
        if (progress == null)
            return;
        var color = switch (progress.phase()) {
            case DISCOVERING -> ChatFormatting.LIGHT_PURPLE;
            case PROCESSING -> ChatFormatting.GREEN;
            case FINALIZING -> ChatFormatting.GOLD;
        };
        var text = Component.translatable("gui.yes_steve_model.sync_hint.title")
            .append(CatalogProgressText.of(progress).copy().withStyle(color));
        var position = LoadingStateScreenConfig.LOADING_STATE_POSITION.get();
        var textWidth = font.width(text);
        var x = switch (position) {
            case TOP_LEFT, BOTTOM_LEFT -> 10;
            case TOP_CENTER, BOTTOM_CENTER -> (screenWidth - textWidth) / 2;
            case TOP_RIGHT, BOTTOM_RIGHT -> screenWidth - 10 - textWidth;
        };
        var y = switch (position) {
            case TOP_LEFT, TOP_CENTER, TOP_RIGHT -> 10;
            case BOTTOM_LEFT, BOTTOM_RIGHT -> screenHeight - 30;
            case BOTTOM_CENTER -> screenHeight - 85;
        };
        GuiDraw.drawString(graphics, font, text, x, y, 0xFFFFFF);
        if (progress.phase() == CatalogScanProgress.Phase.PROCESSING && progress.total() > 0) {
            var barWidth = 150;
            var barHeight = 10;
            var barX = switch (position) {
                case TOP_LEFT, BOTTOM_LEFT -> 10;
                case TOP_CENTER, BOTTOM_CENTER -> (screenWidth - barWidth) / 2;
                case TOP_RIGHT, BOTTOM_RIGHT -> screenWidth - 10 - barWidth;
            };
            var barY = y + 12;
            graphics.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF555555);
            graphics.fill(barX, barY, barX + (int)(barWidth * ((float)progress.processed() / progress.total())),
                barY + barHeight, 0xFF00FF00);
        }
    }
}
