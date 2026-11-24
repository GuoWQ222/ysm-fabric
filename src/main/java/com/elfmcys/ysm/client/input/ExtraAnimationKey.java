// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.input;

import com.elfmcys.ysm.platform.capability.ICapabilityHolder;
import com.elfmcys.ysm.platform.input.YsmKeyMapping;
import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.capability.PlayerAnimatableCapabilityProvider;
import com.elfmcys.ysm.client.event.PlayerMoveEvent;
import com.elfmcys.ysm.client.gui.AnimationRouletteScreen;
import com.elfmcys.ysm.network.forge.ClientProtocolGateway;
import com.elfmcys.ysm.util.InputCheckUtil;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import com.elfmcys.ysm.platform.Dist;
import com.elfmcys.ysm.platform.event.forge.InputEvent;
import com.elfmcys.ysm.platform.event.forge.RegisterKeyMappingsEvent;
import com.elfmcys.ysm.platform.input.KeyConflictContext;
import com.elfmcys.ysm.platform.input.KeyModifier;
import com.elfmcys.ysm.platform.event.SubscribeEvent;
import com.elfmcys.ysm.platform.event.Mod;

import java.util.List;

import static com.elfmcys.ysm.client.gui.AnimationRouletteScreen.addRootClassify;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class ExtraAnimationKey {
    public static final List<YsmKeyMapping> EXTRA_ANIMATION_KEYS = Lists.newArrayList();

    @SubscribeEvent
    public static void registerKeyBinding(RegisterKeyMappingsEvent event) {
        if (!YesSteveModel.isAvailable()) {
            return;
        }
        for (int i = 0; i <= 7; i++) {
            String name = String.format("key.yes_steve_model.extra_animation.%d.desc", i);
            YsmKeyMapping keyMapping = new YsmKeyMapping(name, KeyConflictContext.IN_GAME, KeyModifier.NONE,
                InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), "key.category.yes_steve_model");
            event.register(keyMapping);
            EXTRA_ANIMATION_KEYS.add(keyMapping);
        }
    }

    @SubscribeEvent
    public static void onKeyboardInput(InputEvent.Key event) {
        if (!YesSteveModel.isAvailable()) {
            return;
        }
        if (!InputCheckUtil.isInGame()) {
            return;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        for (var key : EXTRA_ANIMATION_KEYS) {
            if (event.getAction() == InputConstants.PRESS && InputCheckUtil.keyIsMatch(event, key) && player != null
                && !PlayerMoveEvent.isMoveKey(player)) {
                ICapabilityHolder.of(player).getCapability(PlayerAnimatableCapabilityProvider.CAP).ifPresent(cap -> {
                    var model = cap.getModelRenderTarget();
                    int index = EXTRA_ANIMATION_KEYS.indexOf(key);
                    var info = model.info();
                    var animations = info.getExtraAnimations();
                    if (animations.size() > index) {
                        String keyName = animations.get(index).key();
                        if ("#return".equals(keyName)) {
                            // #return 为停止播放轮盘动画
                            ClientProtocolGateway.stopSelfAnimation();
                        } else if (keyName.startsWith("#")
                            && info.getExtraAnimationClassifications().containsKey(keyName.substring(1))) {
                            addRootClassify(keyName.substring(1));
                            AnimationRouletteScreen screen = new AnimationRouletteScreen(
                                info.getExtraAnimationButtons(), info.getExtraAnimationClassifications(), model, cap);
                            com.elfmcys.ysm.platform.McCompat.setScreen(screen);
                        } else {
                            ClientProtocolGateway.playSelfAnimation(keyName);
                        }
                    }
                });
                return;
            }
        }
    }
}
