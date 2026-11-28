/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.legacy.network;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Minecraft before 1.20.5: the part of 1.20.5's StreamCodec YSM's payloads use (mapped from
 * net.minecraft.network.codec.StreamCodec by versions/below-1.20.5.remap).
 */
public interface StreamCodec<B, V> {
    V decode(B buffer);

    void encode(B buffer, V value);

    static <B, V> StreamCodec<B, V> of(BiConsumer<B, V> encoder, Function<B, V> decoder) {
        return new StreamCodec<>() {
            @Override
            public V decode(B buffer) {
                return decoder.apply(buffer);
            }

            @Override
            public void encode(B buffer, V value) {
                encoder.accept(buffer, value);
            }
        };
    }

    default <O> StreamCodec<B, O> map(Function<? super V, ? extends O> to, Function<? super O, ? extends V> from) {
        var self = this;
        return of((buffer, value) -> self.encode(buffer, from.apply(value)), buffer -> to.apply(self.decode(buffer)));
    }

    @SuppressWarnings("unchecked")
    default <S extends B> StreamCodec<S, V> cast() {
        return (StreamCodec<S, V>)this;
    }
}
