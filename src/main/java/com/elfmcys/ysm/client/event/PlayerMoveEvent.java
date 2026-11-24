// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.event;

import com.elfmcys.ysm.platform.capability.ICapabilityHolder;
import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.capability.PlayerAnimatableCapabilityProvider;
import com.elfmcys.ysm.network.NetworkHandler;
import com.elfmcys.ysm.network.forge.ClientProtocolGateway;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import com.elfmcys.ysm.platform.Dist;
import com.elfmcys.ysm.platform.event.forge.InputEvent;
import com.elfmcys.ysm.platform.event.forge.TickEvent;
import com.elfmcys.ysm.platform.event.SubscribeEvent;
import com.elfmcys.ysm.platform.event.Mod;
import com.mojang.blaze3d.platform.InputConstants;

import static com.elfmcys.ysm.client.input.AnimationRouletteKey.LOCK_ROULETTE_KEY;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class PlayerMoveEvent {
    private static boolean LOCK_EXTRA_ANIMATION = false;

    @SubscribeEvent
    public static void onKeyboardInput(InputEvent.Key event) {
        if (!YesSteveModel.isAvailable()) {
            return;
        }
        if (event.getAction() == InputConstants.PRESS && LOCK_ROULETTE_KEY.matches(event.getKey(), event.getScanCode())) {
            LOCK_EXTRA_ANIMATION = !LOCK_EXTRA_ANIMATION;
        }
    }

    /**
     * 改用 TickEvent.ClientTickEvent 监听按键状态，避免与其他模组（如 Touch Controller） 冲突时漏判按键。
     */
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (!YesSteveModel.isAvailable()) {
            return;
        }
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (LOCK_EXTRA_ANIMATION) {
            return;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && isMoveKey(player)) {
            ICapabilityHolder.of(player).getCapability(PlayerAnimatableCapabilityProvider.CAP).ifPresent(cap -> {
                cap.waitForAsyncUpdate();
                if (cap.isPlayingExtraAnimation()) {
                    cap.stopExtraAnimation();
                    if (NetworkHandler.isRemoteChannelPresent()) {
                        ClientProtocolGateway.stopSelfAnimation();
                    }
                }
            });
        }
    }

    public static boolean isMoveKey(LocalPlayer player) {
        // Fabric port: 26.x ClientInput (move vector x = strafe, y = forward; key presses record).
        var input = player.input;
        if (input == null)
            return false;
        var move = input.getMoveVector();
        return hasImpulse(move.x) || hasImpulse(move.y) || input.keyPresses.jump() || input.keyPresses.shift();
    }

    private static boolean hasImpulse(float impulse) {
        return Math.abs(impulse) > 1.0E-5F;
    }

    public static void switchLock() {
        LOCK_EXTRA_ANIMATION = !LOCK_EXTRA_ANIMATION;
    }

    public static boolean isLocked() {
        return LOCK_EXTRA_ANIMATION;
    }
}
