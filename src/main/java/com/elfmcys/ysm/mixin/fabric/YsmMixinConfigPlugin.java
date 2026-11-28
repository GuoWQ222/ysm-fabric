/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.mixin.fabric;

import java.util.List;
import java.util.Set;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

/**
 * Fabric port, multi-version diagnostics: -Dysm.mixin.skip=NameA+NameB (simple class names, set by the build's
 * -Pmixin_skip) leaves those mixins out, so a rendering fault on another Minecraft version can be bisected. Without the
 * property every mixin applies.
 */
public final class YsmMixinConfigPlugin implements IMixinConfigPlugin {
    private static final Set<String> SKIP = Set.of(System.getProperty("ysm.mixin.skip", "").split("\\+"));

    @Override
    public void onLoad(String mixinPackage) {
        //#if LOADER == forge && MC < 1.21.10
        //$$ // Forge before 1.21.10 ships no MixinExtras: the bundled copy (jar-in-jar) is started here
        //$$ com.llamalad7.mixinextras.MixinExtrasBootstrap.init();
        //#endif
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        var simple = mixinClassName.substring(mixinClassName.lastIndexOf('.') + 1);
        return !SKIP.contains(simple);
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
