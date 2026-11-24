// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.event;

import com.elfmcys.ysm.YesSteveModel;
import net.minecraft.world.entity.LivingEntity;
import com.elfmcys.ysm.platform.event.forge.LivingEvent;
import com.elfmcys.ysm.platform.event.forge.ShieldBlockEvent;
import com.elfmcys.ysm.platform.event.SubscribeEvent;
import com.elfmcys.ysm.platform.event.Mod;

@Mod.EventBusSubscriber(modid = YesSteveModel.MOD_ID)
public class LivingShieldBlockEvent {
    public static final String COOLDOWN = "ysm$shield_block_cooldown";

    @SubscribeEvent
    public static void onShieldBlockEvent(ShieldBlockEvent event) {
        holder(event.getEntity()).ysm$setShieldBlockCooldown(5);
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        var holder = holder(event.getEntity());
        int cooldown = holder.ysm$getShieldBlockCooldown();
        if (cooldown > 0) {
            holder.ysm$setShieldBlockCooldown(cooldown - 1);
        } else if (cooldown == 0) {
            holder.ysm$setShieldBlockCooldown(-1); // -1 = absent (Forge removed the key)
        }
    }

    public static boolean inShieldBlockCooldown(LivingEntity entity) {
        return holder(entity).ysm$getShieldBlockCooldown() >= 0;
    }

    private static com.elfmcys.ysm.platform.ShieldBlockCooldownHolder holder(LivingEntity entity) {
        return (com.elfmcys.ysm.platform.ShieldBlockCooldownHolder)entity;
    }
}
