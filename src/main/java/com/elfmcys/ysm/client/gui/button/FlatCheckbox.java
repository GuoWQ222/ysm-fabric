// Modified for the unofficial Fabric 26.2 port by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.gui.button;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.client.gui.GuiDraw;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * Roulette form checkbox: a 128x12 strip of roulette.png (u + 128 when on, v + 12 when hovered) plus a label.
 * Fabric port: 26.x has no StateSwitchingButton, so the toggle state lives here.
 */
public class FlatCheckbox extends AbstractWidget implements IConfigFormsButton {
    private static final Identifier BUTTON_TEXTURE =
        Identifier.fromNamespaceAndPath(YesSteveModel.MOD_ID, "texture/roulette.png");
    private final Consumer<Boolean> onClick;
    private final Component name;
    protected boolean isStateTriggered;

    public FlatCheckbox(int xIn, int yIn, int width, Component name, Consumer<Boolean> onClick) {
        super(xIn, yIn, width, 12, name);
        this.name = name;
        this.onClick = onClick;
    }

    public FlatCheckbox(int xIn, int yIn, Component name, Consumer<Boolean> onClick) {
        this(xIn, yIn, 115, name, onClick);
    }

    public boolean isStateTriggered() {
        return isStateTriggered;
    }

    public void setStateTriggered(boolean triggered) {
        this.isStateTriggered = triggered;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        int u = isStateTriggered ? 128 : 0;
        int v = isHoveredOrFocused() ? 12 : 0;
        GuiDraw.blit(graphics, BUTTON_TEXTURE, getX(), getY(), u, v, width, height);
        GuiDraw.drawString(graphics, Minecraft.getInstance().font, name, getX() + 14, getY() + 2, 0xffffffff, false);
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        this.isStateTriggered = !this.isStateTriggered;
        onClick.accept(this.isStateTriggered);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
