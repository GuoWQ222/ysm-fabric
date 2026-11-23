// Modified for the unofficial Fabric 26.2 port by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.network;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.buffer.ArrayBuffer;
import com.elfmcys.ysm.buffer.UniBuffer;
import com.elfmcys.ysm.network.forge.ClientSessionRuntime;
import com.elfmcys.ysm.network.forge.ForgeUniBufferIO;
import com.elfmcys.ysm.network.frame.FrameCodec;
import com.elfmcys.ysm.network.frame.OutboundFrame;
import com.elfmcys.ysm.network.protocol.MessageDirection;
import com.elfmcys.ysm.network.protocol.ProtocolMessageSpec;
import com.elfmcys.ysm.network.protocol.ProtocolMessages;
import com.elfmcys.ysm.network.protocol.ProtocolVersion;
import com.elfmcys.ysm.platform.Connections;
import com.elfmcys.ysm.platform.network.NetworkDirection;
import com.elfmcys.ysm.platform.network.NetworkEvent;
import com.elfmcys.ysm.platform.network.PacketTarget;
import io.netty.buffer.Unpooled;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import us.hebi.quickbuf.ProtoMessage;

/**
 * The sole loader adapter for the current YSM frame protocol. Fabric port: one {@link YsmFramePayload} type on the
 * ysm channel replaces Forge's event channel; frames, limits and diagnostics are unchanged.
 */
public final class NetworkHandler {
    public static final String VERSION = ProtocolVersion.TRANSPORT_VERSION;
    public static final Identifier CHANNEL_NAME =
        Identifier.fromNamespaceAndPath(YesSteveModel.MOD_ID, ProtocolVersion.CHANNEL_PATH);
    private static boolean initialized;

    private NetworkHandler() {}

