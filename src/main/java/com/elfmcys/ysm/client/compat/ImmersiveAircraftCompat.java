// Modified for the unofficial Fabric 26.2 port by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.compat;

import com.elfmcys.ysm.client.entity.CustomVehicleEntity;
import com.elfmcys.ysm.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.ysm.geckolib3.core.util.MathUtil;
import com.mojang.math.Axis;
import org.joml.Vector3f;
import java.util.Optional;

/**
 * Fabric port stand-in: the ImmersiveAircraftCompat integration targets a Forge mod and is not ported; every method reports
 * "not installed". The original is kept in src/forge-compat-reference.
 */
@SuppressWarnings("unused")
public final class ImmersiveAircraftCompat {

    private ImmersiveAircraftCompat() {}

    public static void init() {
        
    }

    public static boolean isInstalled() {
        return false;
    }

    public static Optional<Vector3f> getRotation(AnimationEvent<CustomVehicleEntity> event) {
        return Optional.empty();
    }
}
