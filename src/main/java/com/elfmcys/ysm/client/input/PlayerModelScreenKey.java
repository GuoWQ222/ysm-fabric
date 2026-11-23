// Modified for the unofficial Fabric 26.2 port by GuoWQ222, 2026 (see PORTING.md).
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
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class PlayerModelScreenKey {
    public static final YsmKeyMapping PLAYER_MODEL_KEY =
        new YsmKeyMapping("key.yes_steve_model.player_model.desc", KeyConflictContext.IN_GAME, KeyModifier.ALT,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Y, "key.category.yes_steve_model");

    @SubscribeEvent
    public static void onKeyboardInput(InputEvent.Key event) {
        if (!InputCheckUtil.isInGame()) {
            return;
        }
        if (event.getAction() == GLFW.GLFW_PRESS && InputCheckUtil.keyIsMatch(event, PLAYER_MODEL_KEY)) {
            if (!YesSteveModel.isAvailable()) {
                YesSteveModel.sendUnavailableMessage();
                return;
            }
            if (NetworkHandler.isRemoteChannelPresent()
                && !com.elfmcys.ysm.model.service.ClientModelService.instance().preferences().canSwitchModel()) {
                Minecraft.getInstance().gui.setScreen(new ConfigScreen(null));
                return;
            }
            if (ClientConfig.DISCLAIMER_SHOW.get()) {
                Minecraft.getInstance().gui.setScreen(new DisclaimerScreen());
            } else {
                Minecraft.getInstance().gui.setScreen(new PlayerModelScreen());
            }
        }
    }
}
