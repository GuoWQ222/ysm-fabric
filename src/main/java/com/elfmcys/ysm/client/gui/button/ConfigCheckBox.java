// Modified for the unofficial Fabric 26.2 port by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.gui.button;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.ForgeConfigSpec;

/** A config boolean as a vanilla checkbox. Fabric port: 26.x checkboxes are built (constructor is private). */
public final class ConfigCheckBox {
    private ConfigCheckBox() {}

    public static Checkbox create(int x, int y, String key, ForgeConfigSpec.BooleanValue configSpec) {
        return Checkbox.builder(Component.translatable("gui.yes_steve_model.config." + key), Minecraft.getInstance().font)
            .pos(x, y).maxWidth(400).selected(configSpec.get())
            .onValueChange((checkbox, selected) -> configSpec.set(selected)).build();
    }
}
