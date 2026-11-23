# Licensing of this repository

This repository combines two kinds of files.

## Apache License 2.0

Everything not listed below is covered by the Apache License 2.0 ([LICENSE](LICENSE)). That includes:

- the upstream Yes Steve Model 3.0-dev code;
- upstream files changed for this port, which carry a "Modified ... by GuoWQ222" line;
- files copied or moved from upstream;
- `native-patches/`.

The attribution and third-party notices in [NOTICE.md](NOTICE.md) apply.

Built-in model assets have their own licenses (see NOTICE.md). The models included here are CC0.

## All Rights Reserved

The 104 Java files listed below were written new for this port. They are Copyright (c) 2026 GuoWQ222,
All Rights Reserved, and are **not** licensed under the Apache License 2.0. Each one says so in its header.

You may view them on GitHub, and GitHub's Terms of Service let you fork the repository on GitHub. Beyond that, no
permission is granted to copy, modify, build, run or distribute these files, or works that include them, without
written permission from GuoWQ222.

Section 4 of the Apache License 2.0 allows separate terms for modifications like these. The upstream code itself
stays under the Apache License 2.0 and can be used under that license.

- `src/localtest/java/com/elfmcys/ysm/localtest/VisualTestHarness.java`
- `src/main/java/com/elfmcys/ysm/buffer/Memory.java`
- `src/main/java/com/elfmcys/ysm/client/gui/GuiDraw.java`
- `src/main/java/com/elfmcys/ysm/client/gui/pip/GuiQuadRenderState.java`
- `src/main/java/com/elfmcys/ysm/client/gui/pip/GuiScissor.java`
- `src/main/java/com/elfmcys/ysm/client/gui/pip/YsmGuiModelRenderState.java`
- `src/main/java/com/elfmcys/ysm/client/gui/pip/YsmGuiModelRenderer.java`
- `src/main/java/com/elfmcys/ysm/client/gui/pip/YsmGuiModels.java`
- `src/main/java/com/elfmcys/ysm/client/texture/YsmTexture.java`
- `src/main/java/com/elfmcys/ysm/fabric/FabricEventHooks.java`
- `src/main/java/com/elfmcys/ysm/fabric/YsmFabric.java`
- `src/main/java/com/elfmcys/ysm/fabric/YsmFabricClient.java`
- `src/main/java/com/elfmcys/ysm/mixin/fabric/AbstractArrowExtraInfoMixin.java`
- `src/main/java/com/elfmcys/ysm/mixin/fabric/ArrowPotionAccessor.java`
- `src/main/java/com/elfmcys/ysm/mixin/fabric/ConnectionAccessor.java`
- `src/main/java/com/elfmcys/ysm/mixin/fabric/ConnectionCompressionMixin.java`
- `src/main/java/com/elfmcys/ysm/mixin/fabric/EntityWalkDistanceMixin.java`
- `src/main/java/com/elfmcys/ysm/mixin/fabric/LivingEntityEquipmentAccessor.java`
- `src/main/java/com/elfmcys/ysm/mixin/fabric/LivingEntityEventsMixin.java`
- `src/main/java/com/elfmcys/ysm/mixin/fabric/PlayerListLoginMixin.java`
- `src/main/java/com/elfmcys/ysm/mixin/fabric/ProjectileOwnerMixin.java`
- `src/main/java/com/elfmcys/ysm/mixin/fabric/ServerCommonPacketListenerAccessor.java`
- `src/main/java/com/elfmcys/ysm/mixin/fabric/ServerPlayerRidingMixin.java`
- `src/main/java/com/elfmcys/ysm/mixin/fabric/client/BufferBuilderDirectWriteMixin.java`
- `src/main/java/com/elfmcys/ysm/mixin/fabric/client/ByteBufferBuilderAccessor.java`
- `src/main/java/com/elfmcys/ysm/mixin/fabric/client/ClientRespawnCloneMixin.java`
- `src/main/java/com/elfmcys/ysm/mixin/fabric/client/EntityRenderDispatcherReloadMixin.java`
- `src/main/java/com/elfmcys/ysm/mixin/fabric/client/EntityRendererStateMixin.java`
- `src/main/java/com/elfmcys/ysm/mixin/fabric/client/GameRendererProfileMixin.java`
- `src/main/java/com/elfmcys/ysm/mixin/fabric/client/GuiEntityRendererMixin.java`
- `src/main/java/com/elfmcys/ysm/mixin/fabric/client/GuiExtractWaitMixin.java`
- `src/main/java/com/elfmcys/ysm/mixin/fabric/client/GuiGraphicsExtractorAccessor.java`
- `src/main/java/com/elfmcys/ysm/mixin/fabric/client/GuiScreenOpeningMixin.java`
- `src/main/java/com/elfmcys/ysm/mixin/fabric/client/InputEventsMixin.java`
- `src/main/java/com/elfmcys/ysm/mixin/fabric/client/ItemInHandRendererMixin.java`
- `src/main/java/com/elfmcys/ysm/mixin/fabric/client/LevelFrameMixin.java`
- `src/main/java/com/elfmcys/ysm/mixin/fabric/client/LevelRendererFrameMixin.java`
- `src/main/java/com/elfmcys/ysm/mixin/fabric/client/LivingEntityRendererMixin.java`
- `src/main/java/com/elfmcys/ysm/mixin/fabric/client/MouseButtonEventsMixin.java`
- `src/main/java/com/elfmcys/ysm/mixin/fabric/client/PictureInPictureRendererAccessor.java`
- `src/main/java/com/elfmcys/ysm/mixin/fabric/client/ProjectionMatrixBufferMixin.java`
- `src/main/java/com/elfmcys/ysm/mixin/fabric/client/RenderTypeInvoker.java`
- `src/main/java/com/elfmcys/ysm/mixin/fabric/client/ScreenAccessor.java`
- `src/main/java/com/elfmcys/ysm/mixin/platform/EntityCapabilityMixin.java`
- `src/main/java/com/elfmcys/ysm/network/NetworkHandlerClient.java`
- `src/main/java/com/elfmcys/ysm/network/YsmFramePayload.java`
- `src/main/java/com/elfmcys/ysm/platform/Connections.java`
- `src/main/java/com/elfmcys/ysm/platform/Dist.java`
- `src/main/java/com/elfmcys/ysm/platform/DistExecutor.java`
- `src/main/java/com/elfmcys/ysm/platform/EntityWalkDistance.java`
- `src/main/java/com/elfmcys/ysm/platform/FMLPaths.java`
- `src/main/java/com/elfmcys/ysm/platform/OnlyIn.java`
- `src/main/java/com/elfmcys/ysm/platform/PermissionHelper.java`
- `src/main/java/com/elfmcys/ysm/platform/Platform.java`
- `src/main/java/com/elfmcys/ysm/platform/ServerHolder.java`
- `src/main/java/com/elfmcys/ysm/platform/ShieldBlockCooldownHolder.java`
- `src/main/java/com/elfmcys/ysm/platform/capability/AttachCapabilitiesEvent.java`
- `src/main/java/com/elfmcys/ysm/platform/capability/Capability.java`
- `src/main/java/com/elfmcys/ysm/platform/capability/CapabilityDispatcher.java`
- `src/main/java/com/elfmcys/ysm/platform/capability/CapabilityManager.java`
- `src/main/java/com/elfmcys/ysm/platform/capability/CapabilityToken.java`
- `src/main/java/com/elfmcys/ysm/platform/capability/ICapabilityHolder.java`
- `src/main/java/com/elfmcys/ysm/platform/capability/ICapabilityProvider.java`
- `src/main/java/com/elfmcys/ysm/platform/capability/ICapabilitySerializable.java`
- `src/main/java/com/elfmcys/ysm/platform/capability/LazyOptional.java`
- `src/main/java/com/elfmcys/ysm/platform/event/Cancelable.java`
- `src/main/java/com/elfmcys/ysm/platform/event/Event.java`
- `src/main/java/com/elfmcys/ysm/platform/event/EventBus.java`
- `src/main/java/com/elfmcys/ysm/platform/event/EventPriority.java`
- `src/main/java/com/elfmcys/ysm/platform/event/IEventBus.java`
- `src/main/java/com/elfmcys/ysm/platform/event/MinecraftForge.java`
- `src/main/java/com/elfmcys/ysm/platform/event/Mod.java`
- `src/main/java/com/elfmcys/ysm/platform/event/SubscribeEvent.java`
- `src/main/java/com/elfmcys/ysm/platform/event/Subscribers.java`
- `src/main/java/com/elfmcys/ysm/platform/event/forge/AddReloadListenerEvent.java`
- `src/main/java/com/elfmcys/ysm/platform/event/forge/ClientPlayerNetworkEvent.java`
- `src/main/java/com/elfmcys/ysm/platform/event/forge/EntityJoinLevelEvent.java`
- `src/main/java/com/elfmcys/ysm/platform/event/forge/EntityLeaveLevelEvent.java`
- `src/main/java/com/elfmcys/ysm/platform/event/forge/FMLClientSetupEvent.java`
- `src/main/java/com/elfmcys/ysm/platform/event/forge/FMLCommonSetupEvent.java`
- `src/main/java/com/elfmcys/ysm/platform/event/forge/FMLLoadCompleteEvent.java`
- `src/main/java/com/elfmcys/ysm/platform/event/forge/IModBusEvent.java`
- `src/main/java/com/elfmcys/ysm/platform/event/forge/InputEvent.java`
- `src/main/java/com/elfmcys/ysm/platform/event/forge/LevelEvent.java`
- `src/main/java/com/elfmcys/ysm/platform/event/forge/LivingEvent.java`
- `src/main/java/com/elfmcys/ysm/platform/event/forge/MobEffectEvent.java`
- `src/main/java/com/elfmcys/ysm/platform/event/forge/PlayerEvent.java`
- `src/main/java/com/elfmcys/ysm/platform/event/forge/RegisterCommandsEvent.java`
- `src/main/java/com/elfmcys/ysm/platform/event/forge/RegisterKeyMappingsEvent.java`
- `src/main/java/com/elfmcys/ysm/platform/event/forge/ScreenEvent.java`
- `src/main/java/com/elfmcys/ysm/platform/event/forge/ServerAboutToStartEvent.java`
- `src/main/java/com/elfmcys/ysm/platform/event/forge/ServerStoppingEvent.java`
- `src/main/java/com/elfmcys/ysm/platform/event/forge/ShieldBlockEvent.java`
- `src/main/java/com/elfmcys/ysm/platform/event/forge/TickEvent.java`
- `src/main/java/com/elfmcys/ysm/platform/input/KeyConflictContext.java`
- `src/main/java/com/elfmcys/ysm/platform/input/KeyModifier.java`
- `src/main/java/com/elfmcys/ysm/platform/input/YsmKeyMapping.java`
- `src/main/java/com/elfmcys/ysm/platform/network/NetworkDirection.java`
- `src/main/java/com/elfmcys/ysm/platform/network/NetworkEvent.java`
- `src/main/java/com/elfmcys/ysm/platform/network/PacketTarget.java`
- `src/main/java/com/elfmcys/ysm/platform/render/EntitySubmitHelper.java`
- `src/main/java/com/elfmcys/ysm/platform/render/YsmRenderMatrices.java`
- `src/main/java/com/elfmcys/ysm/platform/render/YsmRenderStateKeys.java`
- `src/verification/test/java/com/elfmcys/ysm/testsupport/FabricTestEnvironment.java`
