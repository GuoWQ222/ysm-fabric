/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.platform;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.state.GameRenderState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jspecify.annotations.Nullable;

/**
 * Fabric port, multi-version build: vanilla calls whose names differ between the supported Minecraft versions. The
 * checked-in code is the 26.2 variant; the {@code //#if MC} blocks are switched by the build's preprocessor.
 */
public final class McCompat {
    private McCompat() {}

    /** The open screen (26.2: Gui owns it; 26.1: Minecraft.screen). */
    /** Whether the HUD is hidden (F1). Loader HUD layers that are not part of the vanilla HUD check this themselves. */
    public static boolean hudHidden() {
        //#if MC >= 26.2
        return Minecraft.getInstance().gui.hud.isHidden();
        //#else
        //$$ return Minecraft.getInstance().options.hideGui;
        //#endif
    }

    public static @Nullable Screen screen() {
        //#if MC >= 26.2
        return Minecraft.getInstance().gui.screen();
        //#else
        //$$ return Minecraft.getInstance().screen;
        //#endif
    }

    public static void setScreen(@Nullable Screen screen) {
        //#if MC >= 26.2
        Minecraft.getInstance().gui.setScreen(screen);
        //#else
        //$$ Minecraft.getInstance().setScreen(screen);
        //#endif
    }

    public static @Nullable Overlay overlay() {
        //#if MC >= 26.2
        return Minecraft.getInstance().gui.overlay();
        //#else
        //$$ return Minecraft.getInstance().getOverlay();
        //#endif
    }

    public static com.mojang.blaze3d.platform.Lighting lighting() {
        //#if MC >= 26.2
        return Minecraft.getInstance().gameRenderer.lighting();
        //#else
        //$$ return Minecraft.getInstance().gameRenderer.getLighting();
        //#endif
    }

    public static Camera mainCamera() {
        //#if MC >= 26.2
        return Minecraft.getInstance().gameRenderer.mainCamera();
        //#else
        //$$ return Minecraft.getInstance().gameRenderer.getMainCamera();
        //#endif
    }

    public static FeatureRenderDispatcher featureRenderDispatcher() {
        //#if MC >= 26.2
        return Minecraft.getInstance().gameRenderer.featureRenderDispatcher();
        //#else
        //$$ return Minecraft.getInstance().gameRenderer.getFeatureRenderDispatcher();
        //#endif
    }

    public static GameRenderState gameRenderState() {
        //#if MC >= 26.2
        return Minecraft.getInstance().gameRenderer.gameRenderState();
        //#else
        //$$ return Minecraft.getInstance().gameRenderer.getGameRenderState();
        //#endif
    }

    /** RGB of a legacy chat color (26.2: TextColor constants; 26.1: ChatFormatting.getColor()). */
    public static int rgb(ChatFormatting format) {
        //#if MC >= 26.2
        return net.minecraft.network.chat.TextColor.fromLegacyFormat(format).getValue();
        //#else
        //$$ return java.util.Objects.requireNonNull(format.getColor(), "not a color");
        //#endif
    }

    public static Block redBed() {
        //#if MC >= 26.2
        return Blocks.BED.red();
        //#else
        //$$ return Blocks.RED_BED;
        //#endif
    }

    public static EntityType<?> horse() {
        //#if MC >= 26.2
        return net.minecraft.world.entity.EntityTypes.HORSE;
        //#else
        //$$ return EntityType.HORSE;
        //#endif
    }

    public static EntityType<?> pig() {
        //#if MC >= 26.2
        return net.minecraft.world.entity.EntityTypes.PIG;
        //#else
        //$$ return EntityType.PIG;
        //#endif
    }

    /** Rotates by a quaternion (26.3: PoseStack.rotate; before: mulPose). */
    public static void rotate(com.mojang.blaze3d.vertex.PoseStack pose, org.joml.Quaternionfc rotation) {
        //#if MC >= 26.3
        //$$ pose.rotate(rotation);
        //#else
        pose.mulPose(rotation);
        //#endif
    }

