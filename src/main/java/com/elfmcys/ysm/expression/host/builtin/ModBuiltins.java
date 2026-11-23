// Modified for the unofficial Fabric 26.2 port by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.expression.host.builtin;

import com.elfmcys.ysm.api.molang.MolangContextView;
import com.elfmcys.ysm.client.entity.AnimatableEntity;
import com.elfmcys.ysm.expression.annotation.BuiltinComponent;
import com.elfmcys.ysm.expression.annotation.BuiltinFunction;
import com.elfmcys.ysm.expression.host.EntityComponent;
import net.minecraft.world.entity.Entity;

@BuiltinComponent("ysm")
public final class ModBuiltins implements EntityComponent<Entity> {
    public ModBuiltins(AnimatableEntity<Entity> owner) {}

    @BuiltinFunction(value = "mod_version", alwaysOrigin = true)
    public String modVersion(MolangContextView<Entity> context, String modId) {
        return net.fabricmc.loader.api.FabricLoader.getInstance().getModContainer(modId)
            .map(container -> container.getMetadata().getVersion().getFriendlyString()).orElse("");
    }
}
