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
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.IConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegisterEvent;

/** Forge entry point (the counterpart of the Fabric port's YsmFabric / YsmFabricClient and of YsmNeoForge). */
@Mod(YesSteveModel.MOD_ID)
public final class YsmForge {
    private static FMLJavaModLoadingContext context;

    public YsmForge(FMLJavaModLoadingContext context) {
        YsmForge.context = context;
        YesSteveModel.init();
        if (com.elfmcys.ysm.platform.Platform.isClient())
            YesSteveModel.initClient();
    }

    public static BusGroup modBusGroup() {
        return context.getModBusGroup();
    }

    /** Forge's own config system (Fabric and NeoForge use Forge Config API Port for the same specs). */
    public static void registerConfigs(IConfigSpec<?> client, IConfigSpec<?> server) {
        context.registerConfig(ModConfig.Type.CLIENT, client);
        context.registerConfig(ModConfig.Type.SERVER, server);
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
