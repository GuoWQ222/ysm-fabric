// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.model.storage;

import java.nio.file.Path;
import java.util.Objects;

/** Logical DATA and schema identity, independent of the physical cache layout. */
public record ConvertedScope(String dataIdentity, String version) {
    /**
     * Version of converted (imported) models. Fabric port: the model schema plus a port conversion revision; raise
     * the revision when the port changes conversion output (for example the native legacy decoder), so models
     * converted by an older build are converted again and the old copies are pruned as another schema.
     * fabric.1: 2.6.5 default texture (declared, else first in file order); unknown form types skipped.
     */
    public static final String CURRENT_VERSION = com.elfmcys.ysm.compatibility.AbiVersion.MODEL_SCHEMA + "+fabric.1";

    public ConvertedScope {
        Objects.requireNonNull(dataIdentity);
        Objects.requireNonNull(version);
        if (version.isBlank())
            throw new IllegalArgumentException("Conversion schema must not be blank");
    }

    public static ConvertedScope of(Path dataRoot, String version) {
        var data = dataRoot.toAbsolutePath().normalize();
        return new ConvertedScope(data.getFileSystem().provider().getScheme() + "\0" + data, version);
    }
}
