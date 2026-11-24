// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.compat.immersivemelodies;

import net.minecraft.world.entity.LivingEntity;

/**
 * Immersive Melodies (Fabric 26.2, mod id {@code immersive_melodies}). As upstream's Forge integration: while an
 * entity plays an instrument, its melody progress (pitch, volume, level, timing) feeds the {@code ctrl:im_*} Molang
 * functions. Immersive Melodies classes are only touched by {@link Inner}.
 */
public final class ImmersiveMelodiesCompat {
    private static final String MOD_ID = "immersive_melodies";
    private static boolean loaded;

    private ImmersiveMelodiesCompat() {}

    public static void init() {
        loaded = com.elfmcys.ysm.platform.Platform.isModLoaded(MOD_ID);
    }

    public static boolean isLoaded() {
        return loaded;
    }

    /** Melody progress sampled for the ctrl:im_* functions. */
    public static final class ImmersiveMelodiesData {
        /** Milliseconds since the last note. */
        public float delta;
        /** Pitch, usually 0..2. */
        public float pitch;
        /** Volume, usually 0..2. */
        public float volume;
        /** Level, 0..1. */
        public float current;
        /** Milliseconds since the melody started. */
        public float time;
    }

    public static void updateMelodyProgress(LivingEntity entity, ImmersiveMelodiesData data) {
        if (loaded)
            Inner.updateMelodyProgress(entity, data);
    }

    private static final class Inner {
        static void updateMelodyProgress(LivingEntity entity, ImmersiveMelodiesData data) {
            if (immersive_melodies.client.animation.EntityModelAnimator.getInstrument(entity) == null)
                return;
            var minecraft = net.minecraft.client.Minecraft.getInstance();
            float time = (minecraft.isPaused() ? 0.0F : minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false))
                + (float)entity.tickCount;
            var progress = immersive_melodies.client.MelodyProgressManager.INSTANCE.getProgress(entity);
            progress.visualTick(time);
            data.pitch = progress.getCurrentPitch();
            data.volume = progress.getCurrentVolume();
            data.current = progress.getCurrent();
            data.delta = progress.delta();
            data.time = progress.getTime();
        }
    }
}
