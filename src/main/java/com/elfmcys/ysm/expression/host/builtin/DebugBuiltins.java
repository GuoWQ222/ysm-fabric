// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.expression.host.builtin;

import com.elfmcys.ysm.api.molang.MolangContextView;
import com.elfmcys.ysm.client.animation.debug.DebugSource;
import com.elfmcys.ysm.client.entity.AnimatableEntity;
import com.elfmcys.ysm.expression.annotation.BuiltinComponent;
import com.elfmcys.ysm.expression.annotation.BuiltinFunction;
import com.elfmcys.ysm.expression.host.EntityComponent;
import com.elfmcys.ysm.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.ysm.util.EquipmentUtil;
import java.util.Comparator;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.nbt.CompoundTag;

@BuiltinComponent("ysm")
public final class DebugBuiltins implements EntityComponent<Entity> {
    private DebugSource debug;

    public DebugBuiltins(AnimatableEntity<Entity> owner) {}

    @Override
    public void beginEval(AnimationEvent<?> event) {
        debug = event.getDebugSource();
    }

    @Override
    public void endEval() {
        debug = null;
    }

    @BuiltinFunction(value = "dump_mods", alwaysOrigin = true, action = true)
    public void dumpMods(MolangContextView<Entity> context) {
        if (debug == null)
            return;
        com.elfmcys.ysm.platform.Platform.mods().stream()
            .sorted(Comparator.comparing(meta -> meta.name()))
            .forEach(meta -> debug.print(Component.literal("Mod: display ").append(copy(meta.name()))
                .append(Component.literal("  id ").append(copy(meta.id())))));
    }

    @BuiltinFunction(value = "dump_effects", alwaysOrigin = true, action = true)
    public void dumpEffects(MolangContextView<Entity> context) {
        if (debug == null)
            return;
        var subject = context.subject();
        Iterable<net.minecraft.world.effect.MobEffectInstance> effects = subject instanceof LivingEntity living
            ? living.getActiveEffects()
            : subject instanceof Arrow
                ? ((com.elfmcys.ysm.mixin.fabric.ArrowPotionAccessor)subject).ysm$getPotionContents().getAllEffects()
                : null;
        if (effects == null)
            return;
        for (var effect : effects) {
            var id = net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.getKey(effect.getEffect().value());
            if (id != null)
                debug.print(Component.literal("Effect: display ")
                    .append(copy(effect.getEffect().value().getDisplayName().getString(99)))
                    .append(Component.literal("  name ").append(copy(id.toString())))
                    .append("  lv=" + (effect.getAmplifier() + 1)));
        }
    }

    @BuiltinFunction(value = "dump_biome", alwaysOrigin = true, action = true)
    public void dumpBiome(MolangContextView<Entity> context) {
        if (debug == null)
            return;
        var entity = context.subject();
        var biome = entity.level().getBiome(entity.blockPosition());
        biome.unwrapKey().ifPresent(key -> print("Name ", key.identifier().toString()));
        biome.tags().forEach(tag -> print("Tag ", tag.location().toString()));
    }

    @BuiltinFunction(value = "dump_equipped_item", alwaysOrigin = true, action = true)
    public void dumpEquippedItem(MolangContextView<Entity> context, String name) {
        if (debug == null || !(context.subject() instanceof LivingEntity living))
            return;
        var slot = switch (name.toLowerCase(Locale.ENGLISH)) {
            case "chest" -> EquipmentSlot.CHEST;
            case "feet" -> EquipmentSlot.FEET;
            case "head" -> EquipmentSlot.HEAD;
            case "legs" -> EquipmentSlot.LEGS;
            case "mainhand" -> EquipmentSlot.MAINHAND;
            case "offhand" -> EquipmentSlot.OFFHAND;
            default -> null;
        };
        if (slot == null) {
            debug.print("Illegal slot type: %s.", name);
            return;
        }
        var stack = EquipmentUtil.getEquippedItem(living, slot);
        var id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (stack.isEmpty() || id == null)
            return;
        print("Display ", stack.getItem().getName(stack).getString(99));
        print("Name ", id.toString());
        stack.typeHolder().tags().forEach(tag -> print("Tag ", tag.location().toString()));
        for (var entry : stack.getEnchantments().entrySet()) {
            var enchantment = entry.getKey();
            var enchantmentId = enchantment.unwrapKey().map(key -> key.identifier().toString()).orElse("?");
            debug.print(Component.literal("Enchantment: display ")
                .append(copy(net.minecraft.world.item.enchantment.Enchantment.getFullname(enchantment, entry.getIntValue())
                    .getString(99)))
                .append(Component.literal("  name ").append(copy(enchantmentId))));
        }
    }

    @BuiltinFunction(value = "dump_relative_block", alwaysOrigin = true, action = true)
    public void dumpRelativeBlock(MolangContextView<Entity> context, float x, float y, float z) {
        if (debug == null || Math.abs(x) > 5 || Math.abs(y) > 5 || Math.abs(z) > 5)
            return;
        var entity = context.subject();
        var pos = new BlockPos((int)Math.round(entity.getX() + x - 0.5d), (int)Math.round(entity.getY() + y - 0.5d),
            (int)Math.round(entity.getZ() + z - 0.5d));
        var block = entity.level().getBlockState(pos);
        var id = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block.getBlock());
        if (id == null)
            return;
        print("Display ", block.getBlock().getName().getString(99));
        print("Name ", id.toString());
        block.typeHolder().tags().forEach(tag -> print("Tag ", tag.location().toString()));
    }

    private void print(String prefix, String value) {
        debug.print(Component.literal(prefix).append(copy(value)));
    }

    private static Component copy(String value) {
        return ComponentUtils.copyOnClickText(value);
    }
}
