/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.mixin.fabric;

import com.elfmcys.ysm.network.forge.ChannelCompressionBypass;
import io.netty.channel.Channel;
import net.minecraft.network.CompressionEncoder;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fabric port of ConnectionMixin: YSM frames are already compressed, so the compression encoder writes them
 * uncompressed (vanilla's decoder accepts uncompressed packets above the threshold).
 */
@Mixin(Connection.class)
public abstract class ConnectionCompressionMixin {
    @Shadow
    private Channel channel;

    @Inject(method = "setupCompression", at = @At("RETURN"))
    private void ysm$configureCompressionBypass(int threshold, boolean validateDecompressed, CallbackInfo ci) {
        var pipeline = channel.pipeline();
        if (threshold < 0) {
            if (pipeline.get(ChannelCompressionBypass.MARKER_HANDLER) != null)
                pipeline.remove(ChannelCompressionBypass.MARKER_HANDLER);
            return;
        }
        var current = pipeline.get("compress");
        if (current instanceof CompressionEncoder compression
            && !(current instanceof ChannelCompressionBypass.Encoder)) {
            pipeline.replace("compress", "compress", new ChannelCompressionBypass.Encoder(compression.getThreshold()));
        }
        if (pipeline.get(ChannelCompressionBypass.MARKER_HANDLER) == null) {
            // 26.x names the packet encoder "outbound_config" until its protocol is configured.
            var encoder = pipeline.get("encoder") != null ? "encoder"
                : pipeline.get("outbound_config") != null ? "outbound_config" : null;
            if (encoder != null)
                pipeline.addAfter(encoder, ChannelCompressionBypass.MARKER_HANDLER,
                    new ChannelCompressionBypass.Marker());
        }
    }
}
