// Modified for the unofficial Fabric 26.2 port by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.accessor;

/** Implemented on AbstractArrow by AbstractArrowExtraInfoMixin (Fabric port: ysm$ names avoid 26.x clashes). */
public interface IArrowExtraInfo {
    boolean ysm$isInGround();

    int ysm$inGroundTime();

    String ysm$getShootItemId();
}