    public static synchronized void init() {
        if (initialized)
            return;
        initialized = true;
        PayloadTypeRegistry.clientboundPlay().register(YsmFramePayload.TYPE, YsmFramePayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(YsmFramePayload.TYPE, YsmFramePayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SoundPolicyPayload.TYPE, SoundPolicyPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(YsmFramePayload.TYPE,
            (payload, context) -> receive(payload, new NetworkEvent.Context(NetworkDirection.PLAY_TO_SERVER,
                Connections.of(context.player()), context.player(), context.server()),
                MessageDirection.CLIENT_TO_SERVER));
        if (com.elfmcys.ysm.platform.Platform.isClient())
            NetworkHandlerClient.init();
    }

    /** 2.6.5 AcceptSoundFX: tells the player's client which model sounds to fetch and play. */
    public static void sendSoundPolicy(ServerPlayer player) {
        if (player.connection != null && ServerPlayNetworking.canSend(player, SoundPolicyPayload.TYPE))
            ServerPlayNetworking.send(player,
                new SoundPolicyPayload(com.elfmcys.ysm.config.ServerConfig.ACCEPT_SOUND_FX.get()));
    }

    @SuppressWarnings("ConstantValue")
    public static boolean isPlayerChannelPresent(ServerPlayer player) {
        return player.connection != null && ServerPlayNetworking.canSend(player, YsmFramePayload.TYPE);
    }

    public static boolean isRemoteChannelPresent() {
        return NetworkHandlerClient.isRemoteChannelPresent();
    }

    public static boolean isChannelPresent(@Nullable Connection connection) {
        if (connection == null || com.elfmcys.ysm.platform.Connections.channel(connection) == null)
            return false;
        var listener = connection.getPacketListener();
        if (listener instanceof ServerGamePacketListenerImpl server)
            return ServerPlayNetworking.canSend(server, YsmFramePayload.TYPE);
        return com.elfmcys.ysm.platform.Platform.isClient() && NetworkHandlerClient.isChannelPresent(connection);
    }

    public static int sendToServer(Connection connection, NetworkPayload<?> payload) {
        try (payload; var protobuf = ProtocolBuffer.serialize(payload.protobuf());
            var empty = ArrayBuffer.allocate(0)) {
            var spec = messageSpec(payload.protobuf(), NetworkDirection.PLAY_TO_SERVER);
            var raw = payload.raw().<UniBuffer>map(value -> value).orElse(empty);
            spec.attachmentPolicy().validate(raw.size());
            try (var frame = FrameCodec.encode(spec.id(), protobuf, raw)) {
                NetworkDiagnostics.message("send-attempt", MessageDirection.CLIENT_TO_SERVER, payload.protobuf(),
                    "ordinary");
                sendFrame(NetworkDirection.PLAY_TO_SERVER, connection::send, frame, "ordinary");
                return frame.size();
            }
        }
    }

    public static void sendToServer(Connection connection, ProtoMessage<?> message) {
        sendToServer(connection, payload(message));
    }

    public static void sendToServer(ProtoMessage<?> message) {
        sendToServer(payload(message));
    }

    public static void sendToServer(NetworkPayload<?> payload) {
        if (!isRemoteChannelPresent()) {
            NetworkDiagnostics.message("send-dropped", MessageDirection.CLIENT_TO_SERVER, payload.protobuf(),
                "channel-unavailable");
            payload.close();
            return;
        }
        send(PacketTarget.server(), payload);
    }

    public static void sendToClientPlayer(ProtoMessage<?> message, Player player) {
        sendToClientPlayer(payload(message), player);
    }

    public static void sendToClientPlayer(NetworkPayload<?> payload, Player player) {
        send(PacketTarget.player((ServerPlayer)player), payload);
    }

    public static void broadcastToVisiblePlayers(ProtoMessage<?> message, Entity centerEntity) {
        send(PacketTarget.trackingEntity(centerEntity), payload(message));
    }

    public static void broadcastToVisiblePlayersAndSelf(ProtoMessage<?> message, Entity centerEntity) {
        send(PacketTarget.trackingEntityAndSelf(centerEntity), payload(message));
    }

    public static void sendFrame(PacketTarget target, OutboundFrame frame) {
        sendFrame(target.getDirection(), target::send, frame, "dispatch");
    }

    private static void sendFrame(NetworkDirection direction, Consumer<Packet<?>> sender, OutboundFrame frame,
        String path) {
        var diagnosticDirection = direction == NetworkDirection.PLAY_TO_SERVER ? MessageDirection.CLIENT_TO_SERVER
            : MessageDirection.SERVER_TO_CLIENT;
        NetworkDiagnostics.frame("send-attempt", diagnosticDirection, frame, path);
        var submitted = false;
        try {
            var bytes = new byte[frame.size()];
            var buffer = new FriendlyByteBuf(Unpooled.wrappedBuffer(bytes).writerIndex(0));
            ForgeUniBufferIO.write(buffer, frame.borrow());
            var payload = new YsmFramePayload(bytes);
            Packet<?> packet = direction == NetworkDirection.PLAY_TO_SERVER ? new ServerboundCustomPayloadPacket(payload)
                : new net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket(payload);
            sender.accept(packet);
            submitted = true;
            NetworkDiagnostics.frame("send-submitted", diagnosticDirection, frame, path);
        } finally {
            if (!submitted)
                NetworkDiagnostics.frame("send-failed", diagnosticDirection, frame, path);
        }
    }

    private static void send(PacketTarget target, NetworkPayload<?> payload) {
        try (payload; var protobuf = ProtocolBuffer.serialize(payload.protobuf())) {
            var attachment =
                payload.raw().<UniBuffer>map(value -> value).orElseGet(() -> ArrayBuffer.borrow(new byte[0]));
            var spec = messageSpec(payload.protobuf(), target.getDirection());
            spec.attachmentPolicy().validate(attachment.size());
            try (var frame = FrameCodec.encode(spec.id(), protobuf, attachment)) {
                var direction = target.getDirection() == NetworkDirection.PLAY_TO_SERVER
                    ? MessageDirection.CLIENT_TO_SERVER : MessageDirection.SERVER_TO_CLIENT;
                NetworkDiagnostics.message("send-attempt", direction, payload.protobuf(), "ordinary");
                sendFrame(target.getDirection(), target::send, frame, "ordinary");
            }
        }
    }

    private static ProtocolMessageSpec<?> messageSpec(ProtoMessage<?> message, NetworkDirection direction) {
        var spec = ProtocolMessages.REGISTRY.find(message.getClass()).orElseThrow(
            () -> new IllegalArgumentException("Unregistered protocol message type: " + message.getClass().getName()));
        var expected = direction == NetworkDirection.PLAY_TO_SERVER ? MessageDirection.CLIENT_TO_SERVER
            : MessageDirection.SERVER_TO_CLIENT;
        if (!spec.direction().accepts(expected)) {
            throw new IllegalArgumentException("Protocol message has the wrong network direction");
        }
        return spec;
    }

    static void receive(YsmFramePayload payload, NetworkEvent.Context context, MessageDirection direction) {
        var frameBytes = payload.frame().length;
        NetworkDiagnostics.frame("received", direction, -1, frameBytes, "fabric");
        try {
            var source = new FriendlyByteBuf(Unpooled.wrappedBuffer(payload.frame()));
            var wire = com.elfmcys.ysm.buffer.SharedNativeBuffer.adopt(ForgeUniBufferIO.readNative(source, frameBytes));
            try (var frame = FrameCodec.decode(wire, id -> ProtocolInbound.accepts(id, direction))) {
                var spec = ProtocolMessages.REGISTRY.find(frame.messageId()).orElseThrow();
                NetworkDiagnostics.frame("decoded", direction, frame.messageId(), wire.size(), "fabric");
                ProtocolInbound.dispatch(frame, spec, () -> context);
            }
        } catch (RuntimeException error) {
            NetworkDiagnostics.frame("dropped", direction, -1, frameBytes, "invalid-frame");
            YesSteveModel.LOGGER.warn("Rejected invalid YSM frame", error);
            if (direction == MessageDirection.SERVER_TO_CLIENT) {
                var connection = context.getNetworkManager();
                context.enqueueWork(() -> {
                    try {
                        ClientSessionRuntime.failProtocolSession(connection);
                    } catch (IllegalStateException ignored) {
                    }
                });
            }
        } finally {
            context.setPacketHandled(true);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static NetworkPayload<?> payload(ProtoMessage<?> message) {
        return NetworkPayload.protobuf((ProtoMessage)message);
    }
}
