// Modified for the unofficial Fabric 26.2 port by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.expression.host.builtin;

import com.elfmcys.ysm.accessor.IArrowExtraInfo;
import com.elfmcys.ysm.api.molang.MolangContextView;
import com.elfmcys.ysm.expression.annotation.BuiltinComponent;
import com.elfmcys.ysm.expression.annotation.BuiltinFunction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;

@BuiltinComponent("ysm")
public final class ProjectileBuiltins {
    private ProjectileBuiltins() {}

    @BuiltinFunction("in_ground")
    public static boolean inGround(MolangContextView<net.minecraft.world.entity.projectile.arrow.AbstractArrow> context) {
        return context.subject() instanceof com.elfmcys.ysm.accessor.IArrowExtraInfo info && info.ysm$isInGround();
    }

    @BuiltinFunction("on_ground_time")
    public static float onGroundTime(MolangContextView<net.minecraft.world.entity.projectile.arrow.AbstractArrow> context) {
        return context.subject() instanceof com.elfmcys.ysm.accessor.IArrowExtraInfo info ? info.ysm$inGroundTime() : 0;
    }

    @BuiltinFunction("is_spectral_arrow")
    public static boolean
        isSpectralArrow(MolangContextView<net.minecraft.world.entity.projectile.arrow.AbstractArrow> context) {
        return context.subject() instanceof net.minecraft.world.entity.projectile.arrow.SpectralArrow;
    }

    @BuiltinFunction("is_biting")
    public static boolean isBiting(MolangContextView<net.minecraft.world.entity.projectile.FishingHook> context) {
        return ((com.elfmcys.ysm.mixin.fabric.FishingHookAccessor)context.subject()).ysm$IsBiting();
    }

    @BuiltinFunction("hooked_in")
    public static String hookedIn(MolangContextView<net.minecraft.world.entity.projectile.FishingHook> context) {
        var hooked = ((com.elfmcys.ysm.mixin.fabric.FishingHookAccessor)context.subject()).ysm$GetHookedIn();
        if (hooked == null)
            return "";
        var id = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(hooked.getType());
        return id == null ? "" : id.toString();
    }

    @BuiltinFunction("throwable_item")
    public static String
        throwableItem(MolangContextView<net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile> context) {
        var item =
            ((com.elfmcys.ysm.mixin.fabric.ThrowableItemProjectileAccessor)context.subject()).ysm$GetDefaultItem();
        var id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item);
        return id == null ? "" : id.toString();
    }

    @BuiltinFunction("projectile_owner")
    @com.elfmcys.ysm.api.annotation.MolangNullable
    public static Entity projectileOwner(MolangContextView<net.minecraft.world.entity.projectile.Projectile> context) {
        return context.subject().getOwner();
    }

    @BuiltinFunction("shoot_item_id")
    public static String shootItemId(MolangContextView<Entity> context) {

        var entity = context.subject();
        return entity instanceof AbstractArrow && entity instanceof IArrowExtraInfo extra ? extra.ysm$getShootItemId() : "";
    }
}
