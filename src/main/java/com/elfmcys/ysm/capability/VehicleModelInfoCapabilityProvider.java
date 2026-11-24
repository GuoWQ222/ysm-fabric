// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
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

public class VehicleModelInfoCapabilityProvider implements ICapabilitySerializable<CompoundTag> {
    public static Capability<VehicleModelInfoCapability> CAP = CapabilityManager.get(new CapabilityToken<>() {});
    private VehicleModelInfoCapability instance = null;

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return CAP.orEmpty(cap, LazyOptional.of(this::createCapability));
    }

    @NotNull
    private VehicleModelInfoCapability createCapability() {
        if (instance == null) {
            this.instance = new VehicleModelInfoCapability();
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
