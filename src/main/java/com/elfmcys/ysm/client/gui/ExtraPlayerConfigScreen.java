// Modified for the unofficial Fabric 26.2 port by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.gui;

import com.elfmcys.ysm.config.ExtraPlayerScreenConfig;
import com.elfmcys.ysm.util.RenderUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class ExtraPlayerConfigScreen extends Screen {
    private static final char RESET_KEY = 'r';
    private int posX;
    private int posY;
    private float scale;
    private float yawOffset;
    private boolean isChangePos = false;
    private boolean isChangeScale = false;
    private int controlSize = 5;
    private int yawChangeButton = GLFW.GLFW_MOUSE_BUTTON_RIGHT;

    public ExtraPlayerConfigScreen() {
        super(Component.literal("YSM Extra Player Render Config GUI"));
        this.posX = ExtraPlayerScreenConfig.PLAYER_POS_X.get();
        this.posY = ExtraPlayerScreenConfig.PLAYER_POS_Y.get();
        this.scale = ExtraPlayerScreenConfig.PLAYER_SCALE.get().floatValue();
        this.yawOffset = ExtraPlayerScreenConfig.PLAYER_YAW_OFFSET.get().floatValue();
        if (AndroidCompat.isAndroid()) {
            this.controlSize = 16;
            this.yawChangeButton = GLFW.GLFW_MOUSE_BUTTON_LEFT;
        }
    }

    @Override
    protected void init() {
        this.clearWidgets();

        int yOffset = -30;
        if (AndroidCompat.isAndroid()) {
            Component reset = Component.translatable("controls.reset");
            this.addRenderableWidget(Button.builder(reset, button -> this.reset())
                .bounds(this.width / 2 - 50, this.height - 35, 100, 30).build());
            yOffset = -60;
        }

        Component name = Component.translatable("gui.yes_steve_model.hide_or_show");
        int nameWidth = this.font.width(name) + 24;
        this.addRenderableWidget(Checkbox.builder(name, this.font).pos((this.width - nameWidth) / 2, this.height + yOffset)
            .selected(ExtraPlayerScreenConfig.DISABLE_PLAYER_RENDER.get())
            .onValueChange((checkbox, selected) -> {
                ExtraPlayerScreenConfig.DISABLE_PLAYER_RENDER.set(selected);
                ExtraPlayerScreenConfig.DISABLE_PLAYER_RENDER.save();
            }).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int pMouseX, int pMouseY, float frameDeltaTime) {
        int startX = this.posX;
        int startY = this.posY;
        int endX = (int)(startX + this.scale * 1);
        int endY = (int)(startY + this.scale * 2);

        graphics.pose().pushMatrix();
        graphics.pose().translate((float)(0), (float)(0));

        GuiDraw.vLine(graphics, width / 2 - 1, -2, height + 2, 0x9fffffff);
        GuiDraw.hLine(graphics, -2, width + 2, height / 2 - 1, 0x9fffffff);

        GuiDraw.vLine(graphics, 10, -2, height + 2, 0x9fffffff);
        GuiDraw.vLine(graphics, width - 10, -2, height + 2, 0x9fffffff);
        GuiDraw.hLine(graphics, -2, width + 2, 10, 0x9fffffff);
        GuiDraw.hLine(graphics, -2, width + 2, height - 10, 0x9fffffff);

        GuiDraw.vLine(graphics, startX, startY, endY, 0xffff0000);
        GuiDraw.vLine(graphics, endX, startY, endY, 0xffff0000);
        GuiDraw.hLine(graphics, startX, endX, startY, 0xffff0000);
        GuiDraw.hLine(graphics, startX, endX, endY, 0xffff0000);

        graphics.fillGradient(startX, startY, endX, endY, 0x4fffffff, 0x4fffffff);

        graphics.fillGradient(startX - this.controlSize, startY - this.controlSize, startX + this.controlSize,
            startY + this.controlSize, 0xFF00FF9F, 0xFF00FF9F);
        graphics.fillGradient(endX - this.controlSize, endY - this.controlSize, endX + this.controlSize,
            endY + this.controlSize, 0xFF00009F, 0xFF00009F);

        int y = 15;
        MutableComponent component = Component.translatable("gui.yes_steve_model.extra_player_render.tips");
        List<FormattedCharSequence> split = font.split(component, 500);
        for (FormattedCharSequence charSequence : split) {
            int w = font.width(charSequence);
            GuiDraw.drawString(graphics, font, charSequence, width - 15 - w, y, 0xFFFFFF);
            y += 10;
        }

        graphics.pose().popMatrix();

        if (this.minecraft.player != null && !ExtraPlayerScreenConfig.DISABLE_PLAYER_RENDER.get()) {
            RenderUtil.renderExtraPlayerEntity(graphics, this.minecraft.player, this.posX, this.posY, this.scale,
                this.yawOffset, -500, minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false));
        }

        super.extractRenderState(graphics, pMouseX, pMouseY, frameDeltaTime);
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x(), mouseY = event.y();
        int button = event.button();
        boolean xIn = this.posX - this.controlSize < mouseX && mouseX < this.posX + this.controlSize;
        boolean yIn = this.posY - this.controlSize < mouseY && mouseY < this.posY + this.controlSize;
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && xIn && yIn) {
            this.isChangePos = true;
        }
        int endX = (int)(this.posX + this.scale * 1);
        int endY = (int)(this.posY + this.scale * 2);
        boolean xIn2 = endX - this.controlSize < mouseX && mouseX < endX + this.controlSize;
        boolean yIn2 = endY - this.controlSize < mouseY && mouseY < endY + this.controlSize;
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && xIn2 && yIn2) {
            this.isChangeScale = true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseReleased(net.minecraft.client.input.MouseButtonEvent event) {
        double pMouseX = event.x(), pMouseY = event.y();
        int pButton = event.button();
        this.isChangePos = false;
        this.isChangeScale = false;
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(net.minecraft.client.input.MouseButtonEvent event, double deltaX, double deltaY) {
        double mouseX = event.x(), mouseY = event.y();
        int button = event.button();
        if (isChangeScale) {
            double scale1 = mouseX - this.posX;
            double scale2 = (mouseY - this.posY) / 2;
            this.scale = (float)Math.min(scale1, scale2);
            return true;
        }
        if (isChangePos) {
            this.posX = (int)mouseX;
            this.posY = (int)mouseY;
            return true;
        }
        // 手机下是左键，桌面端是右键
        if (button == this.yawChangeButton) {
            this.yawOffset += (float)(deltaX * 2);
            return true;
        }
        return false;
    }

    @Override
    public boolean charTyped(net.minecraft.client.input.CharacterEvent event) {
        char typedChar = (char)event.codepoint();
        int keyCode = 0;
        if (Character.toLowerCase(typedChar) == RESET_KEY && net.minecraft.client.Minecraft.getInstance().hasAltDown()) {
            this.reset();
        }
        return super.charTyped(event);
    }

    private void reset() {
        this.posX = 10;
        this.posY = 10;
        this.scale = 40;
        this.yawOffset = 5;
    }

    @Override
    public void onClose() {
        ExtraPlayerScreenConfig.PLAYER_POS_X.set(this.posX);
        ExtraPlayerScreenConfig.PLAYER_POS_Y.set(this.posY);
        ExtraPlayerScreenConfig.PLAYER_SCALE.set((double)this.scale);
        ExtraPlayerScreenConfig.PLAYER_YAW_OFFSET.set((double)this.yawOffset);
        // One save writes the whole client spec (all four values above).
        ExtraPlayerScreenConfig.PLAYER_YAW_OFFSET.save();
        super.onClose();
    }
}
