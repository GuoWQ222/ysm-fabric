// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.gui.button;

import com.elfmcys.ysm.client.gui.GuiDraw;
import com.elfmcys.ysm.model.catalog.client.entry.ClientFailedCatalogEntry;
import com.elfmcys.ysm.util.ModelIdUtil;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Disabled path-only card for a publication entry whose metadata did not activate. */
public final class FailedCatalogModelButton extends Button {
    private final ClientFailedCatalogEntry entry;

    public FailedCatalogModelButton(int x, int y, ClientFailedCatalogEntry entry) {
        super(x, y, 52, 90, Component.literal(ModelIdUtil.getFileNameFromPath(entry.displayPath())), ignored -> {
        }, DEFAULT_NARRATION);
        this.entry = entry;
        active = false;
    }

    @Override
    //#if MC >= 1.21.9
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick) {
    //#else
    //$$ public boolean mouseClicked(double ysmX, double ysmY, int ysmButton) {
    //$$     var event = com.elfmcys.ysm.legacy.input.MouseButtonEvent.of(ysmX, ysmY, ysmButton);
    //$$     boolean doubleClick = false;
    //#endif
        double mouseX = event.x(), mouseY = event.y();
        int button = event.button();
        return mouseX >= getX() && mouseX < getX() + width && mouseY >= getY() && mouseY < getY() + height
            && CatalogCardActions.click(net.minecraft.client.Minecraft.getInstance().hasControlDown(), button, entry.displayPath(), null, false,
                value -> Minecraft.getInstance().keyboardHandler.setClipboard(value), ignored -> {
                });
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fillGradient(getX(), getY(), getX() + width, getY() + height, 0xFF2E2E2E, 0xFF2E2E2E);
        GuiDraw.drawCenteredString(graphics, Minecraft.getInstance().font, Component.literal("!"), getX() + width / 2, getY() + (height - 20) / 2, 0xFFE57373);
        var lines = Minecraft.getInstance().font.split(getMessage(), 45);
        var y = getY() + height - (lines.size() > 1 ? 19 : 15);
        for (var line : lines.stream().limit(2).toList()) {
            GuiDraw.drawCenteredString(graphics, Minecraft.getInstance().font, line, getX() + width / 2, y, 0xFF9E9E9E);
            y += 9;
        }
    }

    public void renderTooltip(GuiGraphicsExtractor graphics, Screen screen, int mouseX, int mouseY) {
        if (mouseX < getX() || mouseX >= getX() + width || mouseY < getY() || mouseY >= getY() + height) {
            return;
        }
        GuiDraw.renderComponentTooltip(graphics, net.minecraft.client.Minecraft.getInstance().font, List.of(Component.literal(entry.displayPath()).withStyle(ChatFormatting.GRAY),
                Component.literal(entry.error()).withStyle(ChatFormatting.RED)), mouseX, mouseY);
    }
}
