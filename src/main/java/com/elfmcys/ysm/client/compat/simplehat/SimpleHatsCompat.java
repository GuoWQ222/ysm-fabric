// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.compat.simplehat;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Fabric port stand-in: the SimpleHatsCompat integration targets a Forge mod and is not ported; every method reports
 * "not installed". The original is kept in src/forge-compat-reference.
 */
@SuppressWarnings("unused")
public final class SimpleHatsCompat {

    private SimpleHatsCompat() {}

    public static void init() {
        
    }

    public static ItemStack getCuriosHead(LivingEntity livingEntity) {
        return null;
    }
}
