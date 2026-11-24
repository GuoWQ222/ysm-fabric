// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.model.storage;

import com.elfmcys.ysm.buffer.SharedArrayBuffer;
import com.elfmcys.ysm.model.domain.PublishedModelId;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

final class PreferenceCodec {
    // Stored roaming format: version 1 = one-byte field count (written by upstream builds), version 2 (Fabric port)
    // = two-byte count for more than 255 fields; each name length is one unsigned byte.
    private static final byte ROAMING_FORMAT = 2;
    private static final int MAX_FIELDS = com.elfmcys.ysm.network.protocol.ProtocolLimits.MAX_ROAMING_VARIABLES;
    private static final int MAX_NAME_BYTES = com.elfmcys.ysm.network.protocol.ProtocolLimits.MAX_ROAMING_VARIABLE_NAME_BYTES;

    static {
        if (MAX_FIELDS > 0xffff || MAX_NAME_BYTES > 255)
            throw new ExceptionInInitializerError("roaming limits exceed the stored format");
    }

    private PreferenceCodec() {}

    static NamedRoamingSnapshot snapshot(NamedRoamingSnapshot payload) {
        if (payload.names() == null || payload.values() == null || payload.names().length != payload.values().length)
            throw new IllegalArgumentException("Roaming arrays must have equal lengths");
        if (payload.names().length > MAX_FIELDS)
            throw new IllegalArgumentException("Roaming payload exceeds " + MAX_FIELDS + " fields");
        var names = new java.util.HashSet<String>();
        for (int index = 0; index < payload.names().length; index++) {
            var name = payload.names()[index];
            if (name == null)
                throw new IllegalArgumentException("Invalid roaming field");
            var bytes = utf8(name);
            if (name.isEmpty() || bytes.length > MAX_NAME_BYTES || !names.add(name))
                throw new IllegalArgumentException("Invalid roaming field");
            if (!Float.isFinite(payload.values()[index]))
                throw new IllegalArgumentException("Roaming values must be finite");
        }
        return payload;
    }

    static byte[] roaming(NamedRoamingSnapshot value) {
        var bytes = ByteBuffer.allocate(3 + value.names().length * (1 + MAX_NAME_BYTES + 4));
        bytes.put(ROAMING_FORMAT).putShort((short)value.names().length);
        for (int index = 0; index < value.names().length; index++) {
            var text = utf8(value.names()[index]);
            bytes.put((byte)text.length).put(text).putFloat(value.values()[index]);
        }
        return java.util.Arrays.copyOf(bytes.array(), bytes.position());
    }

    static NamedRoamingSnapshot roaming(SharedArrayBuffer value) {
        var bytes = value.nio().order(ByteOrder.BIG_ENDIAN);
        var format = bytes.get();
        if (format != 1 && format != ROAMING_FORMAT)
            throw new IllegalArgumentException("Unknown preference value format");
        var count = format == 1 ? Byte.toUnsignedInt(bytes.get()) : Short.toUnsignedInt(bytes.getShort());
        if (count > MAX_FIELDS)
            throw new IllegalArgumentException("Roaming payload exceeds " + MAX_FIELDS + " fields");
        var names = new String[count];
        var values = new float[count];
        var seen = new java.util.HashSet<String>();
        for (var index = 0; index < count; index++) {
            var length = Byte.toUnsignedInt(bytes.get());
            if (length == 0 || length > MAX_NAME_BYTES)
                throw new IllegalArgumentException("Invalid roaming field");
            names[index] = text(bytes, length);
            if (!seen.add(names[index]))
                throw new IllegalArgumentException("Invalid roaming field");
            values[index] = bytes.getFloat();
            if (!Float.isFinite(values[index]))
                throw new IllegalArgumentException("Roaming values must be finite");
        }
        end(bytes);
        return new NamedRoamingSnapshot(names, values);
    }

    static byte[] selection(PlayerPreferences.LocalSelection value) {
        var identity = utf8(value.identity().toString());
        var texture = utf8(value.texture());
        return ByteBuffer.allocate(5 + identity.length + texture.length).put((byte)1).putShort((short)identity.length)
            .put(identity).putShort((short)texture.length).put(texture).array();
    }

    static PlayerPreferences.LocalSelection selection(SharedArrayBuffer value) {
        var bytes = value.nio().order(ByteOrder.BIG_ENDIAN);
        version(bytes);
        var identity = PublishedModelId.parse(text(bytes, Short.toUnsignedInt(bytes.getShort())));
        var texture = text(bytes, Short.toUnsignedInt(bytes.getShort()));
        end(bytes);
        return new PlayerPreferences.LocalSelection(identity, texture);
    }

    static Boolean favourite(SharedArrayBuffer value) {
        var bytes = value.nio();
        if (bytes.remaining() != 1 || (bytes.get(0) != 0 && bytes.get(0) != 1))
            throw new IllegalArgumentException("Invalid favourite value");
        return bytes.get() == 1;
    }

    static byte[] utf8(String text) {
        try {
            var bytes = StandardCharsets.UTF_8.newEncoder().onMalformedInput(CodingErrorAction.REPORT)
                .encode(java.nio.CharBuffer.wrap(text));
            var result = new byte[bytes.remaining()];
            bytes.get(result);
            return result;
        } catch (CharacterCodingException failure) {
            throw new IllegalArgumentException("Invalid UTF-8 text", failure);
        }
    }

    private static String text(ByteBuffer bytes, int length) {
        if (length > bytes.remaining())
            throw new IllegalArgumentException("Truncated preference text");
        var part = bytes.slice();
        part.limit(length);
        bytes.position(bytes.position() + length);
        try {
            return StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT).decode(part).toString();
        } catch (CharacterCodingException failure) {
            throw new IllegalArgumentException("Invalid UTF-8 text", failure);
        }
    }

    private static void version(ByteBuffer bytes) {
        if (bytes.get() != 1)
            throw new IllegalArgumentException("Unknown preference value format");
    }

    private static void end(ByteBuffer bytes) {
        if (bytes.hasRemaining())
            throw new IllegalArgumentException("Trailing preference bytes");
    }
}
