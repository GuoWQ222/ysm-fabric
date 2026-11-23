// Modified for the unofficial Fabric 26.2 port by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.compat.bettercombat;

import com.elfmcys.ysm.client.compat.bettercombat.event.PlayerAttackEvent;
import com.elfmcys.ysm.util.ReflectionUtil;
import net.bettercombat.api.client.BetterCombatClientEvents;
import net.bettercombat.client.animation.AttackAnimationStack;
import net.minecraft.client.player.AbstractClientPlayer;

import java.lang.invoke.VarHandle;

/**
 * Loaded only with Better Combat installed. Better Combat 3.x keeps the running attack in an
 * {@link AttackAnimationStack} (a Player Animation Library controller) in the {@code attackAnimation} field it adds to
 * {@link AbstractClientPlayer}; the 1.20 version used a KeyframeAnimationPlayer whose extra data held the name.
 */
final class BetterCombatCompatInner {
    private static VarHandle attackAnimationField;

    private BetterCombatCompatInner() {}

    static void innerInit() {
        ReflectionUtil.getField(AbstractClientPlayer.class, "attackAnimation", AttackAnimationStack.class)
            .ifPresent(field -> attackAnimationField = field);
        if (attackAnimationField == null)
            return;
        BetterCombatClientEvents.ATTACK_START.register(new PlayerAttackEvent());
    }

    static String getAttackAnimation(AbstractClientPlayer player) {
        if (attackAnimationField == null)
            return "";
        var stack = (AttackAnimationStack)attackAnimationField.get(player);
        if (stack == null || !stack.isActive())
            return "";
        var animation = stack.getCurrentAnimationInstance();
        if (animation == null)
            return "";
        var name = animation.data().name();
        return name == null ? "" : name;
    }
}
