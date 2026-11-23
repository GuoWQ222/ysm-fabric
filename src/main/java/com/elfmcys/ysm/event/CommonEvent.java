// Modified for the unofficial Fabric 26.2 port by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.event;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.capability.AuthModelsCapability;
import com.elfmcys.ysm.capability.ModelInfoCapability;
import com.elfmcys.ysm.capability.PlayerAnimatableCapability;
import com.elfmcys.ysm.capability.ProjectileAnimatableCapability;
import com.elfmcys.ysm.capability.ProjectileModelInfoCapability;
import com.elfmcys.ysm.capability.VehicleAnimatableCapability;
import com.elfmcys.ysm.capability.VehicleModelInfoCapability;
import com.elfmcys.ysm.model.ModelRuntime;
import com.elfmcys.ysm.network.NetworkHandler;
import com.elfmcys.ysm.platform.Dist;
import com.elfmcys.ysm.platform.event.SubscribeEvent;
import com.elfmcys.ysm.platform.event.Mod;
import com.elfmcys.ysm.platform.event.forge.FMLCommonSetupEvent;
import com.elfmcys.ysm.platform.FMLEnvironment;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public final class CommonEvent {
    @SubscribeEvent
    public static void onSetupEvent(FMLCommonSetupEvent event) {
        if (!YesSteveModel.isAvailable()) {
            YesSteveModel.LOGGER.error("{}", YesSteveModel.getUnavailableMessageString());
            return;
        }
        event.enqueueWork(() -> {
            ModelRuntime.initialize();
            NetworkHandler.init();
            if (FMLEnvironment.dist == Dist.DEDICATED_SERVER) {
                ModelRuntime.system().catalog().activate();
            }
        });
    }

    // Fabric port: capabilities need no registration (platform.capability.CapabilityManager is token based).

}
