// Modified for the unofficial Fabric 26.2 port by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.buffer;

import com.elfmcys.ysm.util.UnsafeUtil;
import java.nio.ByteBuffer;

final class NativeNioBuffer implements NativeStorage {
    private final ByteBuffer owner;
    private final ByteBuffer data;

    NativeNioBuffer(ByteBuffer source) {
        if (!source.isDirect())
            throw new IllegalArgumentException("Buffer is not direct");
        // invokeCleaner requires the allocation base, not a duplicate or slice.
        owner = source;
        data = source.slice().order(source.order());
    }

    @Override
    public long ptr() {
        return com.elfmcys.ysm.buffer.Memory.address(data);
    }

    @Override
    public int size() {
        return data.capacity();
    }

    @Override
    public ByteBuffer nio() {
        return data.duplicate().order(data.order());
    }

    @Override
    public void close() {
        UnsafeUtil.getUnsafe().invokeCleaner(owner);
    }
}
