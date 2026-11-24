// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.network.protocol;

public final class ProtocolLimits {
    public static final int MAX_PLAYER_STATE_BYTES = 128 * 1024;
    public static final int MAX_ENTITY_ACTION_BYTES = 8 * 1024;
    public static final int MAX_SWING_HAND_BYTES = 256;
    public static final int MAX_MINECRAFT_STATE_BYTES = 32 * 1024;
    /**
     * Fabric port (2.6.5 parity): 2.6.5 sets no limit and community models use up to 450 roaming variables (upstream:
     * 64). 512 names of at most 32 bytes keep a full selection below the 32 KiB serverbound payload limit.
     */
    public static final int MAX_ROAMING_VARIABLES = 512;
    /** Fabric port: community models use names of up to 37 bytes (upstream: 32). */
    public static final int MAX_ROAMING_VARIABLE_NAME_BYTES = 64;
    /**
     * Fabric port: UTF-8 bytes of all names of one roaming dictionary, so that 512 names of up to 64 bytes still keep a
     * full selection (names, ids and values) below the 32 KiB serverbound payload limit.
     */
    public static final int MAX_ROAMING_DICTIONARY_BYTES = 16 * 1024;

    public static int utf8Bytes(Iterable<String> names) {
        int total = 0;
        for (var name : names)
            total += name.getBytes(java.nio.charset.StandardCharsets.UTF_8).length;
        return total;
    }

    private ProtocolLimits() {}
}
