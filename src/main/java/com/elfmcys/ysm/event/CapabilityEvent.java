// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.event;

import com.elfmcys.ysm.platform.capability.ICapabilityHolder;
import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.capability.AuthModelsCapability;
import com.elfmcys.ysm.capability.AuthModelsCapabilityProvider;
import com.elfmcys.ysm.capability.ModelInfoCapability;
import com.elfmcys.ysm.capability.ModelInfoCapabilityProvider;
import com.elfmcys.ysm.capability.ProjectileModelInfoCapabilityProvider;
import com.elfmcys.ysm.capability.VehicleModelInfoCapabilityProvider;
import com.elfmcys.ysm.config.ServerConfig;
import com.elfmcys.ysm.model.ModelRuntime;
import com.elfmcys.ysm.model.service.ServerModelService;
import com.elfmcys.ysm.network.NetworkHandler;
import com.elfmcys.ysm.network.forge.MinecraftStateHandler;
import com.elfmcys.ysm.network.forge.PlayerStateHandler;
import com.elfmcys.ysm.network.forge.SessionProtocolHandler;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import com.elfmcys.ysm.platform.Dist;
import com.elfmcys.ysm.platform.capability.LazyOptional;
import com.elfmcys.ysm.platform.capability.AttachCapabilitiesEvent;
import com.elfmcys.ysm.platform.event.forge.TickEvent;
import com.elfmcys.ysm.platform.event.forge.EntityJoinLevelEvent;
import com.elfmcys.ysm.platform.event.forge.PlayerEvent;
import com.elfmcys.ysm.platform.event.SubscribeEvent;
import com.elfmcys.ysm.platform.event.Mod;
import com.elfmcys.ysm.platform.FMLEnvironment;

@Mod.EventBusSubscriber
@SuppressWarnings("removal")
public final class CapabilityEvent {
    private static final Identifier MODEL_INFO_CAP = Identifier.fromNamespaceAndPath(YesSteveModel.MOD_ID, "model_id");
    private static final Identifier PROJECTILE_MODEL_INFO_CAP =
        Identifier.fromNamespaceAndPath(YesSteveModel.MOD_ID, "projectile_model_id");
    private static final Identifier VEHICLE_MODEL_INFO_CAP =
        Identifier.fromNamespaceAndPath(YesSteveModel.MOD_ID, "vehicle_model_id");
    private static final Identifier AUTH_MODELS_CAP = Identifier.fromNamespaceAndPath(YesSteveModel.MOD_ID, "own_models");
    private static final Identifier ANIMATABLE_CAP = Identifier.fromNamespaceAndPath(YesSteveModel.MOD_ID, "animatable");
    private static final Identifier PROJECTILE_ANIMATABLE_CAP =
        Identifier.fromNamespaceAndPath(YesSteveModel.MOD_ID, "projectile_animatable");
    private static final Identifier VEHICLE_ANIMATABLE_CAP =
        Identifier.fromNamespaceAndPath(YesSteveModel.MOD_ID, "vehicle_animatable");
    private static final Identifier CLIENT_LAZY_CAP = Identifier.fromNamespaceAndPath(YesSteveModel.MOD_ID, "client_lazy");

