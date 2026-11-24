// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.capability;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.projectile.Projectile;
import com.elfmcys.ysm.platform.Dist;
import com.elfmcys.ysm.platform.OnlyIn;
import com.elfmcys.ysm.platform.capability.Capability;
import com.elfmcys.ysm.platform.capability.CapabilityManager;
import com.elfmcys.ysm.platform.capability.CapabilityToken;
import com.elfmcys.ysm.platform.capability.ICapabilityProvider;
import com.elfmcys.ysm.platform.capability.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public class ProjectileAnimatableCapabilityProvider implements ICapabilityProvider {
    public static Capability<ProjectileAnimatableCapability> CAP = CapabilityManager.get(new CapabilityToken<>() {});
    private ProjectileAnimatableCapability instance;
    private Projectile projectile;

    public ProjectileAnimatableCapabilityProvider(Projectile projectile) {
        this.projectile = projectile;
    }

    public ProjectileAnimatableCapability initialize() {
        if (this.instance == null) {
            this.instance = new ProjectileAnimatableCapability(projectile);
            this.projectile = null;
        }
        return this.instance;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return CAP.orEmpty(cap, instance == null ? LazyOptional.empty() : LazyOptional.of(() -> instance));
    }
}
