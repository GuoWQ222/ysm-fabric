// Modified for the unofficial Fabric 26.2 port by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import com.elfmcys.ysm.platform.capability.Capability;
import com.elfmcys.ysm.platform.capability.CapabilityManager;
import com.elfmcys.ysm.platform.capability.CapabilityToken;
import com.elfmcys.ysm.platform.capability.ICapabilitySerializable;
import com.elfmcys.ysm.platform.capability.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ProjectileModelInfoCapabilityProvider implements ICapabilitySerializable<CompoundTag> {
    public static Capability<ProjectileModelInfoCapability> CAP = CapabilityManager.get(new CapabilityToken<>() {});
    private ProjectileModelInfoCapability instance = null;

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return CAP.orEmpty(cap, LazyOptional.of(this::createCapability));
    }

    @NotNull
    private ProjectileModelInfoCapability createCapability() {
        if (instance == null) {
            this.instance = new ProjectileModelInfoCapability();
        }
        return instance;
    }

    @Override
    public CompoundTag serializeNBT() {
        return createCapability().serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        createCapability().deserializeNBT(nbt);
    }
}
