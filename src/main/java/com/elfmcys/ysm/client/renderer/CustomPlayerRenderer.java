// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.renderer;

import com.elfmcys.ysm.capability.PlayerAnimatableCapability;
import com.elfmcys.ysm.client.compat.swarfare.SWarfareCompat;
import com.elfmcys.ysm.client.entity.CustomPlayerEntity;
import com.elfmcys.ysm.client.gui.GuiModelFraming;
import com.elfmcys.ysm.client.renderer.layer.CustomParrotOnShoulderLayer;
import com.elfmcys.ysm.client.renderer.layer.CustomPlayerElytraLayer;
import com.elfmcys.ysm.client.renderer.layer.CustomPlayerHeadLayer;
import com.elfmcys.ysm.client.renderer.layer.CustomPlayerItemInHandLayer;
import com.elfmcys.ysm.event.api.SpecialPlayerRenderEvent;
import com.elfmcys.ysm.geckolib3.geo.GeoReplacedEntityRenderer;
import com.elfmcys.ysm.platform.event.MinecraftForge;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

/** Draws a player's YSM model in place of the vanilla body (Fabric port: submitted, see GeoReplacedEntityRenderer). */
public class CustomPlayerRenderer extends GeoReplacedEntityRenderer<Player, CustomPlayerEntity> {
    public CustomPlayerRenderer(EntityRendererProvider.Context ctx) {
        addLayer(new CustomPlayerItemInHandLayer(ctx.getItemModelResolver()));
        //#if MC >= 1.21.2
        addLayer(new CustomPlayerElytraLayer(ctx.getModelSet(), ctx.getEquipmentRenderer()));
        //#else
        //$$ addLayer(new CustomPlayerElytraLayer(ctx.getModelSet()));
        //#endif
        addLayer(new CustomParrotOnShoulderLayer(ctx.getModelSet()));
        addLayer(new CustomPlayerHeadLayer(ctx.getItemModelResolver()));
    }

    /**
     * World draw of a player. handoff runs once YSM owns the draw (or hides the player for a compat mod), so vanilla
     * then skips its body and layers.
     */
    public boolean renderReplacement(PlayerAnimatableCapability cap, LivingEntityRenderState state, float partialTick,
        PoseStack poseStack, SubmitNodeCollector collector, Runnable handoff) {
        return renderReplacement(cap, state, partialTick, poseStack, collector, null, handoff);
    }

    public boolean renderReplacement(PlayerAnimatableCapability cap, LivingEntityRenderState state, float partialTick,
        PoseStack poseStack, SubmitNodeCollector collector, @Nullable GuiModelFraming framing, Runnable handoff) {
        if (!cap.isPresentationAllowed())
            return false;
        if (SWarfareCompat.shouldHidePlayerRender(cap.getEntity())) {
            handoff.run();
            return true;
        }
        var data = cap.update(partialTick);
        return cap.consumePrepared(data, borrowed -> {
            var event = new SpecialPlayerRenderEvent(cap.getEntity(), cap, cap.getInstalledModelId());
            var suppressed = MinecraftForge.EVENT_BUS.post(event);
            if (!cap.canConsume(borrowed, borrowed.ctx))
                return false;
            handoff.run();
            if (suppressed)
                return true;
            submitPrepared(cap, borrowed, state, event.getTextureLocationOverride(), partialTick, poseStack, collector,
                framing);
            return true;
        });
    }
}
