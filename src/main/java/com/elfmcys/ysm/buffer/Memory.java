/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.buffer;

//#if MC >= 26.1
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
//#endif
import java.nio.Buffer;
import java.util.Objects;

/**
 * Raw memory access for YSM's native buffers.
 *
 * <p>
 * Fabric port (Java 25): replaces netty's PlatformDependent memory helpers, which netty 4.2 disables on Java 24+
 * (its sun.misc.Unsafe path is off unless the JVM is started with --sun-misc-unsafe-memory-access=allow). Uses the
 * standard java.lang.foreign API; the caller guarantees the addresses are valid for the given lengths, exactly as with
 * the netty helpers. Minecraft before 26.1 targets Java 17/21, where java.lang.foreign is not final yet: there the
 * helpers use sun.misc.Unsafe directly (as netty did), which works on every Java from 17 on, including players who run
 * an older Minecraft on Java 24+ (the JDK then prints its one-time Unsafe notice).
 */
public final class Memory {
    //#if MC >= 26.1
    /** The whole address space; bounds are the caller's responsibility (as with Unsafe). */
    private static final MemorySegment ALL = MemorySegment.NULL.reinterpret(Long.MAX_VALUE);
    //#else
    //$$ private static final sun.misc.Unsafe UNSAFE;
    //$$ /** Offset of java.nio.Buffer.address (direct buffers keep their base address there). */
    //$$ private static final long ADDRESS_OFFSET;
    //$$ /** Copies touching the Java heap go in steps, so the JVM can reach a safepoint in between (as netty does). */
    //$$ private static final long HEAP_COPY_STEP = 1024 * 1024;
    //$$
    //$$ static {
    //$$     try {
    //$$         var field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
    //$$         field.setAccessible(true);
    //$$         UNSAFE = (sun.misc.Unsafe)field.get(null);
    //$$         ADDRESS_OFFSET = UNSAFE.objectFieldOffset(Buffer.class.getDeclaredField("address"));
    //$$     } catch (ReflectiveOperationException failure) {
    //$$         throw new ExceptionInInitializerError(failure);
    //$$     }
    //$$ }
    //#endif

    private Memory() {}

    /** Base address of a direct buffer (independent of its position and limit). */
    public static long address(Buffer direct) {
        Objects.requireNonNull(direct, "direct");
        if (!direct.isDirect())
            throw new IllegalArgumentException("Not a direct buffer");
        //#if MC >= 26.1
        var whole = direct.duplicate().clear();
        return MemorySegment.ofBuffer(whole).address();
        //#else
        //$$ return UNSAFE.getLong(direct, ADDRESS_OFFSET);
        //#endif
    }

    public static void copy(long source, long target, long length) {
        if (length > 0)
            //#if MC >= 26.1
            MemorySegment.copy(ALL, source, ALL, target, length);
            //#else
            //$$ UNSAFE.copyMemory(source, target, length);
            //#endif
    }

    public static void copy(byte[] source, int sourceIndex, long target, long length) {
        if (length <= 0)
            return;
        Objects.checkFromIndexSize(sourceIndex, length, source.length);
        //#if MC >= 26.1
        MemorySegment.copy(MemorySegment.ofArray(source), sourceIndex, ALL, target, length);
        //#else
        //$$ long offset = sun.misc.Unsafe.ARRAY_BYTE_BASE_OFFSET + sourceIndex;
        //$$ while (length > 0) {
        //$$     long step = Math.min(length, HEAP_COPY_STEP);
        //$$     UNSAFE.copyMemory(source, offset, null, target, step);
        //$$     offset += step;
        //$$     target += step;
        //$$     length -= step;
        //$$ }
        //#endif
    }

    public static void copy(long source, byte[] target, int targetIndex, long length) {
        if (length <= 0)
            return;
        Objects.checkFromIndexSize(targetIndex, length, target.length);
        //#if MC >= 26.1
        MemorySegment.copy(ALL, source, MemorySegment.ofArray(target), targetIndex, length);
        //#else
        //$$ long offset = sun.misc.Unsafe.ARRAY_BYTE_BASE_OFFSET + targetIndex;
        //$$ while (length > 0) {
        //$$     long step = Math.min(length, HEAP_COPY_STEP);
        //$$     UNSAFE.copyMemory(null, source, target, offset, step);
        //$$     source += step;
        //$$     offset += step;
        //$$     length -= step;
        //$$ }
        //#endif
    }

    public static byte getByte(long address) {
        //#if MC >= 26.1
        return ALL.get(ValueLayout.JAVA_BYTE, address);
        //#else
        //$$ return UNSAFE.getByte(address);
        //#endif
    }
}
