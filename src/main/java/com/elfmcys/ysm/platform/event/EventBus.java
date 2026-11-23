/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.platform.event;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Fabric port: a small synchronous event bus. A listener registered for a type also receives its subtypes.
 * Handlers run in priority order; canceled events skip handlers that do not opt into receiveCanceled.
 *
 * <p>
 * Forge kept mod-lifecycle events (IModBusEvent) and game events on separate buses. A class annotated
 * {@code @Mod.EventBusSubscriber(bus = MOD)} receives only mod-bus events and one annotated for the FORGE bus only game
 * events, as on Forge (otherwise e.g. key mappings would be registered twice); other listeners receive both.
 */
public final class EventBus implements IEventBus {
    private record Listener(Object owner, Class<?> type, EventPriority priority, boolean receiveCanceled,
        Boolean modBus, Consumer<Event> call) {}

    private final List<Listener> listeners = new CopyOnWriteArrayList<>();
    private final Map<Class<?>, List<Listener>> resolved = new ConcurrentHashMap<>();
    private final String name;

    public EventBus(String name) {
        this.name = name;
    }

    @Override
    public void register(Object target) {
        boolean isClass = target instanceof Class<?>;
        Class<?> type = isClass ? (Class<?>)target : target.getClass();
        var lookup = MethodHandles.lookup();
        var subscriber = type.getAnnotation(Mod.EventBusSubscriber.class);
        Boolean modBus = subscriber == null ? null : subscriber.bus() == Mod.EventBusSubscriber.Bus.MOD;
        boolean any = false;
        for (Method method : type.getDeclaredMethods()) {
            var annotation = method.getAnnotation(SubscribeEvent.class);
            if (annotation == null)
                continue;
            if (Modifier.isStatic(method.getModifiers()) != isClass)
                continue;
            if (method.getParameterCount() != 1 || !Event.class.isAssignableFrom(method.getParameterTypes()[0]))
                throw new IllegalArgumentException(name + ": bad @SubscribeEvent method " + method);
            try {
                method.setAccessible(true);
                MethodHandle handle = lookup.unreflect(method);
                if (!isClass)
                    handle = handle.bindTo(target);
                final MethodHandle bound = handle;
                Consumer<Event> call = event -> {
                    try {
                        bound.invoke(event);
                    } catch (RuntimeException | Error failure) {
                        throw failure;
                    } catch (Throwable failure) {
                        throw new RuntimeException(failure);
                    }
                };
                listeners.add(new Listener(target, method.getParameterTypes()[0], annotation.priority(),
                    annotation.receiveCanceled(), modBus, call));
                any = true;
            } catch (IllegalAccessException failure) {
                throw new IllegalStateException(name + ": cannot access " + method, failure);
            }
        }
        if (any)
            resolved.clear();
    }

    @Override
    public void unregister(Object target) {
        if (listeners.removeIf(listener -> listener.owner() == target))
            resolved.clear();
    }

    @Override
    public <T extends Event> void addListener(Class<T> type, Consumer<T> listener) {
        addListener(EventPriority.NORMAL, type, listener);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Event> void addListener(EventPriority priority, Class<T> type, Consumer<T> listener) {
        listeners.add(new Listener(listener, type, priority, false, null, event -> listener.accept((T)event)));
        resolved.clear();
    }

    @Override
    public boolean post(Event event) {
        var targets = resolved.computeIfAbsent(event.getClass(), type -> {
            var list = new ArrayList<Listener>();
            boolean modEvent = com.elfmcys.ysm.platform.event.forge.IModBusEvent.class.isAssignableFrom(type);
            for (var listener : listeners)
                if (listener.type().isAssignableFrom(type)
                    && (listener.modBus() == null || listener.modBus() == modEvent))
                    list.add(listener);
            list.sort(Comparator.comparingInt(listener -> listener.priority().ordinal()));
            return List.copyOf(list);
        });
        for (var listener : targets) {
            if (event.isCanceled() && !listener.receiveCanceled())
                continue;
            listener.call().accept(event);
        }
        return event.isCanceled();
    }
}
