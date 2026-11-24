// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.compat.bettercombat.event;

import com.elfmcys.ysm.network.forge.ClientProtocolGateway;
import net.bettercombat.api.AttackHand;
import net.bettercombat.api.client.BetterCombatClientEvents;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;

public class PlayerAttackEvent implements BetterCombatClientEvents.PlayerAttackStart {
    @Override
    public void onPlayerAttackStart(LocalPlayer player, AttackHand hand) {
        var arm = hand.isOffHand() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        com.elfmcys.ysm.platform.McCompat.startLocalSwing(player, arm);
        ClientProtocolGateway.swingHand(arm);
    }
}
