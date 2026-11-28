/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.legacy.state;

import com.elfmcys.ysm.platform.render.YsmRenderStateKeys;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
//#if MC >= 1.20.5
import net.minecraft.world.entity.EntityAttachment;
//#endif
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.phys.Vec3;

/**
 * Minecraft before 1.21.2: builds YSM's render-state stand-ins from the entity, filling the fields as 1.21.2's
 * extractRenderState does (only those YSM reads).
 */
public final class StateExtractor {
    private StateExtractor() {}

    /** A state of the entity's kind, filled; light and outline as the dispatcher would pass them. */
    public static EntityRenderState extract(Entity entity, float partialTick) {
        EntityRenderState state;
        if (entity instanceof AbstractClientPlayer)
            state = new PlayerRenderState();
        else if (entity instanceof LivingEntity)
            state = new LivingEntityRenderState();
        else if (entity instanceof FishingHook)
            state = new FishingHookRenderState();
        else if (entity instanceof AbstractMinecart)
            state = new MinecartRenderState();
        else
            state = new EntityRenderState();
        fill(entity, partialTick, state);
        return state;
    }

    private static void fill(Entity entity, float partialTick, EntityRenderState state) {
        var minecraft = Minecraft.getInstance();
        var dispatcher = minecraft.getEntityRenderDispatcher();
        YsmRenderStateKeys.setEntity(state, entity, partialTick);
        YsmRenderStateKeys.setLight(state, dispatcher.getPackedLightCoords(entity, partialTick));
        YsmRenderStateKeys.setOutlineColor(state,
            minecraft.shouldEntityAppearGlowing(entity) ? 0xFF000000 | entity.getTeamColor() : 0);
        state.x = Mth.lerp(partialTick, entity.xOld, entity.getX());
        state.y = Mth.lerp(partialTick, entity.yOld, entity.getY());
        state.z = Mth.lerp(partialTick, entity.zOld, entity.getZ());
        state.ageInTicks = entity.tickCount + partialTick;
        state.boundingBoxWidth = entity.getBbWidth();
        state.boundingBoxHeight = entity.getBbHeight();
        state.eyeHeight = entity.getEyeHeight();
        state.distanceToCameraSq = dispatcher.distanceToSqr(entity);
        state.isInvisible = entity.isInvisible();
        state.isDiscrete = entity.isDiscrete();
        state.displayFireAnimation = entity.displayFireAnimation();
        state.nameTag = entity.hasCustomName() && entity.isCustomNameVisible() ? entity.getDisplayName() : null;
        //#if MC >= 1.20.5
        state.nameTagAttachment = entity.getAttachments().getNullable(EntityAttachment.NAME_TAG, 0,
            entity.getViewYRot(partialTick));
        //#else
        //$$ // before 1.20.5: no attachments; the tag sits at getNameTagOffsetY (height + 0.5, the 0.5 added when drawn)
        //$$ state.nameTagAttachment = new net.minecraft.world.phys.Vec3(0, entity.getNameTagOffsetY() - 0.5, 0);
        //#endif
        //#if MC >= 1.21
        if (entity instanceof net.minecraft.world.entity.Leashable leashable && leashable.getLeashHolder() != null) {
        //#else
        //$$ // before 1.21 only mobs are leashed
        //$$ if (entity instanceof net.minecraft.world.entity.Mob leashable && leashable.getLeashHolder() != null) {
        //#endif
            var leash = new EntityRenderState.LeashState();
            leash.entity = entity;
            leash.holder = leashable.getLeashHolder();
            leash.partialTick = partialTick;
            state.leashState = leash;
        }
        if (entity instanceof LivingEntity living && state instanceof LivingEntityRenderState livingState)
            fillLiving(living, partialTick, livingState);
        if (entity instanceof AbstractClientPlayer player && state instanceof PlayerRenderState playerState) {
            playerState.skin = player.getSkin();
            playerState.showCape = player.isModelPartShown(PlayerModelPart.CAPE)
                && playerState.skin.capeTexture() != null;
        }
        if (entity instanceof FishingHook hook && state instanceof FishingHookRenderState hookState)
            hookState.lineOriginOffset = lineOrigin(hook, partialTick);
        if (entity instanceof AbstractMinecart minecart && state instanceof MinecartRenderState cartState)
            fillMinecart(minecart, partialTick, cartState);
    }

