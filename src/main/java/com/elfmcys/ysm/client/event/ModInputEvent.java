// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.event;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.util.InputCheckUtil;
import com.elfmcys.ysm.platform.Dist;
import com.elfmcys.ysm.platform.event.forge.InputEvent;
import com.elfmcys.ysm.platform.event.SubscribeEvent;
import com.elfmcys.ysm.platform.event.Mod;
import com.mojang.blaze3d.platform.InputConstants;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = YesSteveModel.MOD_ID)
public class ModInputEvent {
    // Input callbacks finish before animation publication; frame owners join readers before the next input poll.
    public static final boolean[] KEY_STATES = new boolean[com.elfmcys.ysm.platform.input.KeyCodes.GLFW_KEY_LAST + 1];
    public static final boolean[] MOUSE_STATES = new boolean[com.elfmcys.ysm.platform.input.KeyCodes.GLFW_MOUSE_LAST + 1];

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (!YesSteveModel.isAvailable()) {
            return;
        }
        if (!InputCheckUtil.isInGame()) {
            return;
        }
        // the table uses GLFW numbering (as models do); 26.3 reports SDL scancodes
        int key = com.elfmcys.ysm.platform.input.KeyCodes.toGlfwKey(event.getKey());
        if (com.elfmcys.ysm.platform.input.KeyCodes.GLFW_KEY_FIRST <= key && key <= com.elfmcys.ysm.platform.input.KeyCodes.GLFW_KEY_LAST) {
            if (event.getAction() == InputConstants.PRESS) {
                KEY_STATES[key] = true;
            } else if (event.getAction() == InputConstants.RELEASE) {
                KEY_STATES[key] = false;
            }
        }
    }

    @SubscribeEvent
    public static void onMouseInput(InputEvent.MouseButton.Post event) {
        if (!YesSteveModel.isAvailable()) {
            return;
        }
        if (!InputCheckUtil.isInGame()) {
            return;
        }
        int button = com.elfmcys.ysm.platform.input.KeyCodes.toGlfwMouse(event.getButton());
        if (com.elfmcys.ysm.platform.input.KeyCodes.GLFW_MOUSE_FIRST <= button && button <= com.elfmcys.ysm.platform.input.KeyCodes.GLFW_MOUSE_LAST) {
            if (event.getAction() == InputConstants.PRESS) {
                MOUSE_STATES[button] = true;
            } else if (event.getAction() == InputConstants.RELEASE) {
                MOUSE_STATES[button] = false;
            }
        }
    }
}
