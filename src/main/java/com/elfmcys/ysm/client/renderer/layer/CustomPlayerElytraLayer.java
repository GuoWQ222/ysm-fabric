// Modified for the unofficial Fabric 26.2 port by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.renderer.layer;

import com.elfmcys.ysm.client.entity.CustomPlayerEntity;
import com.elfmcys.ysm.geckolib3.geo.GeoLayerRenderer;
import com.elfmcys.ysm.geckolib3.geo.GeoRenderData;
import com.elfmcys.ysm.client.model.locator.PlayerLocator;
import com.elfmcys.ysm.util.EquipmentUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.equipment.ElytraModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import org.jetbrains.annotations.Nullable;

/** Elytra (and other wing equipment) at the elytra locator (Fabric port: 26.x equipment layers). */
public class CustomPlayerElytraLayer extends GeoLayerRenderer<CustomPlayerEntity> {
    private final ElytraModel elytraModel;
    private final ElytraModel babyElytraModel;
    private final EquipmentLayerRenderer equipmentRenderer;

    public CustomPlayerElytraLayer(EntityModelSet modelSet, EquipmentLayerRenderer equipmentRenderer) {
        this.elytraModel = new ElytraModel(modelSet.bakeLayer(ModelLayers.ELYTRA));
        this.babyElytraModel = new ElytraModel(modelSet.bakeLayer(ModelLayers.ELYTRA_BABY));
        this.equipmentRenderer = equipmentRenderer;
    }

    @Override
    public void render(PoseStack poseStack, SubmitNodeCollector collector, CustomPlayerEntity animatable,
        GeoRenderData renderData, LivingEntityRenderState state, int packedLight, int overlay) {
        if (!(state instanceof HumanoidRenderState humanoid))
            return;
        var player = animatable.getEntity();
        var stack = EquipmentUtil.getEquippedElytraItem(player);
        if (stack.isEmpty())
            stack = player.getItemBySlot(EquipmentSlot.CHEST);
        var equippable = stack.get(DataComponents.EQUIPPABLE);
        if (equippable == null || equippable.assetId().isEmpty())
            return;
        var wings = stack;
        var asset = equippable.assetId().get();
        var texture = playerElytraTexture(state);
        var model = humanoid.isBaby ? babyElytraModel : elytraModel;
        renderData.modelState.visitLocatorGroup(PlayerLocator.get().elytra, poseStack, locatorPose -> {
            // 2.6.5 (26.x) draws the 26.x elytra model at the locator, only flipped; the 1.20.1 offset and 2x scale
            // made the wings twice as large on 26.x.
            locatorPose.mulPose(Axis.ZP.rotationDegrees(180));
            equipmentRenderer.renderLayers(EquipmentClientInfo.LayerType.WINGS, asset, model, humanoid, wings,
                locatorPose, collector, packedLight, texture, state.outlineColor, 0);
        });
    }

    private static @Nullable Identifier playerElytraTexture(LivingEntityRenderState state) {
        if (state instanceof AvatarRenderState avatar) {
            var skin = avatar.skin;
            if (skin.elytra() != null)
                return skin.elytra().texturePath();
            if (skin.cape() != null && avatar.showCape)
                return skin.cape().texturePath();
        }
        return null;
    }
}