    private static void fillLiving(LivingEntity living, float partialTick, LivingEntityRenderState state) {
        float body = Mth.rotLerp(partialTick, living.yBodyRotO, living.yBodyRot);
        float head = Mth.rotLerp(partialTick, living.yHeadRotO, living.yHeadRot);
        state.bodyRot = body;
        state.yRot = Mth.wrapDegrees(head - body);
        state.xRot = living.getViewXRot(partialTick);
        state.deathTime = living.deathTime > 0 ? living.deathTime + partialTick : 0;
        state.walkAnimationPos = living.walkAnimation.position(partialTick);
        state.walkAnimationSpeed = living.walkAnimation.speed(partialTick);
        state.scale = living.getScale();
        state.isUpsideDown = LivingEntityRenderer.isEntityUpsideDown(living);
        state.isFullyFrozen = living.isFullyFrozen();
        state.isBaby = living.isBaby();
        state.pose = living.getPose();
        state.bedOrientation = living.hasPose(net.minecraft.world.entity.Pose.SLEEPING) ? living.getBedOrientation() : null;
    }

    private static void fillMinecart(AbstractMinecart minecart, float partialTick, MinecartRenderState state) {
        state.isNewRender = false;
        state.yRot = Mth.rotLerp(partialTick, minecart.yRotO, minecart.getYRot());
        state.xRot = Mth.lerp(partialTick, minecart.xRotO, minecart.getXRot());
        state.posOnRail = minecart.getPos(state.x, state.y, state.z);
        if (state.posOnRail != null) {
            state.frontPos = minecart.getPosOffs(state.x, state.y, state.z, 0.3F);
            state.backPos = minecart.getPosOffs(state.x, state.y, state.z, -0.3F);
        }
    }

    /** The fishing line start relative to the hook, as upstream YSM (1.20.1) and vanilla compute it. */
    private static Vec3 lineOrigin(FishingHook hook, float partialTick) {
        Player player = hook.getPlayerOwner();
        if (player == null)
            return Vec3.ZERO;
        int handDirection = player.getMainArm() == net.minecraft.world.entity.HumanoidArm.RIGHT ? 1 : -1;
        if (!(player.getMainHandItem().getItem() instanceof FishingRodItem))
            handDirection = -handDirection;
        float swing = Mth.sin(Mth.sqrt(player.getAttackAnim(partialTick)) * Mth.PI);
        float bodyRot = Mth.lerp(partialTick, player.yBodyRotO, player.yBodyRot) * (Mth.PI / 180F);
        double sin = Mth.sin(bodyRot);
        double cos = Mth.cos(bodyRot);
        double handOffset = handDirection * 0.35;
        double armDistance = 0.8;
        double px;
        double py;
        double pz;
        float eyeOffset;
        var dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        var options = dispatcher.options;
        if (options != null && options.getCameraType().isFirstPerson() && player == Minecraft.getInstance().player) {
            double fovScale = 960.0 / options.fov().get();
            Vec3 point = dispatcher.camera.getNearPlane().getPointOnPlane((float)handDirection * 0.525F, -0.1F)
                .scale(fovScale).yRot(swing * 0.5F).xRot(-swing * 0.7F);
            px = Mth.lerp(partialTick, player.xo, player.getX()) + point.x;
            py = Mth.lerp(partialTick, player.yo, player.getY()) + point.y;
            pz = Mth.lerp(partialTick, player.zo, player.getZ()) + point.z;
            eyeOffset = player.getEyeHeight();
        } else {
            px = Mth.lerp(partialTick, player.xo, player.getX()) - cos * handOffset - sin * armDistance;
            py = player.yo + player.getEyeHeight() + (player.getY() - player.yo) * partialTick - 0.45;
            pz = Mth.lerp(partialTick, player.zo, player.getZ()) - sin * handOffset + cos * armDistance;
            eyeOffset = player.isCrouching() ? -0.1875F : 0.0F;
        }
        double hx = Mth.lerp(partialTick, hook.xo, hook.getX());
        double hy = Mth.lerp(partialTick, hook.yo, hook.getY()) + 0.25;
        double hz = Mth.lerp(partialTick, hook.zo, hook.getZ());
        return new Vec3(px - hx, py - hy + eyeOffset, pz - hz);
    }
}
