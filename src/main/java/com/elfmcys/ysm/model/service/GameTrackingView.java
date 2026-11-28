// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.model.service;

import com.elfmcys.ysm.mixin.fabric.ChunkMapAccessor;
import com.elfmcys.ysm.mixin.fabric.TrackedEntityAccessor;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;

/** Borrows the current host tracking relation without retaining a parallel index. */
public interface GameTrackingView {
    List<ServerPlayer> observers(ServerPlayer subject);

    boolean isTracking(ServerPlayer observer, ServerPlayer subject);

    static GameTrackingView live() {
        return new GameTrackingView() {
            @Override
            public List<ServerPlayer> observers(ServerPlayer subject) {
                var tracked = tracked(subject);
                if (tracked == null)
                    return List.of();
                return tracked.ysm$getSeenBy().stream().map(connection -> connection.getPlayer()).toList();
            }

            @Override
            public boolean isTracking(ServerPlayer observer, ServerPlayer subject) {
                var tracked = tracked(subject);
                return tracked != null && tracked.ysm$getSeenBy().contains(observer.connection);
            }

            private TrackedEntityAccessor tracked(ServerPlayer subject) {
                return (TrackedEntityAccessor)((ChunkMapAccessor)((net.minecraft.server.level.ServerLevel)subject.level()).getChunkSource().chunkMap)
                    .ysm$getEntityMap().get(subject.getId());
            }
        };
    }
}
