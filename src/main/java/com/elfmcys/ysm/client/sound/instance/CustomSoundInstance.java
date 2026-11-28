// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.sound.instance;

import com.elfmcys.ysm.client.sound.stream.CustomAudioStream;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/** A prepared stream adapter. Playback ownership remains in the default YSM backend. */
public final class CustomSoundInstance extends MinecraftSoundInstance
    //#if LOADER == fabric
    implements AudioHost.HostPlayback, net.fabricmc.fabric.api.client.sound.v1.FabricSoundInstance {
    //#else
    //$$ implements AudioHost.HostPlayback {
    //#endif
    private final CustomAudioStream stream;

    public CustomSoundInstance(SoundEvent soundEvent, CustomAudioStream stream, Entity entity) {
        super(soundEvent, entity);
        this.stream = Objects.requireNonNull(stream, "stream");
    }

    //#if LOADER == fabric
    @Override
    public CompletableFuture<AudioStream> getAudioStream(SoundBufferLibrary soundBuffers,
        net.minecraft.resources.Identifier id, boolean looping) {
        return CompletableFuture.completedFuture(new MinecraftAudioStreamAdapter(stream));
    }
    //#elseif LOADER == neoforge || MC < 1.21.6
    //$$ // NeoForge, and Forge before 1.21.6: SoundInstance extension (custom stream instead of a sound file)
    //$$ @Override
    //$$ public CompletableFuture<AudioStream> getStream(SoundBufferLibrary soundBuffers,
    //$$     net.minecraft.client.resources.sounds.Sound sound, boolean looping) {
    //$$     return CompletableFuture.completedFuture(new MinecraftAudioStreamAdapter(stream));
    //$$ }
    //#else
    //$$ // Forge 26 has no stream hook on SoundInstance: forge.mixin.SoundEngineStreamMixin calls this instead
    //$$ public CompletableFuture<AudioStream> ysmStream() {
    //$$     return CompletableFuture.completedFuture(new MinecraftAudioStreamAdapter(stream));
    //$$ }
    //#endif

    @Override
    public boolean active() {
        return Minecraft.getInstance().getSoundManager().isActive(this);
    }

    @Override
    public void stopPlayback() {
        setStopped();
    }

    private static final class MinecraftAudioStreamAdapter implements AudioStream {
        private final CustomAudioStream stream;

        private MinecraftAudioStreamAdapter(CustomAudioStream stream) {
            this.stream = stream;
        }

        @Override
        public AudioFormat getFormat() {
            return stream.getFormat();
        }

        @Override
        public ByteBuffer read(int capacity) throws IOException {
            return stream.read(capacity);
        }

        @Override
        public void close() throws IOException {
            stream.close();
        }
    }
}
