// Modified for the unofficial Fabric 26.2 port by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.buffer;

import com.elfmcys.ysm.util.UnsafeUtil;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

final class MappedFileBuffer implements NativeStorage {
    private final MappedByteBuffer mapped;

    MappedFileBuffer(FileChannel file, long offset, long size) throws IOException {
        if (size < 0 || size > MAX_SIZE)
            throw new IllegalArgumentException("Invalid size");
        mapped = file.map(FileChannel.MapMode.READ_ONLY, offset, size);
    }

    @Override
    public long ptr() {
        return com.elfmcys.ysm.buffer.Memory.address(mapped);
    }

    @Override
    public int size() {
        return mapped.capacity();
    }

    @Override
    public ByteBuffer nio() {
        return mapped.asReadOnlyBuffer();
    }

    @Override
    public void close() {
        UnsafeUtil.getUnsafe().invokeCleaner(mapped);
    }
}
