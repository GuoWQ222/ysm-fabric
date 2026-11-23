/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.buffer;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.Buffer;
import java.util.Objects;

/**
 * Raw memory access for YSM's native buffers.
 *
 * <p>
 * Fabric port (Java 25): replaces netty's PlatformDependent memory helpers, which netty 4.2 disables on Java 24+
 * (its sun.misc.Unsafe path is off unless the JVM is started with --sun-misc-unsafe-memory-access=allow). Uses the
 * standard java.lang.foreign API; the caller guarantees the addresses are valid for the given lengths, exactly as with
 * the netty helpers.
 */
public final class Memory {
    /** The whole address space; bounds are the caller's responsibility (as with Unsafe). */
    private static final MemorySegment ALL = MemorySegment.NULL.reinterpret(Long.MAX_VALUE);

    private Memory() {}

    /** Base address of a direct buffer (independent of its position and limit). */
    public static long address(Buffer direct) {
        Objects.requireNonNull(direct, "direct");
        if (!direct.isDirect())
            throw new IllegalArgumentException("Not a direct buffer");
        var whole = direct.duplicate().clear();
        return MemorySegment.ofBuffer(whole).address();
    }

    public static void copy(long source, long target, long length) {
        if (length > 0)
            MemorySegment.copy(ALL, source, ALL, target, length);
    }

    public static void copy(byte[] source, int sourceIndex, long target, long length) {
        if (length > 0)
            MemorySegment.copy(MemorySegment.ofArray(source), sourceIndex, ALL, target, length);
    }

    public static void copy(long source, byte[] target, int targetIndex, long length) {
        if (length > 0)
            MemorySegment.copy(ALL, source, MemorySegment.ofArray(target), targetIndex, length);
    }

    public static byte getByte(long address) {
        return ALL.get(ValueLayout.JAVA_BYTE, address);
    }
}
