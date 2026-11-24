// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.renderer.layer;

import com.elfmcys.ysm.client.compat.simplehat.SimpleHatsCompat;
import com.elfmcys.ysm.client.entity.CustomPlayerEntity;
import com.elfmcys.ysm.geckolib3.geo.GeoLayerRenderer;
import com.elfmcys.ysm.geckolib3.geo.GeoRenderData;
import com.elfmcys.ysm.client.model.locator.PlayerLocator;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Non-armour head items (skulls, pumpkins, blocks) at the head locator. */
public class CustomPlayerHeadLayer extends GeoLayerRenderer<CustomPlayerEntity> {
    private final ItemModelResolver itemModelResolver;

    public CustomPlayerHeadLayer(ItemModelResolver itemModelResolver) {
        this.itemModelResolver = itemModelResolver;
    }

    @Override
    public void render(PoseStack poseStack, SubmitNodeCollector collector, CustomPlayerEntity animatable,
        GeoRenderData renderData, LivingEntityRenderState state, int packedLight, int overlay) {
        var player = animatable.getEntity();
        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        if (!head.isEmpty() && !isArmorHead(head))
            renderHeadItem(poseStack, collector, packedLight, renderData, state, player, head);
        ItemStack curiosHead = SimpleHatsCompat.getCuriosHead(player);
        if (curiosHead != null && !curiosHead.isEmpty())
            renderHeadItem(poseStack, collector, packedLight, renderData, state, player, curiosHead);
    }

    /** Fabric port: 26.x armour is an equippable head item with an equipment asset (was ArmorItem). */
    private static boolean isArmorHead(ItemStack itemStack) {
        var equippable = itemStack.get(DataComponents.EQUIPPABLE);
        return equippable != null && equippable.slot() == EquipmentSlot.HEAD && equippable.assetId().isPresent();
    }

    private void renderHeadItem(PoseStack poseStack, SubmitNodeCollector collector, int packedLight,
        GeoRenderData data, LivingEntityRenderState state, Player player, ItemStack head) {
        var itemState = new ItemStackRenderState();
        itemModelResolver.updateForLiving(itemState, head, ItemDisplayContext.HEAD, player);
        if (itemState.isEmpty())
            return;
        data.modelState.visitLocatorGroup(PlayerLocator.get().head, poseStack, locatorPose -> {
            locatorPose.scale(0.625F, 0.625F, 0.625F);
            locatorPose.translate(0.0F, 0.25F, 0.0F);
            itemState.submit(locatorPose, collector, packedLight, OverlayTexture.NO_OVERLAY, state.outlineColor);
        });
    }
}