    //#if MC >= 26.3
    //$$ // 26.3 keeps the swing in a private LivingEntity.SwingState
    //$$ private static final java.lang.invoke.VarHandle SWING_STATE;
    //$$ private static final java.lang.invoke.VarHandle SWING_TICKS;
    //$$
    //$$ static {
    //$$     try {
    //$$         var lookup = java.lang.invoke.MethodHandles.lookup();
    //$$         SWING_STATE = java.lang.invoke.MethodHandles.privateLookupIn(net.minecraft.world.entity.LivingEntity.class, lookup)
    //$$             .findVarHandle(net.minecraft.world.entity.LivingEntity.class, "swingState",
    //$$                 net.minecraft.world.entity.LivingEntity.SwingState.class);
    //$$         SWING_TICKS = java.lang.invoke.MethodHandles.privateLookupIn(net.minecraft.world.entity.LivingEntity.SwingState.class, lookup)
    //$$             .findVarHandle(net.minecraft.world.entity.LivingEntity.SwingState.class, "ticks", int.class);
    //$$     } catch (ReflectiveOperationException failure) {
    //$$         throw new ExceptionInInitializerError(failure);
    //$$     }
    //$$ }
    //$$
    //#endif
    public static boolean isSwinging(net.minecraft.world.entity.LivingEntity entity) {
        //#if MC >= 26.3
        //$$ return entity.isSwinging();
        //#else
        return entity.swinging;
        //#endif
    }

    /** Ticks since the swing started (26.3: SwingState.ticks). */
    public static int swingTime(net.minecraft.world.entity.LivingEntity entity) {
        //#if MC >= 26.3
        //$$ return (int)SWING_TICKS.get(SWING_STATE.get(entity));
        //#else
        return entity.swingTime;
        //#endif
    }

    public static net.minecraft.world.InteractionHand swingingArm(net.minecraft.world.entity.LivingEntity entity) {
        //#if MC >= 26.3
        //$$ var swing = entity.getCurrentSwing();
        //$$ return swing == null ? null : swing.hand();
        //#else
        return entity.swingingArm;
        //#endif
    }

    public static float attackAnim(net.minecraft.world.entity.LivingEntity entity, float partialTick) {
        //#if MC >= 26.3
        //$$ return entity.getSwingAnimation(partialTick);
        //#else
        return entity.getAttackAnim(partialTick);
        //#endif
    }

    /** LivingEntity.swing(hand, updateSelf) (26.3 also takes the swing animation). */
    public static void swing(net.minecraft.world.entity.LivingEntity entity, net.minecraft.world.InteractionHand hand,
        boolean updateSelf) {
        //#if MC >= 26.3
        //$$ entity.swing(hand, net.minecraft.world.item.component.SwingAnimation.DEFAULT, updateSelf);
        //#else
        entity.swing(hand, updateSelf);
        //#endif
    }

    /** Starts a swing on this client only (no packet), as Better Combat's attack start needs. */
    public static void startLocalSwing(net.minecraft.world.entity.LivingEntity entity,
        net.minecraft.world.InteractionHand hand) {
        //#if MC >= 26.3
        //$$ var animation = net.minecraft.world.item.component.SwingAnimation.DEFAULT;
        //$$ ((net.minecraft.world.entity.LivingEntity.SwingState)SWING_STATE.get(entity)).startIfAble(hand, animation,
        //$$     animation.duration());
        //#else
        entity.swingingArm = hand;
        entity.swingTime = -1;
        entity.swinging = true;
        //#endif
    }

    public static int refreshRate() {
        //#if MC >= 26.3
        //$$ return Math.round(Minecraft.getInstance().getWindow().getActiveVideoMode().getRefreshRate());
        //#else
        return Minecraft.getInstance().getWindow().getRefreshRate();
        //#endif
    }

    public static void openUri(String uri) {
        //#if MC >= 26.3
        //$$ com.mojang.blaze3d.Blaze3D.openUri(java.net.URI.create(uri));
        //#else
        net.minecraft.util.Util.getPlatform().openUri(uri);
        //#endif
    }

    public static void openPath(java.nio.file.Path path) {
        //#if MC >= 26.3
        //$$ com.mojang.blaze3d.Blaze3D.openPath(path);
        //#else
        net.minecraft.util.Util.getPlatform().openFile(path.toFile());
        //#endif
    }

    /** The second key number of a key event (GLFW scancode; 26.3: SDL keycode), as KeyEvent's constructor takes it. */
    public static int scancode(net.minecraft.client.input.KeyEvent event) {
        //#if MC >= 26.3
        //$$ return event.keycode();
        //#else
        return event.scancode();
        //#endif
    }

    public static EntityType<?> oakBoat() {
        //#if MC >= 26.2
        return net.minecraft.world.entity.EntityTypes.OAK_BOAT;
        //#else
        //$$ return EntityType.OAK_BOAT;
        //#endif
    }
}
