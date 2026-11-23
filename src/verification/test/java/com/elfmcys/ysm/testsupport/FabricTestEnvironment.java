/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.testsupport;

import java.lang.reflect.Proxy;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.impl.launch.FabricLauncher;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import org.junit.platform.launcher.LauncherSession;
import org.junit.platform.launcher.LauncherSessionListener;

/**
 * Fabric port: unit tests run without the Fabric launcher, but Forge Config API Port asks it whether this is a
 * development environment. Installs a minimal production-mode launcher for the test JVM.
 */
public final class FabricTestEnvironment implements LauncherSessionListener {
    @Override
    public void launcherSessionOpened(LauncherSession session) {
        if (FabricLauncherBase.getLauncher() != null)
            return;
        var loader = FabricTestEnvironment.class.getClassLoader();
        var launcher = (FabricLauncher)Proxy.newProxyInstance(loader, new Class<?>[] {FabricLauncher.class},
            (proxy, method, args) -> switch (method.getName()) {
                case "isDevelopment" -> false;
                case "getEnvironmentType" -> EnvType.CLIENT;
                case "getTargetClassLoader" -> loader;
                case "getClassPath" -> List.of();
                case "isClassLoaded" -> false;
                case "getEntrypoint" -> "test";
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> proxy == args[0];
                case "toString" -> "FabricTestEnvironment launcher";
                default -> null;
            });
        FabricLauncherBase.setLauncher(launcher);
    }
}
