// Modified for the unofficial Fabric 26.2 port by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.gui.button;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.client.entity.AnimatableEntity;
import com.elfmcys.ysm.client.gui.GuiDraw;
import com.elfmcys.ysm.info.roulette.forms.ConfigActionRoute;
import com.elfmcys.ysm.network.forge.ClientProtocolGateway;
import java.text.DecimalFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * Molang config slider (roulette forms). Fabric port: built on the vanilla slider (Forge's ForgeSlider is not
 * available) with the same range, step snapping, "prefix + value" label and YSM texture.
 */
public class FlatSlider extends AbstractSliderButton implements IConfigFormsButton {
    private static final Identifier BUTTON_TEXTURE =
        Identifier.fromNamespaceAndPath(YesSteveModel.MOD_ID, "texture/roulette.png");
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");
    private final AnimatableEntity<?> animatableEntity;
    private final long expressionGeneration;
    private final ConfigActionRoute route;
    private final Component prefix;
    private final double min;
    private final double max;
    private final double step;

    public FlatSlider(int x, int y, Component prefix, double currentValue, AnimatableEntity<?> animatableEntity,
        long expressionGeneration, ConfigActionRoute route, double step, double min, double max) {
        super(x, y, 115, 15, Component.empty(), 0);
        this.animatableEntity = animatableEntity;
        this.expressionGeneration = expressionGeneration;
        this.route = route;
        this.prefix = prefix;
        this.min = min;
        this.max = max;
        this.step = step;
        this.value = normalize(currentValue);
        updateMessage();
    }

    /** The slider value in [min, max], snapped to the step. */
    public double getValue() {
        return snap(min + value * (max - min));
    }

    private double snap(double raw) {
        double v = raw;
        if (step > 0)
            v = min + Math.round((v - min) / step) * step;
        return Mth.clamp(v, Math.min(min, max), Math.max(min, max));
    }

    private double normalize(double denormalized) {
        return max == min ? 0 : Mth.clamp((snap(denormalized) - min) / (max - min), 0, 1);
    }

    @Override
    protected void updateMessage() {
        setMessage(Component.empty().append(prefix).append(getValueString()));
    }

    @Override
    protected void applyValue() {
        value = normalize(min + value * (max - min));
        try {
            ClientProtocolGateway.submitConfigFloat(animatableEntity, expressionGeneration, route, (float)getValue());
        } catch (RuntimeException exception) {
            YesSteveModel.LOGGER.error(exception);
        }
    }

    public String getValueString() {
        return DECIMAL_FORMAT.format(getValue());
    }

    public void setDisplayedValue(double value) {
        // Projection refresh must not write the observed value back to Molang.
        this.value = normalize(value);
        updateMessage();
    }

    private int textureY() {
        return (isFocused() && !canChangeValue ? 1 : 0) * 20;
    }

    private int handleTextureY() {
        return (!isHovered() && !canChangeValue ? 2 : 3) * 20;
    }

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        var font = Minecraft.getInstance().font;
        GuiDraw.blitWithBorder(graphics, BUTTON_TEXTURE, getX(), getY(), 0, textureY() + 24, width, height, 200, 15,
            2, 3, 2, 2);
        GuiDraw.blitWithBorder(graphics, BUTTON_TEXTURE, getX() + (int)(value * (double)(width - 8)), getY(), 0,
            handleTextureY() + 24, 8, height, 200, 15, 2, 3, 2, 2);
        int color = (active ? 0xFFFFFF : 0xA0A0A0) | Mth.ceil(alpha * 255.0F) << 24;
        GuiDraw.drawCenteredString(graphics, font, getMessage(), getX() + width / 2, getY() + (height - 8) / 2, color);
    }
}
