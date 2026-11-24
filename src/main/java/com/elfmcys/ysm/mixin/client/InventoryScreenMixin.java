// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.mixin.client;

import com.elfmcys.ysm.util.RenderUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public class InventoryScreenMixin {
    @Inject(at = @At("HEAD"),
        method = "renderEntityInInventoryFollowsAngle(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIIFFLnet/minecraft/world/entity/LivingEntity;)V",
        remap = false)
    private static void beforeRenderEntityInInventoryFollowsAngle(GuiGraphicsExtractor pGuiGraphics, int pX, int pY, int pScale,
        float angleXComponent, float angleYComponent, LivingEntity pEntity, CallbackInfo ci) {
        RenderUtil.setRenderingInInventory(true);
    }

    @Inject(at = @At("RETURN"),
        method = "renderEntityInInventoryFollowsAngle(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIIFFLnet/minecraft/world/entity/LivingEntity;)V",
        remap = false)
    private static void afterRenderEntityInInventoryFollowsAngle(GuiGraphicsExtractor pGuiGraphics, int pX, int pY, int pScale,
        float angleXComponent, float angleYComponent, LivingEntity pEntity, CallbackInfo ci) {
        RenderUtil.setRenderingInInventory(false);
    }
}
