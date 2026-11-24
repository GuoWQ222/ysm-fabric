// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.gui;

import com.elfmcys.ysm.config.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public class DisclaimerScreen extends Screen {
    private Checkbox readCheckbox;
    private int x;
    private int y;

    public DisclaimerScreen() {
        super(Component.literal("Disclaimer GUI"));
    }

    @Override
    protected void init() {
        this.clearWidgets();

        MutableComponent mainText = Component.translatable("gui.yes_steve_model.disclaimer.text");
        List<FormattedCharSequence> splitMainText = font.split(mainText, 400);
        int totalHeight = splitMainText.size() * font.lineHeight + 20 + 20 + 10 + 20;
        this.x = (width - 400) / 2;
        this.y = (height - totalHeight) / 2;

        MutableComponent readCheckboxText = Component.translatable("gui.yes_steve_model.disclaimer.read");
        int readTextWidth = font.width(readCheckboxText);
        readCheckbox = Checkbox.builder(readCheckboxText, font).pos((width - readTextWidth) / 2, y + totalHeight - 50)
            .selected(!ClientConfig.DISCLAIMER_SHOW.get()).build();
        addRenderableWidget(readCheckbox);
        addRenderableWidget(new Button.Builder(Component.translatable("gui.yes_steve_model.disclaimer.close"), b -> {
            if (readCheckbox.selected()) {
                ClientConfig.DISCLAIMER_SHOW.set(false);
                ClientConfig.DISCLAIMER_SHOW.save();
                com.elfmcys.ysm.platform.McCompat.setScreen(new PlayerModelScreen());
            } else {
                com.elfmcys.ysm.platform.McCompat.setScreen(null);
            }
        }).size(300, 20).pos((width - 300) / 2, y + totalHeight - 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int pMouseX, int pMouseY, float pPartialTick) {
        GuiDraw.drawWordWrap(graphics, font, Component.translatable("gui.yes_steve_model.disclaimer.text"), x, y, 400, 0xffffffff);
        super.extractRenderState(graphics, pMouseX, pMouseY, pPartialTick);
    }
}
