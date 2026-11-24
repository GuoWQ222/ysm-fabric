// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.ListTag;
import com.elfmcys.ysm.platform.capability.Capability;
import com.elfmcys.ysm.platform.capability.CapabilityManager;
import com.elfmcys.ysm.platform.capability.CapabilityToken;
import com.elfmcys.ysm.platform.capability.ICapabilitySerializable;
import com.elfmcys.ysm.platform.capability.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AuthModelsCapabilityProvider implements ICapabilitySerializable<ListTag> {
    public static Capability<AuthModelsCapability> AUTH_MODELS_CAP = CapabilityManager.get(new CapabilityToken<>() {});
    private AuthModelsCapability instance = null;

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return AUTH_MODELS_CAP.orEmpty(cap, LazyOptional.of(this::createCapability));
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap) {
        return getCapability(cap, null);
    }

    @NotNull
    private AuthModelsCapability createCapability() {
        if (instance == null) {
            this.instance = new AuthModelsCapability();
        }
        return instance;
    }

    @Override
    public void deserializeNBT(ListTag nbt) {
        createCapability().deserializeNBT(nbt);
    }

    @Override
    public ListTag serializeNBT() {
        return createCapability().serializeNBT();
    }
}
