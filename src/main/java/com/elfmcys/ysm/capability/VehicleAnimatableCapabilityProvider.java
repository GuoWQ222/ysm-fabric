// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.capability;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
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
public class VehicleAnimatableCapabilityProvider implements ICapabilityProvider {
    public static Capability<VehicleAnimatableCapability> CAP = CapabilityManager.get(new CapabilityToken<>() {});
    private VehicleAnimatableCapability instance;
    private Entity entity;

    public VehicleAnimatableCapabilityProvider(Entity entity) {
        this.entity = entity;
    }

    public VehicleAnimatableCapability initialize() {
        if (this.instance == null) {
            this.instance = new VehicleAnimatableCapability(entity);
            this.entity = null;
        }
        return instance;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return CAP.orEmpty(cap, instance == null ? LazyOptional.empty() : LazyOptional.of(() -> instance));
    }
}
