// Modified for the unofficial Fabric 26.2 port by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.compat;

import com.elfmcys.ysm.client.entity.CustomVehicleEntity;
import com.elfmcys.ysm.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.ysm.geckolib3.core.util.MathUtil;
import com.mojang.math.Axis;
import net.fabricmc.loader.api.FabricLoader;
import org.joml.Vector3f;

import java.util.Optional;

/**
 * Immersive Aircraft (Fabric 26.2, mod id {@code immersive_aircraft}). As upstream's Forge integration: a YSM vehicle
 * model on an aircraft follows its pitch, roll and wind sway. API calls live in {@link Inner}.
 */
public final class ImmersiveAircraftCompat {
    private static final String MOD_ID = "immersive_aircraft";
    private static boolean installed;

    private ImmersiveAircraftCompat() {}

    public static void init() {
        installed = FabricLoader.getInstance().isModLoaded(MOD_ID);
    }

    public static boolean isInstalled() {
        return installed;
    }

    public static Optional<Vector3f> getRotation(AnimationEvent<CustomVehicleEntity> event) {
        return installed ? Inner.getRotation(event) : Optional.empty();
    }

    private static final class Inner {
        static Optional<Vector3f> getRotation(AnimationEvent<CustomVehicleEntity> event) {
            if (!(event.getAnimatableEntity().getEntity() instanceof immersive_aircraft.entity.AircraftEntity plane))
                return Optional.empty();
            Vector3f effect = plane.onGround() ? new Vector3f() : plane.getWindEffect();
            var rotation = new Vector3f();
            MathUtil.getEulerAnglesZYX(Axis.XP.rotationDegrees(effect.z).rotateZ(MathUtil.degreesToRadians(effect.x))
                .rotateX(-MathUtil.degreesToRadians(plane.getViewXRot(event.getPartialTick())))
                .rotateZ(-MathUtil.degreesToRadians(plane.getRoll(event.getPartialTick()))), rotation);
            return Optional.of(rotation);
        }
    }
}
