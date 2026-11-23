// Modified for the unofficial Fabric 26.2 port by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.capability;

import net.minecraft.core.Direction;
import com.elfmcys.ysm.platform.capability.Capability;
import com.elfmcys.ysm.platform.capability.CapabilityManager;
import com.elfmcys.ysm.platform.capability.CapabilityToken;
import com.elfmcys.ysm.platform.capability.ICapabilityProvider;
import com.elfmcys.ysm.platform.capability.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClientLazyCapabilityProvider implements ICapabilityProvider {
    public static Capability<ClientLazyCapability> CAP = CapabilityManager.get(new CapabilityToken<>() {});
    private final ClientLazyCapability instance;

    public ClientLazyCapabilityProvider(VehicleAnimatableCapabilityProvider vehicleAnimatableCapabilityProvider,
        @Nullable ProjectileAnimatableCapabilityProvider projectileAnimatableCapabilityProvider) {
        this.instance =
            new ClientLazyCapability(vehicleAnimatableCapabilityProvider, projectileAnimatableCapabilityProvider);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return CAP.orEmpty(cap, LazyOptional.of(() -> instance));
    }
}
