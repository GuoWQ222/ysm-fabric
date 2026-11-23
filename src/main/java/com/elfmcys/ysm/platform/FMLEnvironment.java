package com.elfmcys.ysm.platform;

/** Fabric port: Forge's FMLEnvironment fields. */
public final class FMLEnvironment {
    public static final Dist dist = Platform.dist();
    public static final boolean production = Platform.isProduction();

    private FMLEnvironment() {}
}
