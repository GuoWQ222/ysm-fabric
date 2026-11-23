/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.platform;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * Fabric port: Forge's DistExecutor call shape. The double supplier keeps client-only code in a lambda body that is
 * never linked on the other physical side (ServerSideIsolationTest recognises this guard).
 */
public final class DistExecutor {
    private DistExecutor() {}

    public static <T> T unsafeCallWhenOn(Dist dist, Supplier<Callable<T>> toRun) {
        if (Platform.dist() != dist)
            return null;
        try {
            return toRun.get().call();
        } catch (RuntimeException failure) {
            throw failure;
        } catch (Exception failure) {
            throw new RuntimeException(failure);
        }
    }

    public static void unsafeRunWhenOn(Dist dist, Supplier<Runnable> toRun) {
        if (Platform.dist() == dist)
            toRun.get().run();
    }
}
