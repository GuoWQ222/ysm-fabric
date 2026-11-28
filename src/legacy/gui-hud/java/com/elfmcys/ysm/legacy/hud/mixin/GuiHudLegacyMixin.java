/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.legacy.hud.mixin;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Forge for Minecraft 1.21 and 1.21.6 / 1.21.7 has no HUD layer event (AddGuiOverlayLayersEvent came with 1.21.1 and
 * again with 1.21.8): YSM's HUD
 * layers draw after the vanilla HUD, hidden with it (F1).
 */
@Mixin(Gui.class)
public abstract class GuiHudLegacyMixin {
    @Inject(method = "render", at = @At("TAIL"))
    //#if MC >= 1.21
    private void ysm$hudLayers(GuiGraphics graphics, DeltaTracker delta, CallbackInfo ci) {
    //#else
    //$$ // before 1.21 the HUD gets the partial tick
    //$$ private void ysm$hudLayers(GuiGraphics graphics, float partialTick, CallbackInfo ci) {
    //$$     var delta = DeltaTracker.of(partialTick);
    //#endif
        if (com.elfmcys.ysm.platform.McCompat.hudHidden())
            return;
        com.elfmcys.ysm.client.event.ClientSetupEvent.hudLayers().values().forEach(layer -> layer.render(graphics, delta));
    }
}
