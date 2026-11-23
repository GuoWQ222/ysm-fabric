// Modified for the unofficial Fabric 26.2 port by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm;

import com.elfmcys.ysm.api.internal.event.ExtensionEventHandlerLoader;
import com.elfmcys.ysm.config.ClientConfig;
import com.elfmcys.ysm.config.ServerConfig;
import com.elfmcys.ysm.fabric.FabricEventHooks;
import com.elfmcys.ysm.init.ModSounds;
import com.elfmcys.ysm.platform.event.Event;
import com.elfmcys.ysm.platform.event.IEventBus;
import com.elfmcys.ysm.platform.event.MinecraftForge;
import com.elfmcys.ysm.platform.event.Subscribers;
import com.elfmcys.ysm.util.NativeLibUtil;
import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Fabric port: the shared core of the mod. Forge's {@code @Mod} constructor is split into {@link #init()} (common,
 * from the Fabric main entry point) and {@link #initClient()} (from the client entry point).
 */
public final class YesSteveModel {
    public static final String MOD_ID = "ysm";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    /** Forge had a game bus and a mod bus; the port uses one bus for both. */
    private static final IEventBus EVENT_BUS = MinecraftForge.EVENT_BUS;
    private static boolean initialized;

    private YesSteveModel() {}

    public static void init() {
        if (initialized)
            return;
        initialized = true;
        try {
            NativeLibUtil.load();
        } catch (java.io.IOException failure) {
            // As upstream: an I/O failure while installing the native library aborts mod loading.
            throw new java.io.UncheckedIOException(failure);
        }
        // Platform detection must finish before platform-dependent defaults are registered.
        initConfig();
        com.elfmcys.ysm.command.CommandArguments.register();
        if (!NativeLibUtil.isAvailable()) {
            LOGGER.error(getUnavailableMessageString());
            return;
        }
        Subscribers.registerCommon(EVENT_BUS);
        ExtensionEventHandlerLoader.attach(EVENT_BUS);
        FabricEventHooks.registerCommon();
        FabricEventHooks.postCommonSetup();
    }

    public static void initClient() {
        ModSounds.register();
        if (!NativeLibUtil.isAvailable())
            return;
        Subscribers.registerClient(EVENT_BUS);
        com.elfmcys.ysm.fabric.FabricClientEventHooks.register();
    }

    public static void registerEventHandler(Object handler) {
        EVENT_BUS.register(handler);
    }

    private static void initConfig() {
        ConfigRegistry.INSTANCE.register(MOD_ID, ModConfig.Type.CLIENT, ClientConfig.init());
        ConfigRegistry.INSTANCE.register(MOD_ID, ModConfig.Type.SERVER, ServerConfig.init());
    }

    public static boolean postEvent(Event event) {
        return EVENT_BUS.post(event);
    }

    public static boolean isAvailable() {
        return NativeLibUtil.isAvailable();
    }

    public static boolean isMobilePlatform() {
        return NativeLibUtil.isMobilePlatform();
    }

    public static void sendUnavailableMessage() {
        var player = Minecraft.getInstance().player;
        if (player != null) {
            player.sendSystemMessage(getUnavailableMessage());
        }
    }

    public static Component getUnavailableMessage() {
        return NativeLibUtil.getUnsupportedMsg();
    }

    public static String getUnavailableMessageString() {
        return NativeLibUtil.getUnsupportedMsgStr();
    }
}
