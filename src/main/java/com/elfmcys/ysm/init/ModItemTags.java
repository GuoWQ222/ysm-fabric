// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.init;

import com.elfmcys.ysm.YesSteveModel;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ModItemTags {
    public static final TagKey<Item> AXES = tag("axes");
    public static final TagKey<Item> HOES = tag("hoes");
    public static final TagKey<Item> PICKAXES = tag("pickaxes");
    public static final TagKey<Item> SHOVELS = tag("shovels");
    public static final TagKey<Item> SWORDS = tag("swords");
    public static final TagKey<Item> THROWABLE_POTION = tag("throwable_potion");
    public static final TagKey<Item> BOWS = tag("bows");
    public static final TagKey<Item> CROSSBOWS = tag("crossbows");
    public static final TagKey<Item> FISHING_RODS = tag("fishing_rods");
    public static final TagKey<Item> SHIELDS = tag("shields");
    public static final TagKey<Item> TRIDENTS = tag("tridents");
    public static final TagKey<Item> SLASH_BLADE = tag("slashblade");
    /** 2.6.5 parity: maces (c:tools/mace + minecraft:mace) and 26.x spears ("pike", classified as "lance"). */
    public static final TagKey<Item> MACE = tag("mace");
    public static final TagKey<Item> PIKE = tag("pike");

    private static TagKey<Item> tag(String name) {
        return TagKey.create(net.minecraft.core.registries.Registries.ITEM, Identifier.fromNamespaceAndPath(YesSteveModel.MOD_ID, name));
    }
}