    @SubscribeEvent
    @SuppressWarnings("resource")
    public static void onAttachCapabilityEvent(AttachCapabilitiesEvent<Entity> event) {
        if (!YesSteveModel.isAvailable()) {
            return;
        }

        Entity entity = event.getObject();
        if (entity instanceof Player player) {
            if (!entity.level().isClientSide()
                && !ICapabilityHolder.of(player).getCapability(ModelInfoCapabilityProvider.MODEL_INFO_CAP).isPresent()
                && !event.getCapabilities().containsKey(MODEL_INFO_CAP)) {
                event.addCapability(MODEL_INFO_CAP, new ModelInfoCapabilityProvider());
            }
            if (!ICapabilityHolder.of(player).getCapability(AuthModelsCapabilityProvider.AUTH_MODELS_CAP).isPresent()
                && !event.getCapabilities().containsKey(AUTH_MODELS_CAP)) {
                event.addCapability(AUTH_MODELS_CAP, new AuthModelsCapabilityProvider());
            }

        } else if (entity instanceof Projectile) {
            if (!entity.level().isClientSide()
                && !ICapabilityHolder.of(entity).getCapability(ProjectileModelInfoCapabilityProvider.CAP).isPresent()
                && !event.getCapabilities().containsKey(PROJECTILE_MODEL_INFO_CAP)) {
                event.addCapability(PROJECTILE_MODEL_INFO_CAP, new ProjectileModelInfoCapabilityProvider());
            }
        } else {
            if (!entity.level().isClientSide()
                && !ICapabilityHolder.of(entity).getCapability(VehicleModelInfoCapabilityProvider.CAP).isPresent()
                && !event.getCapabilities().containsKey(VEHICLE_MODEL_INFO_CAP)) {
                event.addCapability(VEHICLE_MODEL_INFO_CAP, new VehicleModelInfoCapabilityProvider());
            }
        }

        if (FMLEnvironment.dist == Dist.CLIENT && entity.level().isClientSide()) {
            // client-only half in a client class: verifying this common class must not load client types (a
            // dedicated server has none)
            com.elfmcys.ysm.client.event.ClientCapabilityAttach.attach(event, entity, ANIMATABLE_CAP,
                PROJECTILE_ANIMATABLE_CAP, VEHICLE_ANIMATABLE_CAP, CLIENT_LAZY_CAP);
        }
    }

    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event) {
        if (!YesSteveModel.isAvailable()) {
            return;
        }
        ICapabilityHolder.of(event.getOriginal()).reviveYsmCaps();
        LazyOptional<ModelInfoCapability> oldModelInfoCap = getModelInfoCap(event.getOriginal());
        LazyOptional<AuthModelsCapability> oldAuthModelsCap = getAuthModelsCap(event.getOriginal());
        ICapabilityHolder.of(event.getOriginal()).invalidateYsmCaps();

        LazyOptional<ModelInfoCapability> newModelInfoCap = getModelInfoCap(event.getEntity());
        LazyOptional<AuthModelsCapability> newAuthModelsCap = getAuthModelsCap(event.getEntity());

        newModelInfoCap.ifPresent((newModelInfo) -> oldModelInfoCap.ifPresent(newModelInfo::moveFrom));
        newAuthModelsCap.ifPresent((newAuthModels) -> oldAuthModelsCap.ifPresent(newAuthModels::copyFrom));
    }

    @SubscribeEvent
    public static void onTrackingPlayer(PlayerEvent.StartTracking event) {
        if (!YesSteveModel.isAvailable()) {
            return;
        }
        if (event.getTarget() instanceof ServerPlayer trackPlayer) {
            if (event.getEntity() instanceof ServerPlayer observer)
                ServerModelService.current().ifPresent(service -> service.sendTrackingInitial(trackPlayer, observer));
        } else if (event.getTarget() instanceof Projectile projectile) {
            ICapabilityHolder.of(projectile).getCapability(ProjectileModelInfoCapabilityProvider.CAP).ifPresent(cap -> {
                if (cap.isInitialized()) {
                    NetworkHandler.sendToClientPlayer(MinecraftStateHandler.projectile(projectile.getId(), cap),
                        event.getEntity());
                }
            });
        } else if (event.getTarget() != null) {
            ICapabilityHolder.of(event.getTarget()).getCapability(VehicleModelInfoCapabilityProvider.CAP).ifPresent(cap -> {
                if (cap.isInitialized()) {
                    NetworkHandler.sendToClientPlayer(MinecraftStateHandler.vehicle(event.getTarget().getId(), cap),
                        event.getEntity());
                }
            });
        }
    }

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        if (!YesSteveModel.isAvailable()) {
            return;
        }
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            getModelInfoCap(serverPlayer).ifPresent(modelInfoCap -> {
                if (!NetworkHandler.isPlayerChannelPresent(serverPlayer) && !modelInfoCap.isMandatory()) {
                    modelInfoCap.consumeDirty();
                    return;
                }
                modelInfoCap.stopAnimation(serverPlayer);
                PlayerStateHandler.sendEntityInitial(serverPlayer);
                PlayerStateHandler.sendFlags(serverPlayer, serverPlayer);
            });

            getAuthModelsCap(serverPlayer)
                .ifPresent(authModelsCap -> SessionProtocolHandler.refreshGrants(serverPlayer));

        }
    }

    /**
     * 同步客户端服务端数据
     */
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (!YesSteveModel.isAvailable()) {
            return;
        }
        if (event.phase == TickEvent.Phase.END) {
            var players = event.getServer().getPlayerList().getPlayers();
            var lowBandwidthUsage = ServerConfig.LOW_BANDWIDTH_USAGE.get();
            for (ServerPlayer player : players) {
                getModelInfoCap(player).ifPresent(cap -> {
                    if (!NetworkHandler.isPlayerChannelPresent(player) && !cap.isMandatory()) {
                        cap.consumeDirty();
                        return;
                    }
                    if (cap.consumeDirty()) {
                        PlayerStateHandler.publishSelection(player);
                        if (player.getVehicle() != null && player.getVehicle().getFirstPassenger() == player)
                            CapabilityEvent.onVehicleSetModel(player.getVehicle(), player);
                    }
                    if (cap.consumeFlagsDirty())
                        PlayerStateHandler.broadcastFlags(player);
                    cap.getPropertiesTracker().tick(player, true, lowBandwidthUsage);
                });
            }
            ServerModelService.current().ifPresent(service -> {
                if (FMLEnvironment.dist == Dist.DEDICATED_SERVER) {
                    var system = ModelRuntime.system();
                    system.tickCatalog();
                    system.tickServerRuntime();
                }
                service.tick();
            });
        }
    }

    public static void onProjectileSetOwner(Projectile projectile, ServerPlayer owner) {
        ICapabilityHolder.of(owner).getCapability(ModelInfoCapabilityProvider.MODEL_INFO_CAP).ifPresent(ownerCap -> {
            if (!NetworkHandler.isPlayerChannelPresent(owner) && !ownerCap.isMandatory()) {
                return;
            }
            ICapabilityHolder.of(projectile).getCapability(ProjectileModelInfoCapabilityProvider.CAP).ifPresent(cap -> {
                ownerCap.executeWithMolangVars(molangVars -> {
                    cap.init(ownerCap.getModelId(), molangVars);
                    NetworkHandler.broadcastToVisiblePlayers(MinecraftStateHandler.projectile(projectile.getId(), cap),
                        projectile);
                });
            });
        });
    }

    public static void onVehicleSetModel(Entity vehicle, ServerPlayer owner) {
        ICapabilityHolder.of(owner).getCapability(ModelInfoCapabilityProvider.MODEL_INFO_CAP).ifPresent(ownerCap -> {
            if (!NetworkHandler.isPlayerChannelPresent(owner) && !ownerCap.isMandatory()) {
                return;
            }
            ICapabilityHolder.of(vehicle).getCapability(VehicleModelInfoCapabilityProvider.CAP).ifPresent(cap -> {
                // 失败就丢弃
                ownerCap.getMolangVars().ifPresent(molangVars -> {
                    cap.update(ownerCap.getModelId(), molangVars);
                    NetworkHandler.broadcastToVisiblePlayers(MinecraftStateHandler.vehicle(vehicle.getId(), cap),
                        vehicle);
                });
            });
        });
    }

    private static LazyOptional<ModelInfoCapability> getModelInfoCap(Player player) {
        return ICapabilityHolder.of(player).getCapability(ModelInfoCapabilityProvider.MODEL_INFO_CAP);
    }

    private static LazyOptional<AuthModelsCapability> getAuthModelsCap(Player player) {
        return ICapabilityHolder.of(player).getCapability(AuthModelsCapabilityProvider.AUTH_MODELS_CAP);
    }

}
