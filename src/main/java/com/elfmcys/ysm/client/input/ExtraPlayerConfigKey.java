// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.input;

import com.elfmcys.ysm.platform.input.YsmKeyMapping;
import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.client.gui.ExtraPlayerConfigScreen;
import com.elfmcys.ysm.util.InputCheckUtil;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import com.elfmcys.ysm.platform.Dist;
import com.elfmcys.ysm.platform.event.forge.InputEvent;
import com.elfmcys.ysm.platform.input.KeyConflictContext;
import com.elfmcys.ysm.platform.input.KeyModifier;
import com.elfmcys.ysm.platform.event.SubscribeEvent;
import com.elfmcys.ysm.platform.event.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class ExtraPlayerConfigKey {
    public static final YsmKeyMapping EXTRA_PLAYER_RENDER_KEY =
        new YsmKeyMapping("key.yes_steve_model.open_extra_player_render.desc", KeyConflictContext.IN_GAME, KeyModifier.ALT,
            InputConstants.Type.KEYSYM, InputConstants.KEY_P, "key.category.yes_steve_model");

    @SubscribeEvent
    public static void onKeyboardInput(InputEvent.Key event) {
        if (!YesSteveModel.isAvailable()) {
            return;
        }
        if (!InputCheckUtil.isInGame()) {
            return;
        }
        if (event.getAction() == InputConstants.PRESS && InputCheckUtil.keyIsMatch(event, EXTRA_PLAYER_RENDER_KEY)) {
            com.elfmcys.ysm.platform.McCompat.setScreen(new ExtraPlayerConfigScreen());
        }
    }
}
