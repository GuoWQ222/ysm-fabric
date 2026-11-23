/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.model.resource.client;

import com.elfmcys.ysm.format.schema.model.views.SoundMedia;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Fabric port (2.6.5 AcceptSoundFX): which model sounds the client fetches and plays, as set by the server.
 * Rejected sounds are never requested, so they cost neither bandwidth nor client memory; playback of a missing sound
 * is a no-op. The level is per connection and resets to "all sounds" on disconnect.
 */
public final class SoundAcceptance {
    /** 2.6.5's limits for "short sounds only". */
    static final double SHORT_SOUND_SECONDS = 4.0;
    static final long SHORT_SOUND_BYTES = 40L * 1024L;

    private static volatile int level;

    private SoundAcceptance() {}

    public static void setLevel(int value) {
        level = Math.max(0, Math.min(2, value));
    }

    public static void reset() {
        level = 0;
    }

    public static int level() {
        return level;
    }

    public static boolean accepts(SoundMedia media) {
        return accepts(level, media);
    }

    static boolean accepts(int level, SoundMedia media) {
        if (level <= 0)
            return true;
        if (level >= 2)
            return false;
        long bytes = media.firstPage().remaining();
        for (var locator : media.locators())
            bytes += locator.storedSize();
        return accepts(level, media.frames(), media.sampleRate(), bytes);
    }

    /** The policy for one sound of the given length (frames at sampleRate) and stored size. */
    static boolean accepts(int level, long frames, long sampleRate, long bytes) {
        if (level <= 0)
            return true;
        if (level >= 2)
            return false;
        double seconds = frames / (double)sampleRate;
        return seconds < SHORT_SOUND_SECONDS && bytes < SHORT_SOUND_BYTES;
    }

    /** The accepted subset, in the original order; the same map when everything is accepted. */
    public static Map<String, SoundMedia> filter(Map<String, SoundMedia> sounds) {
        int current = level;
        if (current <= 0)
            return sounds;
        var accepted = new LinkedHashMap<String, SoundMedia>();
        for (var entry : sounds.entrySet()) {
            if (accepts(current, entry.getValue()))
                accepted.put(entry.getKey(), entry.getValue());
        }
        return accepted;
    }
}
