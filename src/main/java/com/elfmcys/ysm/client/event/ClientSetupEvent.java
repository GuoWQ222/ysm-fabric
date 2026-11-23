// Modified for the unofficial Fabric 26.2 port by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.event;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.resources.Identifier;
import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.client.ClientStartup;
import com.elfmcys.ysm.model.ModelRuntime;
import com.elfmcys.ysm.client.gui.overlay.DebugAnimationScreen;
import com.elfmcys.ysm.client.gui.overlay.ExtraPlayerScreen;
import com.elfmcys.ysm.client.gui.overlay.LoadingStateScreen;
import com.elfmcys.ysm.client.input.AnimationRouletteKey;
import com.elfmcys.ysm.client.input.DebugAnimationKey;
import com.elfmcys.ysm.client.input.ExtraAnimationKey;
import com.elfmcys.ysm.client.input.ExtraPlayerConfigKey;
import com.elfmcys.ysm.client.input.PlayerModelScreenKey;
import com.elfmcys.ysm.platform.Dist;
import com.elfmcys.ysm.platform.event.forge.RegisterKeyMappingsEvent;
import com.elfmcys.ysm.platform.event.SubscribeEvent;
import com.elfmcys.ysm.platform.event.Mod;
import com.elfmcys.ysm.platform.event.forge.FMLClientSetupEvent;


@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetupEvent {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        if (!YesSteveModel.isAvailable()) {
            return;
        }

        event.enqueueWork(() -> ClientStartup.start(ModelRuntime.system()));
    }

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(PlayerModelScreenKey.PLAYER_MODEL_KEY);

        if (!YesSteveModel.isAvailable()) {
            return;
        }

        event.register(AnimationRouletteKey.ANIMATION_ROULETTE_KEY);
        event.register(AnimationRouletteKey.LOCK_ROULETTE_KEY);
        event.register(DebugAnimationKey.DEBUG_ANIMATION_KEY);
        event.register(ExtraPlayerConfigKey.EXTRA_PLAYER_RENDER_KEY);
        ExtraAnimationKey.registerKeyBinding(event);
    }

    /** Fabric port: HUD overlays are Fabric HUD elements drawn after the vanilla HUD (Forge: above the debug text). */
    public static void registerHudElements() {
        if (!YesSteveModel.isAvailable())
            return;
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(YesSteveModel.MOD_ID, "debug_info"),
            DebugAnimationScreen.getGuiOverlay());
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(YesSteveModel.MOD_ID, "extra_player"),
            new ExtraPlayerScreen());
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(YesSteveModel.MOD_ID, "loading_state"),
            new LoadingStateScreen());
    }

}
