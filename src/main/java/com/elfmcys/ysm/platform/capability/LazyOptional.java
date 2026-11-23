/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.platform.capability;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Fabric port: Forge's lazily resolved, invalidatable optional. The supplier runs at most once; after
 * {@link #invalidate()} the optional behaves as empty.
 */
public final class LazyOptional<T> {
    private static final LazyOptional<Void> EMPTY = new LazyOptional<>(null);

    private Supplier<? extends T> supplier;
    private volatile T resolved;
    private volatile boolean isValid = true;

    private LazyOptional(Supplier<? extends T> supplier) {
        this.supplier = supplier;
    }

    public static <T> LazyOptional<T> of(Supplier<? extends T> supplier) {
        return supplier == null ? empty() : new LazyOptional<>(supplier);
    }

    @SuppressWarnings("unchecked")
    public static <T> LazyOptional<T> empty() {
        return (LazyOptional<T>)EMPTY;
    }

    @SuppressWarnings("unchecked")
    public <X> LazyOptional<X> cast() {
        return (LazyOptional<X>)this;
    }

    private T getValue() {
        if (!isValid || supplier == null)
            return null;
        if (resolved == null) {
            synchronized (this) {
                if (resolved == null && supplier != null)
                    resolved = Objects.requireNonNull(supplier.get(), "LazyOptional supplier returned null");
            }
        }
        return resolved;
    }

    public boolean isPresent() {
        return supplier != null && isValid;
    }

    public void ifPresent(Consumer<? super T> consumer) {
        T value = getValue();
        if (value != null)
            consumer.accept(value);
    }

    public <U> LazyOptional<U> lazyMap(Function<? super T, ? extends U> mapper) {
        return isPresent() ? of(() -> mapper.apply(getValue())) : empty();
    }

    public <U> Optional<U> map(Function<? super T, ? extends U> mapper) {
        return isPresent() ? Optional.ofNullable(mapper.apply(getValue())) : Optional.empty();
    }

    public Optional<T> filter(Predicate<? super T> predicate) {
        T value = getValue();
        return value != null && predicate.test(value) ? Optional.of(value) : Optional.empty();
    }

    public Optional<T> resolve() {
        return Optional.ofNullable(getValue());
    }

    public T orElse(T other) {
        T value = getValue();
        return value != null ? value : other;
    }

    public T orElseGet(Supplier<? extends T> other) {
        T value = getValue();
        return value != null ? value : other.get();
    }

    public <X extends Throwable> T orElseThrow(Supplier<? extends X> exception) throws X {
        T value = getValue();
        if (value != null)
            return value;
        throw exception.get();
    }

    public T orElseThrow() {
        T value = getValue();
        if (value != null)
            return value;
        throw new IllegalStateException("LazyOptional is empty or invalidated");
    }

    public void invalidate() {
        if (this != EMPTY)
            isValid = false;
    }
}
