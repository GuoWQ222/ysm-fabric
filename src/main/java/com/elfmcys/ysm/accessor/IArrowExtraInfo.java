// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.accessor;

/** Implemented on AbstractArrow by AbstractArrowExtraInfoMixin (Fabric port: ysm$ names avoid 26.x clashes). */
public interface IArrowExtraInfo {
    boolean ysm$isInGround();

    int ysm$inGroundTime();

    String ysm$getShootItemId();
}
