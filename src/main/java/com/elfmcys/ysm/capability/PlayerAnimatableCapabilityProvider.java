// Modified for the unofficial Fabric 26.2 port by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.capability;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.Direction;
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
public class PlayerAnimatableCapabilityProvider implements ICapabilityProvider {
    public static Capability<PlayerAnimatableCapability> CAP = CapabilityManager.get(new CapabilityToken<>() {});
    private PlayerAnimatableCapability instance;
    private AbstractClientPlayer player;

    public PlayerAnimatableCapabilityProvider(AbstractClientPlayer player) {
        this.player = player;
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return getCapability(cap);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap) {
        return CAP.orEmpty(cap, LazyOptional.of(this::createCapability));
    }

    @NotNull
    private PlayerAnimatableCapability createCapability() {
        if (instance == null) {
            this.instance = new PlayerAnimatableCapability(player);
            player = null;
        }
        return instance;
    }
}
