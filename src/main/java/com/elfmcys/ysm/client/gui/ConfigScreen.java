// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.gui;

import com.elfmcys.ysm.client.gui.button.ConfigCheckBox;
import com.elfmcys.ysm.client.gui.button.FlatColorButton;
import com.elfmcys.ysm.client.gui.button.PositionButton;
import com.elfmcys.ysm.config.ClientConfig;
import com.elfmcys.ysm.config.ExtraPlayerScreenConfig;
import com.elfmcys.ysm.config.LoadingStateScreenConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class ConfigScreen extends ModelScreen {
    @Nullable
    private final PlayerModelScreen parent;
    @Nullable
    private final net.minecraft.client.gui.screens.Screen returnTo;
    private final boolean fromModMenu;

    public ConfigScreen(@Nullable PlayerModelScreen parent) {
        super(Component.literal("YSM Config GUI"), parent);
        this.parent = parent;
        this.returnTo = parent;
        this.fromModMenu = false;
    }

    /** Fabric port: opened by Mod Menu's "Configure"; Return and Escape go back to Mod Menu's screen. */
    public ConfigScreen(@Nullable net.minecraft.client.gui.screens.Screen modMenuScreen, boolean fromModMenu) {
        super(Component.literal("YSM Config GUI"), (ModelScreen)null);
        this.parent = null;
        this.returnTo = modMenuScreen;
        this.fromModMenu = fromModMenu;
    }

    @Override
    public void onClose() {
        if (fromModMenu) {
            showScreen(returnTo);
            return;
        }
        super.onClose();
    }

    @Override
    protected void init() {
        int x = (width - 420) / 2;
        int y = (height - 265) / 2;

        addRenderableWidget(new FlatColorButton(x + 5, y + 2, 80, 18,
            Component.translatable("gui.yes_steve_model.model.return"), (b) -> showScreen(returnTo)));

        addRenderableWidget(
            // Fabric port: vanilla slider in place of ForgeSlider (0-100, step 1, "label: N%").
            new net.minecraft.client.gui.components.AbstractSliderButton(x + 5, y + 24, 320, 18, Component.empty(),
                ClientConfig.SOUND_VOLUME.get() / 100.0) {
                {
                    updateMessage();
                }

                private double percent() {
                    return Math.round(this.value * 100.0);
                }

                @Override
                protected void updateMessage() {
                    setMessage(Component.translatable("gui.yes_steve_model.config.sound_volume")
                        .append(String.valueOf((int)percent())).append("%"));
                }

                @Override
                protected void applyValue() {
                    ClientConfig.SOUND_VOLUME.set(percent());
                    ClientConfig.SOUND_VOLUME.save();
                }
            });

        addRenderableWidget(ConfigCheckBox.create(x + 5, y + 45, "disable_self_model", ClientConfig.DISABLE_SELF_MODEL));
        addRenderableWidget(ConfigCheckBox.create(x + 5, y + 67, "disable_other_model", ClientConfig.DISABLE_OTHER_MODEL));
        addRenderableWidget(ConfigCheckBox.create(x + 5, y + 89, "print_animation_roulette_msg",
            ClientConfig.PRINT_ANIMATION_ROULETTE_MSG));
        addRenderableWidget(ConfigCheckBox.create(x + 5, y + 111, "disable_self_hands", ClientConfig.DISABLE_SELF_HANDS));
        addRenderableWidget(
            ConfigCheckBox.create(x + 5, y + 133, "disable_player_render", ExtraPlayerScreenConfig.DISABLE_PLAYER_RENDER));
        addRenderableWidget(
            ConfigCheckBox.create(x + 5, y + 155, "disable_projectile_model", ClientConfig.DISABLE_PROJECTILE_MODEL));
        addRenderableWidget(
            ConfigCheckBox.create(x + 5, y + 177, "disable_vehicle_model", ClientConfig.DISABLE_VEHICLE_MODEL));
        addRenderableWidget(ConfigCheckBox.create(x + 5, y + 199, "disable_external_first_person_anim",
            ClientConfig.DISABLE_EXTERNAL_FIRST_PERSON_ANIM));
        addRenderableWidget(ConfigCheckBox.create(x + 5, y + 221, "disable_loading_state_screen",
            LoadingStateScreenConfig.DISABLE_LOADING_STATE_SCREEN));
        addRenderableWidget(
            ConfigCheckBox.create(x + 5, y + 243, "use_compatibility_renderer", ClientConfig.USE_COMPATIBILITY_RENDERER));

        addRenderableWidget(new PositionButton(x + 5, y + 264));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.extractRenderState(graphics, pMouseX, pMouseY, pPartialTick);
    }
}
