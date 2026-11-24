// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.gui;

import com.elfmcys.ysm.platform.capability.ICapabilityHolder;
import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.capability.PlayerAnimatableCapabilityProvider;
import com.elfmcys.ysm.config.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 为了兼容 Android 平台运行 Java 版 MC
 * <p>
 * 设计此类用于一些 Android 特有的兼容性处理
 */
public class AndroidCompat {
    public static boolean isAndroid() {
        return YesSteveModel.isMobilePlatform();
    }

    @Nullable
    public static List<Button> addYsmSkinButton(PauseScreen screen) {
        if (AndroidCompat.isAndroid()) {
            Minecraft mc = Minecraft.getInstance();

            Component skinName = Component.translatable("gui.yes_steve_model.skin");
            Button skinButton = Button.builder(skinName, button -> {
                if (ClientConfig.DISCLAIMER_SHOW.get()) {
                    com.elfmcys.ysm.platform.McCompat.setScreen(new DisclaimerScreen());
                } else {
                    com.elfmcys.ysm.platform.McCompat.setScreen(new PlayerModelScreen());
                }
            }).bounds(screen.width / 2 - 69, screen.height - 35, 138, 30).build();
            skinButton.setTooltip(Tooltip.create(Component.translatable("key.yes_steve_model.player_model.desc")));

            Component configIcon = Component.literal("\uD83D\uDD27");
            Button extraPlayerButton = Button.builder(configIcon, button -> com.elfmcys.ysm.platform.McCompat.setScreen(new ExtraPlayerConfigScreen()))
                .bounds(screen.width / 2 - 120, screen.height - 35, 50, 30).build();
            extraPlayerButton.setTooltip(
                Tooltip.create(Component.translatable("key.yes_steve_model.open_extra_player_render.desc")));

            Component icon = Component.literal("\uD83D\uDE04");
            Button rouletteButton = Button.builder(icon, button -> {
                if (mc.player != null) {
                    ICapabilityHolder.of(mc.player).getCapability(PlayerAnimatableCapabilityProvider.CAP).ifPresent(cap -> {
                        String modelId = cap.getModelId();
                        var model = cap.getModelRenderTarget();
                        if (model != null && !model.info().getExtraAnimations().isEmpty()) {
                            com.elfmcys.ysm.platform.McCompat.setScreen(new AnimationRouletteScreen(modelId, model, cap));
                        }
                    });
                }
            }).bounds(screen.width / 2 + 69, screen.height - 35, 50, 30).build();
            rouletteButton
                .setTooltip(Tooltip.create(Component.translatable("key.yes_steve_model.animation_roulette.desc")));

            return List.of(skinButton, extraPlayerButton, rouletteButton);
        }
        return null;
    }
}
