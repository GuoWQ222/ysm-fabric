// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.event;

import com.elfmcys.ysm.platform.capability.ICapabilityHolder;
import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.capability.ModelInfoCapabilityProvider;
import net.minecraft.server.level.ServerPlayer;
import com.elfmcys.ysm.platform.event.forge.MobEffectEvent;
import com.elfmcys.ysm.platform.event.SubscribeEvent;
import com.elfmcys.ysm.platform.event.Mod;

@Mod.EventBusSubscriber
@SuppressWarnings("resource")
public class MobEffectSyncEvent {
    @SubscribeEvent
    public static void onAdded(MobEffectEvent.Added event) {
        if (!YesSteveModel.isAvailable() || event.getEntity().level().isClientSide()) {
            return;
        }
        if (event.getEntity() instanceof ServerPlayer player && event.getEffectInstance().getEffect() != null) {
            var effectInstance = event.getEffectInstance();
            ICapabilityHolder.of(player).getCapability(ModelInfoCapabilityProvider.MODEL_INFO_CAP).ifPresent(cap -> {
                cap.getPropertiesTracker().addEffect(player, effectInstance.getEffect().value(),
                    effectInstance.getAmplifier() + 1);
            });
        }
    }

    @SubscribeEvent
    public static void onRemoved(MobEffectEvent.Remove event) {
        if (!YesSteveModel.isAvailable() || event.getEntity().level().isClientSide()) {
            return;
        }
        if (event.getEntity() instanceof ServerPlayer player && event.getEffect() != null) {
            ICapabilityHolder.of(player).getCapability(ModelInfoCapabilityProvider.MODEL_INFO_CAP).ifPresent(cap -> {
                cap.getPropertiesTracker().removeEffect(player, event.getEffect().value());
            });
        }
    }

    @SubscribeEvent
    public static void onExpired(MobEffectEvent.Expired event) {
        if (!YesSteveModel.isAvailable() || event.getEntity().level().isClientSide()) {
            return;
        }
        if (event.getEntity() instanceof ServerPlayer player && event.getEffectInstance() != null
            && event.getEffectInstance().getEffect() != null) {
            ICapabilityHolder.of(player).getCapability(ModelInfoCapabilityProvider.MODEL_INFO_CAP).ifPresent(cap -> {
                cap.getPropertiesTracker().removeEffect(player, event.getEffectInstance().getEffect().value());
            });
        }
    }
}
