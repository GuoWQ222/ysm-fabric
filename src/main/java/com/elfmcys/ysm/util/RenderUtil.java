// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.util;

import com.elfmcys.ysm.platform.capability.ICapabilityHolder;
import com.elfmcys.ysm.capability.PlayerAnimatableCapabilityProvider;
import com.elfmcys.ysm.capability.VehicleAnimatableCapabilityProvider;
import com.elfmcys.ysm.client.compat.FirstPersonCompat;
import com.elfmcys.ysm.client.compat.IrisCompat;
import com.elfmcys.ysm.client.entity.AnimatableEntity;
import com.elfmcys.ysm.client.entity.CustomHumanoidEntity;
import com.elfmcys.ysm.client.entity.IPreviewEntity;
import com.elfmcys.ysm.client.event.RegisterEntityRenderersEvent;
import com.elfmcys.ysm.client.gui.GuiFrameInput;
import com.elfmcys.ysm.client.gui.GuiFrameTasks;
import com.elfmcys.ysm.client.gui.GuiModelFraming;
import com.elfmcys.ysm.client.gui.pip.YsmGuiModelRenderState;
import com.elfmcys.ysm.client.gui.pip.YsmGuiModels;
import com.elfmcys.ysm.client.renderer.replace.EntityRendererReplace;
import com.elfmcys.ysm.geckolib3.geo.GeoRenderData;
import com.elfmcys.ysm.geckolib3.geo.GeoReplacedEntityRenderer;
import com.elfmcys.ysm.geckolib3.geo.RenderContext;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.concurrent.ExecutionException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4fStack;
import org.joml.Quaternionf;

/**
 * Render helpers. Fabric port (26.x): GUI model drawing is submitted as picture-in-picture states (YsmGuiModels); the
 * painters below keep 1.20's GUI-space transforms, with the camera part on the model-view stack.
 */
@SuppressWarnings("all")
public final class RenderUtil {
    private static final int GUI_LIGHT = 0xF000F0;
    private static final BlockDisplayContext BLOCK_DISPLAY_CONTEXT = BlockDisplayContext.create();
    private static boolean renderingInInventory = false;
    private static boolean renderingInPaperDoll = false;
    private static boolean renderingLevel = false;

    private RenderUtil() {}

    public static void setRenderingInInventory(boolean value) {
        renderingInInventory = value;
    }

    public static void setRenderingInPaperDoll(boolean renderingEntitiesInPaperDoll) {
        RenderUtil.renderingInPaperDoll = renderingEntitiesInPaperDoll;
    }

    public static void setRenderingLevel(boolean renderingLevel) {
        RenderUtil.renderingLevel = renderingLevel;
    }

    public static boolean isRenderingLevel() {
        RenderSystem.assertOnRenderThread();
        return renderingLevel;
    }

    public static RenderContext extractRenderContext() {
        RenderSystem.assertOnRenderThread();
        return new RenderContext(renderingLevel && !FirstPersonCompat.isRenderingPlayer(),
            IrisCompat.isRenderingShadow(), FirstPersonCompat.isRenderingPlayer(), renderingInInventory,
            renderingInPaperDoll, false, false);
    }

    /** Vertical offset vanilla applied to a passenger on its vehicle (1.20 riding offsets), as seen from the vehicle. */
    public static double passengerHeight(Entity vehicle, Entity passenger) {
        var ridingPosition = vehicle.getPassengerRidingPosition(passenger);
        var attachment = passenger.getVehicleAttachmentPoint(vehicle);
        return ridingPosition.y - vehicle.getY() - attachment.y;
    }

    public static void adjustPassengerPosition(Entity entity, PoseStack poseStack, float partialTicks) {
        Entity vehicle = entity.getVehicle();
        if (vehicle == null)
            return;
        ICapabilityHolder.of(vehicle).getCapability(VehicleAnimatableCapabilityProvider.CAP).ifPresent(vehicleCap -> {
            if (!vehicleCap.isAssigned())
                return;
            vehicleCap.waitForRenderUpdate();
            if (!vehicleCap.hasMatchingInstallation())
                return;
            int index = vehicle.getPassengers().indexOf(entity);
            if (index < 0)
                return;
            // 2.6.5 parity: the passenger sits at the vehicle model's PassengerLocator for its index (recorded when the
            // vehicle was drawn), then undoes the vehicle yaw; without a seat, vanilla placement stays.
            if (!(vehicleCap instanceof com.elfmcys.ysm.client.entity.CustomVehicleEntity seats))
                return;
            var seat = seats.passengerSeat(index);
            if (seat == null)
                return;
            poseStack.mulPose(seat);
            com.elfmcys.ysm.platform.McCompat.rotate(poseStack, Axis.YN.rotationDegrees(seats.seatAngle()));
            double yOffset = vehicle.position().y - vehicle.getPassengerRidingPosition(entity).y
                + entity.getVehicleAttachmentPoint(vehicle).y;
            boolean playerHasCap = entity instanceof Player player
                && ICapabilityHolder.of(player).getCapability(PlayerAnimatableCapabilityProvider.CAP).isPresent();
            if (playerHasCap)
                yOffset = yOffset - 0.5;
            poseStack.translate(0, yOffset, 0);
        });
    }

