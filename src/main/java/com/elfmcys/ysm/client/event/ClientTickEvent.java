// Modified for the unofficial Fabric 26.2 port by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.event;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.capability.PlayerAnimatableCapabilityProvider;
import com.elfmcys.ysm.model.service.ClientModelService;
import com.elfmcys.ysm.client.texture.CustomTextureManager;
import com.elfmcys.ysm.config.ClientConfig;
import com.elfmcys.ysm.network.forge.ClientProtocolGateway;
import net.minecraft.client.Minecraft;
import com.elfmcys.ysm.platform.Dist;
import com.elfmcys.ysm.platform.event.forge.TickEvent;
import com.elfmcys.ysm.platform.event.SubscribeEvent;
import com.elfmcys.ysm.platform.event.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class ClientTickEvent {
    private static int tickCount;
    private static int refreshRate = 60;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (!YesSteveModel.isAvailable()) {
            RegisterEntityRenderersEvent.releaseFirstPersonArm();
            return;
        }
        if (event.phase == TickEvent.Phase.END) {
            return;
        }
        tickCount++;
        CustomTextureManager.tick();
        ClientModelService.current().ifPresent(ClientModelService::tick);
        refreshRate = Minecraft.getInstance().getWindow().getRefreshRate();

        var player = Minecraft.getInstance().player;
        if (player == null || ClientConfig.DISABLE_SELF_MODEL.get() || ClientConfig.DISABLE_SELF_HANDS.get()) {
            RegisterEntityRenderersEvent.releaseFirstPersonArm();
        }
        if (player != null) {
            var capability = player.getCapability(PlayerAnimatableCapabilityProvider.CAP).orElse(null);
            if (capability == null || !capability.isPresentationAllowed()) {
                RegisterEntityRenderersEvent.releaseFirstPersonArm();
            }
            if (capability != null) {
                ClientProtocolGateway.tick(player, capability);
            }
        }
    }

    public static int getTickCount() {
        return tickCount;
    }

    public static int getRefreshRate() {
        return refreshRate;
    }
}
