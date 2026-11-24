// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.input;

import com.elfmcys.ysm.platform.input.YsmKeyMapping;
import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.client.gui.ConfigScreen;
import com.elfmcys.ysm.client.gui.DisclaimerScreen;
import com.elfmcys.ysm.client.gui.PlayerModelScreen;
import com.elfmcys.ysm.config.ClientConfig;
import com.elfmcys.ysm.config.ServerConfig;
import com.elfmcys.ysm.network.NetworkHandler;
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
public class PlayerModelScreenKey {
    public static final YsmKeyMapping PLAYER_MODEL_KEY =
        new YsmKeyMapping("key.yes_steve_model.player_model.desc", KeyConflictContext.IN_GAME, KeyModifier.ALT,
            InputConstants.Type.KEYSYM, InputConstants.KEY_Y, "key.category.yes_steve_model");

    @SubscribeEvent
    public static void onKeyboardInput(InputEvent.Key event) {
        if (!InputCheckUtil.isInGame()) {
            return;
        }
        if (event.getAction() == InputConstants.PRESS && InputCheckUtil.keyIsMatch(event, PLAYER_MODEL_KEY)) {
            if (!YesSteveModel.isAvailable()) {
                YesSteveModel.sendUnavailableMessage();
                return;
            }
            if (NetworkHandler.isRemoteChannelPresent()
                && !com.elfmcys.ysm.model.service.ClientModelService.instance().preferences().canSwitchModel()) {
                com.elfmcys.ysm.platform.McCompat.setScreen(new ConfigScreen(null));
                return;
            }
            if (ClientConfig.DISCLAIMER_SHOW.get()) {
                com.elfmcys.ysm.platform.McCompat.setScreen(new DisclaimerScreen());
            } else {
                com.elfmcys.ysm.platform.McCompat.setScreen(new PlayerModelScreen());
            }
        }
    }
}
