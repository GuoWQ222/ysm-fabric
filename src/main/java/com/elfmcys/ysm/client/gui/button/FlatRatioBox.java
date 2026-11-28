// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.gui.button;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import com.elfmcys.ysm.platform.Dist;
import com.elfmcys.ysm.platform.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FlatRatioBox extends AbstractWidget implements IConfigFormsButton {
    private final int maxHeight;

    public FlatRatioBox(int pX, int pY, int maxHeight, Component component) {
        super(pX, pY, 115, 15, component);
        this.maxHeight = maxHeight;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int pMouseX, int pMouseY, float pPartialTick) {
        graphics.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + maxHeight, 0xef_434242);
        //#if MC >= 1.21.11
        this.extractScrollingStringOverContents(graphics.textRendererForWidget(this, net.minecraft.client.gui.GuiGraphicsExtractor.HoveredTextEffects.NONE), this.getMessage().copy().withColor(0xFFFFFF), 2);
        //#else
        //$$ this.renderScrollingString(graphics, net.minecraft.client.Minecraft.getInstance().font, 2, 0xFFFFFFFF);
        //#endif
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        this.defaultButtonNarrationText(output);
    }
}
