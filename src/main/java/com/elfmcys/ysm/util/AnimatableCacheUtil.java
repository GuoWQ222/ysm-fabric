// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import com.elfmcys.ysm.platform.Dist;
import com.elfmcys.ysm.platform.OnlyIn;

import java.util.concurrent.TimeUnit;

@OnlyIn(Dist.CLIENT)
public final class AnimatableCacheUtil {
    public static final Cache<Identifier, Entity> ENTITIES_CACHE =
        CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).build();
}
