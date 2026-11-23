// Modified for the unofficial Fabric 26.2 port by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.compat.immersivemelodies;

import net.minecraft.world.entity.LivingEntity;

/**
 * Fabric port stand-in: the Immersive Melodies integration is not ported yet. The ctrl:im_* builtins stay declared
 * (ImmersiveMelodiesBuiltinComponent) and are admitted only when the mod is installed.
 */
public final class ImmersiveMelodiesCompat {
    private ImmersiveMelodiesCompat() {}

    public static void init() {}

    public static boolean isLoaded() {
        return false;
    }

    /** Melody progress sampled for the ctrl:im_* functions. */
    public static final class ImmersiveMelodiesData {
        public float delta;
        public float pitch;
        public float volume;
        public float current;
        public float time;
    }

    public static void updateMelodyProgress(LivingEntity entity, ImmersiveMelodiesData data) {
        data.delta = data.pitch = data.volume = data.current = data.time = 0;
    }
}
