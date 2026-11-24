// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.gui.button;

import com.elfmcys.ysm.client.gui.GuiDraw;
import com.elfmcys.ysm.YesSteveModel;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class FlatIconButton extends FlatColorButton {
    private final static Identifier ICON = Identifier.fromNamespaceAndPath(YesSteveModel.MOD_ID, "texture/icon.png");
    private final int textureX;
    private final int textureY;

    public FlatIconButton(int x, int y, int width, int height, int textureX, int textureY, OnPress onPress) {
        super(x, y, width, height, Component.empty(), onPress);
        this.textureX = textureX;
        this.textureY = textureY;
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float pPartialTick) {
        super.extractContents(graphics, mouseX, mouseY, pPartialTick);
        int startX = (this.width - 16) / 2;
        int startY = (this.height - 16) / 2;
        GuiDraw.blit(graphics, ICON, this.getX() + startX, this.getY() + startY, 16, 16, textureX, textureY, 16, 16, 256, 256);
    }
}
