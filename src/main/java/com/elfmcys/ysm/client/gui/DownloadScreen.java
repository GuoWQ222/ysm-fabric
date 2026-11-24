// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.gui;

import com.elfmcys.ysm.client.gui.button.FlatColorButton;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public class DownloadScreen extends ModelScreen {
    private final PlayerModelScreen parent;
    private int x;
    private int y;

    public DownloadScreen(PlayerModelScreen parent) {
        super(Component.literal("YSM Config GUI"), parent);
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.x = (width - 420) / 2;
        this.y = (height - 235) / 2;

        addRenderableWidget(new FlatColorButton(x + 5, y, 80, 18,
            Component.translatable("gui.yes_steve_model.model.return"), (b) -> showScreen(parent)));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int pMouseX, int pMouseY, float pPartialTick) {
        GuiDraw.drawCenteredString(graphics, font, "Coming Soooooooooooooooooooooooooon™", width / 2, height / 2 - 5, com.elfmcys.ysm.platform.McCompat.rgb(net.minecraft.ChatFormatting.DARK_RED));
        super.extractRenderState(graphics, pMouseX, pMouseY, pPartialTick);
    }
}
