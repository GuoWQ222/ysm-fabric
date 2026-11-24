// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.gui.button;

import com.elfmcys.ysm.platform.capability.ICapabilityHolder;
import com.elfmcys.ysm.client.gui.GuiDraw;
import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.capability.PlayerAnimatableCapabilityProvider;
import com.elfmcys.ysm.network.forge.ClientProtocolGateway;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class StarButton extends FlatColorButton {
    private final static Identifier ICON = Identifier.fromNamespaceAndPath(YesSteveModel.MOD_ID, "texture/icon.png");

    public StarButton(int x, int y) {
        super(x, y, 20, 20, Component.empty(), (b) -> {
        });
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float pPartialTick) {
        super.extractContents(graphics, mouseX, mouseY, pPartialTick);
        int startX = (this.width - 16) / 2;
        int startY = (this.height - 16) / 2;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            ICapabilityHolder.of(player).getCapability(PlayerAnimatableCapabilityProvider.CAP).ifPresent(modelInfoCap -> {
                var preferences = com.elfmcys.ysm.model.service.ClientModelService.instance().preferences();
                var modelHash = modelInfoCap.getModelHash();
                if (modelHash != null && preferences.favourite(modelHash)) {
                    GuiDraw.blit(graphics, ICON, this.getX() + startX, this.getY() + startY, 16, 16, 16, 0, 16, 16, 256, 256);
                } else {
                    GuiDraw.blit(graphics, ICON, this.getX() + startX, this.getY() + startY, 16, 16, 0, 0, 16, 16, 256, 256);
                }
            });
        }
    }

    @Override
    public void onPress(net.minecraft.client.input.InputWithModifiers input) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            ICapabilityHolder.of(player).getCapability(PlayerAnimatableCapabilityProvider.CAP).ifPresent(modelInfoCap -> {
                var preferences = com.elfmcys.ysm.model.service.ClientModelService.instance().preferences();
                var modelHash = modelInfoCap.getModelHash();
                if (modelHash == null) {
                    return;
                }
                if (preferences.favourite(modelHash)) {
                    ClientProtocolGateway.updateStar(modelHash, false);
                } else {
                    ClientProtocolGateway.updateStar(modelHash, true);
                }
            });
        }
    }
}
