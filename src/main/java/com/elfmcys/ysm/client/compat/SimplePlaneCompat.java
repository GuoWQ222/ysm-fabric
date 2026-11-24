// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.compat;

import com.elfmcys.ysm.client.entity.CustomVehicleEntity;
import com.elfmcys.ysm.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.ysm.geckolib3.core.util.MathUtil;
import com.mojang.math.Axis;
import org.joml.Math;
import org.joml.Vector3f;
import java.util.Optional;

/**
 * Fabric port stand-in: the SimplePlaneCompat integration targets a Forge mod and is not ported; every method reports
 * "not installed". The original is kept in src/forge-compat-reference.
 */
@SuppressWarnings("unused")
public final class SimplePlaneCompat {

    private SimplePlaneCompat() {}

    public static void init() {
        
    }

    public static boolean isInstalled() {
        return false;
    }

    public static Optional<Vector3f> getRotation(AnimationEvent<CustomVehicleEntity> event) {
        return Optional.empty();
    }
}
