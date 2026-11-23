/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.platform.event;

import com.elfmcys.ysm.platform.Dist;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Fabric port: keeps Forge's {@code @Mod.EventBusSubscriber} marker. Fabric has no annotation scan, so
 * {@link Subscribers} lists every annotated class explicitly and registers it on {@link MinecraftForge#EVENT_BUS}.
 */
public final class Mod {
    private Mod() {}

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface EventBusSubscriber {
        Dist[] value() default {Dist.CLIENT, Dist.DEDICATED_SERVER};

        String modid() default "";

        Bus bus() default Bus.FORGE;

        enum Bus {
            FORGE, MOD
        }
    }
}
