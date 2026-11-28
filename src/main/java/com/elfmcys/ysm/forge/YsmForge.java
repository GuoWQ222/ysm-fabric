/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.forge;

import com.elfmcys.ysm.YesSteveModel;
import com.mojang.brigadier.arguments.ArgumentType;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
//#if MC >= 1.21.6
import net.minecraftforge.eventbus.api.bus.BusGroup;
//#endif
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.IConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegisterEvent;

/** Forge entry point (the counterpart of the Fabric port's YsmFabric / YsmFabricClient and of YsmNeoForge). */
@Mod(YesSteveModel.MOD_ID)
public final class YsmForge {
    private static FMLJavaModLoadingContext context;

    //#if MC >= 1.21.1
    public YsmForge(FMLJavaModLoadingContext context) {
        YsmForge.context = context;
    //#else
    //$$ // Forge for 1.21 constructs mods without arguments
    //$$ public YsmForge() {
    //$$     YsmForge.context = FMLJavaModLoadingContext.get();
    //#endif
        YesSteveModel.init();
        if (com.elfmcys.ysm.platform.Platform.isClient())
            YesSteveModel.initClient();
    }

    //#if MC >= 1.21.6
    public static BusGroup modBusGroup() {
        return context.getModBusGroup();
    }
    //#else
    //$$ /** Forge before 1.21.6 (EventBus 6): the mod event bus (versions/forge-eventbus6.remap routes listeners here). */
    //$$ public static net.minecraftforge.eventbus.api.IEventBus modEventBus() {
    //$$     return context.getModEventBus();
    //$$ }
    //#endif

    /** Forge's own config system (Fabric and NeoForge use Forge Config API Port for the same specs). */
    public static void registerConfigs(IConfigSpec<?> client, IConfigSpec<?> server) {
        //#if MC >= 1.21.1
        context.registerConfig(ModConfig.Type.CLIENT, client);
        context.registerConfig(ModConfig.Type.SERVER, server);
        //#else
        //$$ net.minecraftforge.fml.ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, client);
        //$$ net.minecraftforge.fml.ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, server);
        //#endif
    }

    /** Forge freezes the vanilla registries; entries go in through its RegisterEvent. */
    public static void registerSound(SoundEvent sound) {
        RegisterEvent.getBus(modBusGroup()).addListener(event -> {
            event.register(Registries.SOUND_EVENT, sound.location(), () -> sound);
        });
    }

    public static <A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>> void registerArgument(String name,
        Class<A> type, ArgumentTypeInfo<A, T> info) {
        var id = Identifier.fromNamespaceAndPath(YesSteveModel.MOD_ID, name);
        RegisterEvent.getBus(modBusGroup()).addListener(event -> {
            event.register(Registries.COMMAND_ARGUMENT_TYPE, id, () -> ArgumentTypeInfos.registerByClass(type, info));
        });
    }
}
