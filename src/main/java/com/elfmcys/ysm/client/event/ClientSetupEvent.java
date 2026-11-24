// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.event;

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

    /**
     * YSM's HUD layers in drawing order, drawn after the vanilla HUD (Forge: above the debug text); each loader's
     * client glue registers them with its HUD API. Empty when YSM is unavailable.
     */
    public static java.util.LinkedHashMap<Identifier, com.elfmcys.ysm.client.gui.overlay.HudLayer> hudLayers() {
        var layers = new java.util.LinkedHashMap<Identifier, com.elfmcys.ysm.client.gui.overlay.HudLayer>();
        if (!YesSteveModel.isAvailable())
            return layers;
        layers.put(Identifier.fromNamespaceAndPath(YesSteveModel.MOD_ID, "debug_info"), DebugAnimationScreen.getGuiOverlay());
        layers.put(Identifier.fromNamespaceAndPath(YesSteveModel.MOD_ID, "extra_player"), new ExtraPlayerScreen());
        layers.put(Identifier.fromNamespaceAndPath(YesSteveModel.MOD_ID, "loading_state"), new LoadingStateScreen());
        return layers;
    }

}
