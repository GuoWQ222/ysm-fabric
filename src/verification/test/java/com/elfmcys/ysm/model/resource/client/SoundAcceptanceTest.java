/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.model.resource.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/** AcceptSoundFX levels (2.6.5 parity): 0 all, 1 shorter than 4 s and smaller than 40 KB, 2 none. */
class SoundAcceptanceTest {
    private static final long RATE = 48_000;

    @AfterEach
    void reset() {
        SoundAcceptance.reset();
    }

    @Test
    void levelZeroAcceptsEverything() {
        assertTrue(SoundAcceptance.accepts(0, RATE * 600, RATE, 50L * 1024 * 1024));
    }

    @Test
    void levelTwoRejectsEverything() {
        assertFalse(SoundAcceptance.accepts(2, 1, RATE, 1));
    }

    @Test
    void levelOneKeepsOnlyShortAndSmallSounds() {
        assertTrue(SoundAcceptance.accepts(1, RATE * 4 - 1, RATE, 40L * 1024 - 1));
        assertFalse(SoundAcceptance.accepts(1, RATE * 4, RATE, 1024), "4 s is not shorter than 4 s");
        assertFalse(SoundAcceptance.accepts(1, RATE, RATE, 40L * 1024), "40 KB is not smaller than 40 KB");
    }

    @Test
    void levelIsClampedAndResets() {
        SoundAcceptance.setLevel(7);
        assertEquals(2, SoundAcceptance.level());
        SoundAcceptance.setLevel(-3);
        assertEquals(0, SoundAcceptance.level());
        SoundAcceptance.setLevel(1);
        SoundAcceptance.reset();
        assertEquals(0, SoundAcceptance.level());
    }
}