    /** A host render state for GUI previews (full bright, no outline, entity pose and visibility). */
    public static LivingEntityRenderState guiRenderState(LivingEntity entity) {
        var state = new LivingEntityRenderState();
        state.pose = entity.getPose();
        state.bedOrientation = entity.getBedOrientation();
        state.isInvisible = entity.isInvisible();
        state.lightCoords = GUI_LIGHT;
        state.outlineColor = 0;
        state.scale = 1.0f;
        state.ageInTicks = entity.tickCount;
        state.boundingBoxHeight = entity.getBbHeight();
        return state;
    }

    // ---- Texture screen (large preview with optional bed / ground) ----

    public static <T extends LivingEntity, TAnimatable extends AnimatableEntity<T> & IPreviewEntity> void
        renderTextureScreenEntity(GuiGraphicsExtractor graphics, int x0, int y0, int x1, int y1, float pPosX,
            float pPosY, float pScale, float pitch, float yaw, float partialTicks, TAnimatable entity,
            GeoReplacedEntityRenderer<T, ? super TAnimatable> renderer, boolean showGround) {
        YsmGuiModels.submit(graphics, x0, y0, x1, y1, (view, collector) -> {
            setRenderingInInventory(true);
            try {
                paintTextureScreenEntity(view, collector, pPosX, pPosY, pScale, pitch, yaw, partialTicks, entity,
                    renderer, showGround);
            } finally {
                setRenderingInInventory(false);
            }
        });
    }

