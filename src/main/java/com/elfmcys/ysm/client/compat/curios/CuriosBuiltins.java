// Modified for the unofficial Fabric 26.2 port by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.compat.curios;

import com.elfmcys.ysm.api.annotation.MolangDependency;
import com.elfmcys.ysm.api.molang.MolangContextView;
import com.elfmcys.ysm.client.animation.debug.DebugSource;
import com.elfmcys.ysm.client.entity.AnimatableEntity;
import com.elfmcys.ysm.expression.annotation.BuiltinComponent;
import com.elfmcys.ysm.expression.annotation.BuiltinFunction;
import com.elfmcys.ysm.expression.host.EntityComponent;
import com.elfmcys.ysm.geckolib3.core.event.predicate.AnimationEvent;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.ArrayList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import org.apache.commons.lang3.StringUtils;

/**
 * Curios-owned callable surface, loaded only after the Curios group is admitted. Fabric port: kept so the catalog (and
 * models that call these functions, e.g. the built-in tactics wine fox) is unchanged; the queries go through the
 * CuriosCompat stand-in.
 */
@BuiltinComponent(value = "ysm", dependencies = @MolangDependency(modId = "curios"))
public final class CuriosBuiltins implements EntityComponent<LivingEntity> {
    private DebugSource debug;

    public CuriosBuiltins(AnimatableEntity<LivingEntity> owner) {}

    @Override
    public void beginEval(AnimationEvent<?> event) {
        debug = event.getDebugSource();
    }

    @Override
    public void endEval() {
        debug = null;
    }

    @BuiltinFunction("has_any_curios")
    public static Boolean hasAnyCurios(MolangContextView<LivingEntity> context, int argc, String slotType,
        String... names) {
        if (StringUtils.isEmpty(slotType))
            return null;
        var items = new ReferenceOpenHashSet<Item>();
        for (String name : names) {
            var id = Identifier.tryParse(name);
            if (id == null)
                return null;
            BuiltInRegistries.ITEM.getOptional(id).ifPresent(items::add);
        }
        return CuriosCompat.hasAnyItemEquipped(context.subject(), slotType, items);
    }

    @BuiltinFunction(value = "has_any_curios_with_all_tags", minArgs = 2)
    public static Boolean hasAnyCuriosWithAllTags(MolangContextView<LivingEntity> context, int argc, String slotType,
        String... names) {
        return hasAnyCuriosWithTags(context, slotType, names, true);
    }

    @BuiltinFunction(value = "has_any_curios_with_any_tag", minArgs = 2)
    public static Boolean hasAnyCuriosWithAnyTag(MolangContextView<LivingEntity> context, int argc, String slotType,
        String... names) {
        return hasAnyCuriosWithTags(context, slotType, names, false);
    }

    private static Boolean hasAnyCuriosWithTags(MolangContextView<LivingEntity> context, String slotType,
        String[] names, boolean all) {
        if (StringUtils.isEmpty(slotType))
            return null;
        var tags = new ArrayList<TagKey<Item>>(names.length);
        for (String name : names) {
            var id = Identifier.tryParse(name);
            if (id == null)
                return null;
            tags.add(TagKey.create(Registries.ITEM, id));
        }
        return all ? CuriosCompat.hasAnyItemEquippedWithAllTag(context.subject(), slotType, tags)
            : CuriosCompat.hasAnyItemEquippedWithAnyTag(context.subject(), slotType, tags);
    }

    @BuiltinFunction(value = "dump_curios", alwaysOrigin = true, action = true)
    public void dumpCurios(MolangContextView<LivingEntity> context) {
        if (debug != null)
            debug.print(Component.literal("Curios is not available on Fabric"));
    }
}
