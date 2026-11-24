// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.event;

import com.elfmcys.ysm.client.compat.top.TopPlugin;
import com.elfmcys.ysm.platform.event.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import com.elfmcys.ysm.platform.event.Mod;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public final class InterModMsg {
    @SubscribeEvent
    @SuppressWarnings("Convert2MethodRef")
    public static void onEnqueue(final InterModEnqueueEvent event) {
        InterModComms.sendTo("theoneprobe", "getTheOneProbe", () -> new TopPlugin());
    }
}