    private static <T extends LivingEntity, TAnimatable extends AnimatableEntity<T> & IPreviewEntity> void
        paintTextureScreenEntity(Matrix4fStack view, SubmitNodeCollector collector, float pPosX, float pPosY,
            float pScale, float pitch, float yaw, float partialTicks, TAnimatable entity,
            GeoReplacedEntityRenderer<T, ? super TAnimatable> renderer, boolean showGround) {
        var living = entity.getEntity();
        view.translate(pPosX, pPosY, 1250.0f);

        PoseStack poseStack = new PoseStack();
        // Depth flip in the pose (not the view): 26.x applies the model-view to positions only, and YSM's normals come
        // from the pose, so a view-space flip left GUI models lit from behind (dark).
        poseStack.scale(1.0f, 1.0f, -1.0f);
        poseStack.translate(0.0D, 0.0D, 1000.0D);
        poseStack.scale(pScale, pScale, pScale);
        poseStack.translate(0, 0.8, 0);
        Quaternionf zp = Axis.ZP.rotationDegrees(180.0F);
        Quaternionf xp = Axis.XP.rotationDegrees(-10 + pitch);
        zp.mul(xp);
        com.elfmcys.ysm.platform.McCompat.rotate(poseStack, zp);

        float yBodyRot = living.yBodyRot;
        float yBodyRotO = living.yBodyRotO;
        float yRot = living.getYRot();
        float yRotO = living.yRotO;
        float xRot = living.getXRot();
        float xRotO = living.xRotO;
        float yHeadRotO = living.yHeadRotO;
        float yHeadRot = living.yHeadRot;
        Pose pose = living.getPose();
        living.yBodyRot = -yaw;
        living.yBodyRotO = -yaw;
        living.setYRot(180);
        living.yRotO = 180;
        living.setXRot(0);
        living.xRotO = 0;
        living.yHeadRot = -yaw;
        living.yHeadRotO = -yaw;
        try {
            var camera = guiCamera(xp);
            var guiAnim = entity.getPreviewInfo();
            if (guiAnim.hasPreview("sleep")) {
                com.elfmcys.ysm.platform.McCompat.rotate(poseStack, Axis.YP.rotationDegrees(yaw - 90));
                poseStack.translate(0.5, 0.5625, 0);
                living.setPose(Pose.SLEEPING);
            }
            if (guiAnim.hasPreview("swim") || guiAnim.hasPreview("swim_stand"))
                living.setPose(Pose.SWIMMING);
            if (guiAnim.hasPreview("sneak") || guiAnim.hasPreview("sneaking"))
                living.setPose(Pose.CROUCHING);
            if (guiAnim.hasPreview("sit"))
                poseStack.translate(0, -0.5, 0);
            if (guiAnim.hasPreview("ride"))
                poseStack.translate(0, 0.85, 0);
            if (guiAnim.hasPreview("ride_pig"))
                poseStack.translate(0, 0.3125, 0);
            if (guiAnim.hasPreview("boat"))
                poseStack.translate(0, -0.45, 0);
            try {
                submitExtraEntity(yaw, entity, partialTicks, poseStack, collector, camera);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
            if (guiAnim.hasPreview("sleep"))
                submitBed(pScale, pitch, yaw, collector);
            if (showGround)
                submitGround(pScale, pitch, yaw, collector);
            renderer.submitReplacement(entity, guiRenderState(living), null, partialTicks, poseStack, collector, null,
                () -> {});
        } finally {
            living.yBodyRot = yBodyRot;
            living.yBodyRotO = yBodyRotO;
            living.setYRot(yRot);
            living.yRotO = yRotO;
            living.setXRot(xRot);
            living.xRotO = xRotO;
            living.yHeadRotO = yHeadRotO;
            living.yHeadRot = yHeadRot;
            living.setPose(pose);
        }
    }

    private static CameraRenderState guiCamera(Quaternionf xp) {
        var camera = new CameraRenderState();
        camera.orientation = new Quaternionf(xp).conjugate();
        return camera;
    }

    private static PoseStack previewBlockPose(float scale, float pitch) {
        PoseStack poseStack = new PoseStack();
        poseStack.translate(0.0D, 0.0D, 1000.0D);
        poseStack.scale(scale, scale, scale);
        poseStack.translate(0, 0.8, 0);
        Quaternionf zp = Axis.ZP.rotationDegrees(180.0F);
        zp.mul(Axis.XP.rotationDegrees(-10 + pitch));
        com.elfmcys.ysm.platform.McCompat.rotate(poseStack, zp);
        return poseStack;
    }

    private static void submitBed(float scale, float pitch, float yaw, SubmitNodeCollector collector) {
        var poseStack = previewBlockPose(scale, pitch);
        com.elfmcys.ysm.platform.McCompat.rotate(poseStack, Axis.YP.rotationDegrees(yaw + 180));
        poseStack.translate(-0.5, 0, 0.5);
        submitBlock(com.elfmcys.ysm.platform.McCompat.redBed().defaultBlockState(), poseStack, collector);
    }

    private static void submitGround(float scale, float pitch, float yaw, SubmitNodeCollector collector) {
        var poseStack = previewBlockPose(scale, pitch);
        com.elfmcys.ysm.platform.McCompat.rotate(poseStack, Axis.YP.rotationDegrees(yaw));
        poseStack.translate(-1.5, -1, -2.5);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                poseStack.translate(0, 0, 1);
                submitBlock(Blocks.GRASS_BLOCK.defaultBlockState(), poseStack, collector);
            }
            poseStack.translate(1, 0, -3);
        }
        poseStack.translate(-1, 1, 1);
        submitBlock(Blocks.SHORT_GRASS.defaultBlockState(), poseStack, collector);
        poseStack.translate(0, 0, 1);
        submitBlock(Blocks.RED_TULIP.defaultBlockState(), poseStack, collector);
    }

    private static void submitBlock(BlockState block, PoseStack poseStack, SubmitNodeCollector collector) {
        var resolver = RegisterEntityRenderersEvent.getBlockModelResolver();
        if (resolver == null)
            return;
        var state = new BlockModelRenderState();
        resolver.update(state, block, BLOCK_DISPLAY_CONTEXT);
        state.submit(poseStack, collector, GUI_LIGHT, OverlayTexture.NO_OVERLAY, 0);
    }

    private static <TAnimatable extends AnimatableEntity<?> & IPreviewEntity> void submitExtraEntity(float yaw,
        TAnimatable animatableEntity, float partialTicks, PoseStack poseStack, SubmitNodeCollector collector,
        CameraRenderState camera) throws ExecutionException {
        if (Minecraft.getInstance().level == null)
            return;
        var player = animatableEntity.getEntity();
        var guiAnim = animatableEntity.getPreviewInfo();
        EntityType<?> type = guiAnim.hasPreview("ride") ? com.elfmcys.ysm.platform.McCompat.horse()
            : guiAnim.hasPreview("ride_pig") ? com.elfmcys.ysm.platform.McCompat.pig() : guiAnim.hasPreview("boat") ? com.elfmcys.ysm.platform.McCompat.oakBoat() : null;
        if (type == null)
            return;
        Entity vehicle = AnimatableCacheUtil.ENTITIES_CACHE.get(EntityType.getKey(type),
            () -> type.create(player.level(), EntitySpawnReason.LOAD));
        if (vehicle == null)
            return;
        var dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        poseStack.pushPose();
        try {
            com.elfmcys.ysm.platform.McCompat.rotate(poseStack, Axis.YP.rotationDegrees(yaw));
            var state = dispatcher.extractEntity(vehicle, partialTicks);
            state.lightCoords = GUI_LIGHT;
            state.shadowPieces.clear();
            dispatcher.submit(state, camera, 0, -passengerHeight(vehicle, player), 0, poseStack, collector);
        } finally {
            poseStack.popPose();
        }
    }

    // ---- Model cards and previews ----

    public static <T extends LivingEntity, TAnimatable extends CustomHumanoidEntity<T>> void renderModelInGui(
        GuiGraphicsExtractor graphics, int x0, int y0, int x1, int y1, float pPosX, float pPosY, float pScale,
        float partialTicks, TAnimatable animatableEntity, GeoReplacedEntityRenderer<T, TAnimatable> renderer,
        boolean disablePreviewRotation, boolean disableEquipments) {
        var input = GuiFrameInput.capture(animatableEntity.getEntity(), partialTicks, disablePreviewRotation,
            disableEquipments, false, false, 0);
        submitModel(graphics, x0, y0, x1, y1, pPosX, pPosY, pScale, animatableEntity, renderer, input, null, false,
            null, 0, 0);
    }

    public static <T extends LivingEntity, TAnimatable extends CustomHumanoidEntity<T>> void renderFramedModelInGui(
        GuiGraphicsExtractor graphics, float left, float top, float width, float height, float partialTicks,
        TAnimatable animatableEntity, GeoReplacedEntityRenderer<T, TAnimatable> renderer,
        boolean disablePreviewRotation, boolean disableEquipments, GuiModelFraming framing) {
        var input = GuiFrameInput.capture(animatableEntity.getEntity(), partialTicks, disablePreviewRotation,
            disableEquipments, false, false, 0);
        submitModel(graphics, Mth.floor(left), Mth.floor(top), Mth.ceil(left + width), Mth.ceil(top + height),
            left + width / 2, top + height / 2, 1, animatableEntity, renderer, input, null, false, framing, width,
            height);
    }

    public static <T extends LivingEntity, TAnimatable extends CustomHumanoidEntity<T>> void renderPreparedModelInGui(
        GuiGraphicsExtractor graphics, int x0, int y0, int x1, int y1, float posX, float posY, float scale,
        TAnimatable entity, GeoReplacedEntityRenderer<T, TAnimatable> renderer, GuiFrameInput input,
        GeoRenderData data) {
        submitModel(graphics, x0, y0, x1, y1, posX, posY, scale, entity, renderer, input, data, true, null, 0, 0);
    }

    /** A painter drawing an animatable entity as renderModelInGui does (also used for offscreen previews). */
    public static <T extends LivingEntity, TAnimatable extends CustomHumanoidEntity<T>> YsmGuiModelRenderState.Painter
        modelPainter(float posX, float posY, float scale, float partialTicks, TAnimatable entity,
            GeoReplacedEntityRenderer<T, TAnimatable> renderer, boolean disablePreviewRotation,
            boolean disableEquipments) {
        var input = GuiFrameInput.capture(entity.getEntity(), partialTicks, disablePreviewRotation, disableEquipments,
            false, false, 0);
        return modelPainter(posX, posY, scale, entity, renderer, input, null, false, null, 0, 0);
    }

    private static <T extends LivingEntity, TAnimatable extends CustomHumanoidEntity<T>> void submitModel(
        GuiGraphicsExtractor graphics, int x0, int y0, int x1, int y1, float posX, float posY, float scale,
        TAnimatable entity, GeoReplacedEntityRenderer<T, TAnimatable> renderer, GuiFrameInput input,
        GeoRenderData data, boolean prepared, GuiModelFraming framing, float width, float height) {
        YsmGuiModels.submit(graphics, x0, y0, x1, y1,
            modelPainter(posX, posY, scale, entity, renderer, input, data, prepared, framing, width, height));
    }

    private static <T extends LivingEntity, TAnimatable extends CustomHumanoidEntity<T>> YsmGuiModelRenderState.Painter
        modelPainter(float posX, float posY, float scale, TAnimatable entity,
            GeoReplacedEntityRenderer<T, TAnimatable> renderer, GuiFrameInput input, GeoRenderData data,
            boolean prepared, GuiModelFraming framing, float width, float height) {
        return (view, collector) -> {
            setRenderingInInventory(true);
            try {
                paintModel(view, collector, posX, posY, scale, entity, renderer, input, data, prepared, framing,
                    width, height);
            } finally {
                if (framing != null)
                    framing.end();
                setRenderingInInventory(false);
            }
        };
    }

    private static <T extends LivingEntity, TAnimatable extends CustomHumanoidEntity<T>> void paintModel(
        Matrix4fStack view, SubmitNodeCollector collector, float posX, float posY, float scale, TAnimatable entity,
        GeoReplacedEntityRenderer<T, TAnimatable> renderer, GuiFrameInput input, GeoRenderData data,
        boolean prepared, GuiModelFraming framing, float width, float height) {
        var applied = input.apply();
        Throwable primary = null;
        try {
            view.translate(posX, posY, 1050.0f);
            var poseStack = new PoseStack();
            // Depth flip in the pose (not the view): 26.x applies the model-view to positions only, and YSM's normals come
            // from the pose, so a view-space flip left GUI models lit from behind (dark).
            poseStack.scale(1.0f, 1.0f, -1.0f);
            poseStack.translate(0.0D, input.disableRotation() ? 5.5 : 0, 1000.0D);
            poseStack.scale(scale, scale, scale);
            var zp = Axis.ZP.rotationDegrees(180.0F);
            var xp = Axis.XP.rotationDegrees(input.disableRotation() ? 0 : -10);
            zp.mul(xp);
            com.elfmcys.ysm.platform.McCompat.rotate(poseStack, zp);
            com.elfmcys.ysm.platform.McCompat.rotate(poseStack, Axis.YP.rotationDegrees(input.vehicleRotation()));
            if (framing != null)
                framing.begin(width, height);
            var state = guiRenderState(entity.getEntity());
            if (prepared)
                renderer.submitPreparedReplacement(entity, data, state, null, input.partialTick(), poseStack,
                    collector, framing, () -> {});
            else
                renderer.submitReplacement(entity, state, null, input.partialTick(), poseStack, collector, framing,
                    () -> {});
        } catch (RuntimeException | Error failure) {
            primary = failure;
            throw failure;
        } finally {
            Throwable failure = primary;
            try {
                applied.close();
            } catch (Throwable error) {
                failure = GuiFrameTasks.append(failure, error);
            }
            if (failure != primary) {
                if (failure instanceof Error error)
                    throw error;
                if (failure instanceof RuntimeException error)
                    throw error;
            }
        }
    }

    // ---- Paper doll (extra player overlay) ----

    public static void renderExtraPlayerEntity(GuiGraphicsExtractor graphics, LocalPlayer player, double posX,
        double posY, float scale, float yawOffset, int z, float partialTicks) {
        int x0 = Mth.floor(posX - scale);
        int y0 = Mth.floor(posY - scale);
        int x1 = Mth.ceil(posX + scale * 2);
        int y1 = Mth.ceil(posY + scale * 3);
        YsmGuiModels.submit(graphics, x0, y0, x1, y1, (view, collector) -> {
            setRenderingInPaperDoll(true);
            try {
                view.translate((float)(posX + scale * 0.5), (float)(posY + scale * 2), 0);
                var poseStack = new PoseStack();
                // Depth flip in the pose (not the view): 26.x applies the model-view to positions only, and YSM's normals come
                // from the pose, so a view-space flip left GUI models lit from behind (dark).
                poseStack.scale(1, 1, -1);
                poseStack.translate(0, 0, -z);
                poseStack.scale(scale, scale, scale);
                Quaternionf zRot = Axis.ZP.rotationDegrees(180.1F);
                Quaternionf yRot =
                    Axis.YP.rotationDegrees(Mth.lerp(partialTicks, player.yBodyRotO, player.yBodyRot) + yawOffset - 180);
                zRot.mul(yRot);
                com.elfmcys.ysm.platform.McCompat.rotate(poseStack, zRot);
                var dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
                var state = dispatcher.extractEntity(player, partialTicks);
                state.lightCoords = 15728880;
                state.shadowPieces.clear();
                var camera = new CameraRenderState();
                camera.orientation = new Quaternionf(yRot).conjugate();
                dispatcher.submit(state, camera, 0, 0, 0, poseStack, collector);
            } finally {
                setRenderingInPaperDoll(false);
            }
        });
    }
}
