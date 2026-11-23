// Modified for the unofficial Fabric 26.2 port by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.event;

import com.elfmcys.ysm.client.gui.DownloadScreen;
import com.elfmcys.ysm.client.gui.PlayerModelScreen;

/** Opens the model download screen. Fabric port: Forge's IMC screen override is not available. */
public final class DownloadScreenInterModEvent {
    private DownloadScreenInterModEvent() {}

    public static void openDownloadScreen(PlayerModelScreen modelScreen) {
        modelScreen.showScreen(new DownloadScreen(modelScreen));
    }
}
