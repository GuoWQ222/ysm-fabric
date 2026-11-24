// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.util;

import com.elfmcys.ysm.YesSteveModel;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.resources.Identifier;

import java.util.HashSet;
import java.util.Set;

public final class ModelIdUtil {
    private static final Set<String> KNOWN_EXT = Sets.newHashSet(".zip", ".7z", ".ysm", ".mxc");

    /**
     * "dir1/dir2/id" -> {"id", "dir1/dir2/"}
     * <p/>
     * "id" -> {"id", ""}
     */
    public static Pair<String, String> splitModelPath(String modelPath) {
        var lastSlash = modelPath.lastIndexOf('/');
        if (lastSlash == -1) {
            return Pair.of(modelPath, "");
        } else {
            return Pair.of(modelPath.substring(lastSlash + 1), modelPath.substring(0, lastSlash + 1));
        }
    }

    public static String getFileNameFromPath(String modelPath) {
        var lastSlash = modelPath.lastIndexOf('/');
        String modelName;
        if (lastSlash == -1) {
            modelName = modelPath;
        } else {
            modelName = modelPath.substring(lastSlash + 1);
        }
        var lastDot = modelName.lastIndexOf('.');
        if (lastDot < 1 || !KNOWN_EXT.contains(modelName.substring(lastDot).toLowerCase())) {
            return modelName;
        }

        return modelName.substring(0, lastDot);
    }

    public static String getLastFolderName(String path) {
        if (path == null || path.isEmpty()) {
            return "";
        }
        String trimmed = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
        int idx = trimmed.lastIndexOf('/');
        return idx >= 0 ? trimmed.substring(idx + 1) : trimmed;
    }

    @SuppressWarnings("removal")
    public static Identifier getModelPackIconId(String hierarchy) {
        return Identifier.fromNamespaceAndPath(YesSteveModel.MOD_ID, "model_pack_icon/" + hierarchy.hashCode());
    }

    @SuppressWarnings({"DataFlowIssue", "deprecation"})
    public static Set<Identifier> getEntityIdMatch(String[] matches) {
        var set = new HashSet<Identifier>();
        for (var match : matches) {
            if (match.startsWith("#")) {
                var tagId = Identifier.tryParse(match.substring(1));
                if (tagId == null) {
                    continue;
                }
                var tagKey = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, tagId);
                net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getTagOrEmpty(tagKey)
                    .forEach(type -> type.unwrapKey().ifPresent(key -> set.add(key.identifier())));
            } else {
                var entityId = Identifier.tryParse(match);
                if (entityId == null) {
                    continue;
                }
                set.add(entityId);
                // Since 1.21.2 every boat wood is its own entity type; models written for older versions (including
                // 3.0-dev's own built-ins) still say minecraft:boat / minecraft:chest_boat.
                if (entityId.getNamespace().equals("minecraft")
                    && !net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.containsKey(entityId)) {
                    if (entityId.getPath().equals("boat")) {
                        net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE
                            .getTagOrEmpty(net.minecraft.tags.EntityTypeTags.BOAT)
                            .forEach(type -> type.unwrapKey().ifPresent(key -> set.add(key.identifier())));
                    } else if (entityId.getPath().equals("chest_boat")) {
                        for (var key : net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.keySet())
                            if (key.getNamespace().equals("minecraft")
                                && (key.getPath().endsWith("_chest_boat") || key.getPath().endsWith("_chest_raft")))
                                set.add(key);
                    }
                }
            }
        }
        return set;
    }
}
