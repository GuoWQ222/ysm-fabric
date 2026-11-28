/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.mixin.fabric.client;

//#if MC >= 1.21.11
import net.minecraft.client.renderer.rendertype.RenderSetup;
//#endif
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** RenderType.create is package-private from 1.21.11 (before, the public factory is called directly). */
@Mixin(RenderType.class)
public interface RenderTypeInvoker {
    //#if MC >= 1.21.11
    @Invoker("create")
    static RenderType ysm$create(String name, RenderSetup setup) {
        throw new AssertionError("mixin");
    }
    //#endif
}
