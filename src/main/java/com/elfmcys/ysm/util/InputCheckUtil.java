// Modified for the unofficial Fabric 26.2 port by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.util;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import com.elfmcys.ysm.platform.event.forge.InputEvent;
import com.elfmcys.ysm.platform.input.KeyModifier;

public class InputCheckUtil {
    @SuppressWarnings("removal")
    public static boolean keyIsMatch(InputEvent.Key event, com.elfmcys.ysm.platform.input.YsmKeyMapping keyMapping) {
        return keyMapping.matches(event.getKey(), event.getScanCode())
            && keyMapping.getKeyModifier().equals(KeyModifier.getActiveModifier());
    }

    public static boolean isInGame() {
        Minecraft mc = Minecraft.getInstance();
        // 不能是加载界面
        if (mc.gui.overlay() != null) {
            return false;
        }
        // 不能打开任何 GUI
        if (mc.gui.screen() != null) {
            return false;
        }
        // 当前窗口捕获鼠标操作
        if (!mc.mouseHandler.isMouseGrabbed()) {
            return false;
        }
        // 选择了当前窗口
        return mc.isWindowActive();
    }
}
