// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.input;

import com.elfmcys.ysm.platform.capability.ICapabilityHolder;
import com.elfmcys.ysm.platform.input.YsmKeyMapping;
import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.capability.PlayerAnimatableCapabilityProvider;
import com.elfmcys.ysm.client.gui.AnimationRouletteScreen;
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
public class AnimationRouletteKey {
    public static final YsmKeyMapping ANIMATION_ROULETTE_KEY =
        new YsmKeyMapping("key.yes_steve_model.animation_roulette.desc", KeyConflictContext.IN_GAME, KeyModifier.NONE,
            InputConstants.Type.KEYSYM, InputConstants.KEY_Z, "key.category.yes_steve_model");

    public static final YsmKeyMapping LOCK_ROULETTE_KEY =
        new YsmKeyMapping("key.yes_steve_model.lock_roulette.desc", KeyConflictContext.IN_GAME, KeyModifier.ALT,
            InputConstants.Type.KEYSYM, InputConstants.KEY_L, "key.category.yes_steve_model");

    @SubscribeEvent
    public static void onKeyboardInput(InputEvent.Key event) {
        if (!YesSteveModel.isAvailable()) {
            return;
        }
        if (!InputCheckUtil.isInGame()) {
            return;
        }
        if (event.getAction() == InputConstants.PRESS && InputCheckUtil.keyIsMatch(event, ANIMATION_ROULETTE_KEY)
            && (!NetworkHandler.isRemoteChannelPresent()
                || com.elfmcys.ysm.model.service.ClientModelService.instance().preferences().canSwitchModel())) {
            if (Minecraft.getInstance().player != null) {
                ICapabilityHolder.of(Minecraft.getInstance().player).getCapability(PlayerAnimatableCapabilityProvider.CAP).ifPresent(cap -> {
                    String modelId = cap.getModelId();
                    var model = cap.getModelRenderTarget();
                    if (model != null && !model.info().getExtraAnimations().isEmpty()) {
                        if (com.elfmcys.ysm.platform.McCompat.screen() == null) {
                            com.elfmcys.ysm.platform.McCompat.setScreen(new AnimationRouletteScreen(modelId, model, cap));
                            return;
                        }
                        if (com.elfmcys.ysm.platform.McCompat.screen() instanceof AnimationRouletteScreen) {
                            com.elfmcys.ysm.platform.McCompat.setScreen(null);
                        }
                    }
                });
            }
        }
    }
}
