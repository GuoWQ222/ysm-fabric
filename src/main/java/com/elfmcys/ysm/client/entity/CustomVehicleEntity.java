// Modified for the unofficial Fabric 26.2 port by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.entity;

import com.elfmcys.ysm.model.resource.client.AnimationStore;
import com.elfmcys.ysm.geckolib3.core.manager.AnimationData;
import com.elfmcys.ysm.model.resource.client.ModelRenderTarget;
import com.elfmcys.ysm.config.ClientConfig;

import com.elfmcys.ysm.client.controller.VehicleOriginController;
import com.elfmcys.ysm.client.controller.collections.VehicleControllerCollection;
import com.elfmcys.ysm.geckolib3.core.builder.Animation;
import com.elfmcys.ysm.geckolib3.core.builder.controller.AnimationControllerData;
import com.elfmcys.ysm.geckolib3.geo.render.built.GeoModel;
import com.elfmcys.ysm.model.resource.client.AcquireResult;
import com.elfmcys.ysm.model.resource.client.ModelRenderTarget;
import com.elfmcys.ysm.model.resource.client.ResourceLease;
import com.elfmcys.ysm.model.resource.client.VehicleModelResources;
import com.elfmcys.ysm.model.service.ClientModelService;
import com.elfmcys.ysm.proto.mixel.manifest.asset.RenderTargetKind;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Objects;
import org.joml.Vector3f;

public class CustomVehicleEntity extends AnimatableEntity<Entity> {

    public CustomVehicleEntity(Entity vehicle) {
        super(vehicle, true);
    }

    // 2.6.5 parity: passenger seats = vehicle rotation * PassengerLocator[i], relative to the vehicle origin, recorded
    // when the vehicle model is drawn (render thread) and applied when its passengers are drawn.
    private final java.util.List<org.joml.Matrix4f> passengerSeats = new java.util.ArrayList<>();
    private float seatAngle;

    public void updatePassengerSeats(java.util.List<org.joml.Matrix4f> seats, float angle) {
        passengerSeats.clear();
        passengerSeats.addAll(seats);
        seatAngle = angle;
    }

    public @org.jetbrains.annotations.Nullable org.joml.Matrix4f passengerSeat(int index) {
        return index >= 0 && index < passengerSeats.size() ? passengerSeats.get(index) : null;
    }

    public float seatAngle() {
        return seatAngle;
    }

    /** Test diagnostics: what the last seat recording found. */
    public volatile String seatDiagnostic = "none";

    @Override
    protected String requestedRenderTargetId() {
        var hash = getModelHash();
        return hash == null
            ? isAssigned() ? ClientModelService.instance()
                .findIntrinsicRenderTarget(RenderTargetKind.VEHICLE,
                    entity.getType().builtInRegistryHolder().key().identifier())
                .orElse(null) : null
            : ClientModelService.instance().findRenderTarget(hash, RenderTargetKind.VEHICLE,
                entity.getType().builtInRegistryHolder().key().identifier()).orElse(null);
    }

    @Override
    protected String fallbackRenderTargetId() {
        return null;
    }

    @Override
    protected void prepareAnimationControllers(ModelRenderTarget target, AnimationData animations) {
        Objects.requireNonNull(target.vehicleResources(), "vehicle resources").controllerFactory().accept(this,
            animations);
    }

    @Override
    protected boolean presentationPermission() {
        return !ClientConfig.DISABLE_VEHICLE_MODEL.get();
    }

    @Override
    protected AnimationControllerData resolveControllerData(ModelRenderTarget target, String name) {
        return target.vehicleResources().controllers().get(name);
    }

    @Nullable
    public Vector3f getRotation() {
        if (!hasInstalledModel())
            return null;
        var origin =
            (VehicleOriginController)getAnimationData().getAnimationController(VehicleControllerCollection.NAME_ORIGIN);
        return origin == null ? null : origin.getRotation();
    }

    @Override
    protected GeoModel resolveGeoModel(ModelRenderTarget target, String texture) {
        return Objects.requireNonNull(target.vehicleResources(), "vehicle resources").model();
    }

    @Override
    @NotNull
    public Identifier getTextureLocation() {
        return getModelRenderTarget().textureId();
    }

    @Override
    protected AnimationStore.View animationSource() {
        return getModelRenderTarget().vehicleResources().animations();
    }

    @Override
    public Animation getAnimation(String name) {
        return readAnimation(getModelRenderTarget().vehicleResources().animations(), name);
    }

    @Override
    public @Nullable AnimationControllerData getAnimationControllerData(String animationControllerName) {
        return getModelRenderTarget().vehicleResources().controllers().get(animationControllerName);
    }

    @Override
    // 2.6.5 draws vehicle and projectile models unscaled (its renderers return 1.0); 3.0-dev had 0.7.
    public float getWidthScale() {
        return 1.0F;
    }

    @Override
    public float getHeightScale() {
        return 1.0F;
    }

}
