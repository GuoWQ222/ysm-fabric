// Modified for the unofficial Fabric 26.2 port by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.format.schema.model;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.buffer.NativeBuffer;
import com.elfmcys.ysm.buffer.SharedNativeBuffer;
import com.elfmcys.ysm.buffer.UniBuffer;
import com.elfmcys.ysm.format.schema.file.AssetFileWriter;
import com.elfmcys.ysm.model.domain.ModelId;
import com.elfmcys.ysm.natives.image.Image;
import com.elfmcys.ysm.proto.mixel.manifest.ManifestProto;
import com.elfmcys.ysm.util.ProtoBytes;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.Objects;

public class ModelFileWriter extends AssetFileWriter {
    public ModelFileWriter() {
        setSchemaId(ModelFileConstant.SCHEMA_ID);
        setProperty(ModelFileConstant.PROP_VERSION, ModelFileConstant.CURRENT_VERSION.toString());
        // Fabric port: Fabric jars carry no FML code-signing fingerprint.
        setProperty(ModelFileConstant.PROP_VENDOR, String.format("%s(%s) v%s", "Yes Steve Model", YesSteveModel.MOD_ID,
            com.elfmcys.ysm.platform.Platform.ownVersion()));
        setProperty(ModelFileConstant.PROP_SIGNATURE, "(unsigned)");
    }

    private ModelFileWriter(ModelFileWriter source) {
        super(source);
    }

    public int[] addSoundChunks(ByteBuffer[] chunks) {
        Objects.requireNonNull(chunks, "chunks");
        var ids = new int[chunks.length];
        for (var index = 0; index < chunks.length; ++index) {
            var data = SharedNativeBuffer.adopt(NativeBuffer.copyOf(chunks[index]));
            ids[index] = addBlob(data, 0);
        }
        return ids;
    }

    public long serializedLength(ManifestProto manifest, ByteBuffer[] candidateChunks) throws IOException {
        Objects.requireNonNull(manifest, "manifest");
        Objects.requireNonNull(candidateChunks, "candidateChunks");
        try (var candidate = new ModelFileWriter(this)) {
            for (var chunk : candidateChunks) {
                candidate.addBorrowedBlob(chunk);
            }
            candidate.setManifest(manifest);
            var output = new CountingChannel();
            candidate.write(output);
            return output.count;
        }
    }

    public void setManifest(ManifestProto manifest) throws IOException {
        setManifestProperties(manifest);
        addProtoChunk(ModelFileConstant.MANIFEST_CHUNK_NAME, manifest, 0);
    }

    public void setManifestExact(ManifestProto manifest, UniBuffer logicalBytes) throws IOException {
        setManifestProperties(manifest);
        addRawChunk(ModelFileConstant.MANIFEST_CHUNK_NAME, Objects.requireNonNull(logicalBytes, "logicalBytes"), 0);
    }

    private void setManifestProperties(ManifestProto manifest) throws IOException {
        var properties = manifest.info().properties();
        try {
            var modelId = ModelId.parse(properties.modelId());
            setProperty(ModelFileConstant.PROP_MODEL_ID, modelId.toString());
        } catch (IllegalArgumentException failure) {
            throw new IOException("Manifest contains an invalid ModelId", failure);
        }
        generateSummary(manifest);
    }

    private void generateSummary(ManifestProto manifest) {
        if (manifest.info().hasMetadata()) {
            setSummary(manifest.info().metadataUnsafe().name());
        } else {
            setSummary("");
        }
    }

    public void setThumbnail(Image img) {
        addImage(ModelFileConstant.THUMB_BUTTON_CHUNK_NAME, img);
    }

    public void setIcon(Image img) {
        addImage(ModelFileConstant.THUMB_ICON_CHUNK_NAME, img);
    }

    private static final class CountingChannel implements WritableByteChannel {
        private long count;
        private boolean open = true;

        @Override
        public int write(ByteBuffer source) {
            if (!open) {
                throw new IllegalStateException("Counting channel is closed");
            }
            var bytes = source.remaining();
            count = Math.addExact(count, bytes);
            source.position(source.limit());
            return bytes;
        }

        @Override
        public boolean isOpen() {
            return open;
        }

        @Override
        public void close() {
            open = false;
        }
    }
}
