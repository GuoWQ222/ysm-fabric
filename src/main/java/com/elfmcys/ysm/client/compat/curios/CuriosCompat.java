// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.compat.curios;

import java.util.Collection;
import java.util.Set;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;

/**
 * Fabric port stand-in: Curios is a Forge mod. The ysm:has_any_curios* builtins stay declared (CuriosBuiltins) and are
 * admitted only when a "curios" mod is installed; these queries report nothing equipped.
 */
public final class CuriosCompat {
    private CuriosCompat() {}

    public static void init() {}

    public static boolean isInstalled() {
        return false;
    }

    public static boolean hasAnyItemEquipped(LivingEntity entity, String slotType, Set<Item> items) {
        return false;
    }

    public static boolean hasAnyItemEquippedWithAllTag(LivingEntity entity, String slotType,
        Collection<TagKey<Item>> tags) {
        return false;
    }

    public static boolean hasAnyItemEquippedWithAnyTag(LivingEntity entity, String slotType,
        Collection<TagKey<Item>> tags) {
        return false;
    }
}
