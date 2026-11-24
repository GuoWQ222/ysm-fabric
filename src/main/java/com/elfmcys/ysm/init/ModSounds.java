// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.init;

import com.elfmcys.ysm.YesSteveModel;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

/** Fabric port: YSM's model sound event, registered directly in the sound event registry. */
public final class ModSounds {
    public static final SoundEvent CUSTOM =
        SoundEvent.createFixedRangeEvent(Identifier.fromNamespaceAndPath(YesSteveModel.MOD_ID, "custom"), 16.0F);
    private static boolean registered;

    private ModSounds() {}

    public static void register() {
        if (registered)
            return;
        registered = true;
        //#if LOADER == fabric
        Registry.register(BuiltInRegistries.SOUND_EVENT, CUSTOM.location(), CUSTOM);
        //#elseif LOADER == neoforge
        //$$ // NeoForge freezes the vanilla registries; entries go in through its RegisterEvent
        //$$ com.elfmcys.ysm.neoforge.YsmNeoForge.modBus().addListener((net.neoforged.neoforge.registries.RegisterEvent event) ->
        //$$     event.register(net.minecraft.core.registries.Registries.SOUND_EVENT, CUSTOM.location(), () -> CUSTOM));
        //#else
        //$$ com.elfmcys.ysm.forge.YsmForge.registerSound(CUSTOM);
        //#endif
    }
}
