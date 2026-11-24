// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.gui.overlay;

import com.elfmcys.ysm.client.gui.ExtraPlayerConfigScreen;
import com.elfmcys.ysm.config.ExtraPlayerScreenConfig;
import com.elfmcys.ysm.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;

public class ExtraPlayerScreen implements HudLayer {
    /** Diagnostics (test harness): HUD extraction calls. */
    public static volatile int calls;

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, net.minecraft.client.DeltaTracker deltaTracker) {
        calls++;
        if (ExtraPlayerScreenConfig.DISABLE_PLAYER_RENDER.get()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            return;
        }
        if (com.elfmcys.ysm.platform.McCompat.screen() instanceof ExtraPlayerConfigScreen) {
            return;
        }

        double posX = ExtraPlayerScreenConfig.PLAYER_POS_X.get();
        double posY = ExtraPlayerScreenConfig.PLAYER_POS_Y.get();
        float scale = ExtraPlayerScreenConfig.PLAYER_SCALE.get().floatValue();
        float yawOffset = ExtraPlayerScreenConfig.PLAYER_YAW_OFFSET.get().floatValue();

        RenderUtil.renderExtraPlayerEntity(graphics, player, posX, posY, scale, yawOffset, -500, mc.getDeltaTracker().getGameTimeDeltaPartialTick(false));
    }
}
