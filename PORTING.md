# Porting notes: Yes Steve Model 3.0-dev to Minecraft 26.x (Fabric, NeoForge, Forge)

The base is [YesSteveModel/YesSteveModel](https://github.com/YesSteveModel/YesSteveModel) `dev/1.20` at
`3ec7068136f9a6a07d0eb227d69b20b8bcc00d85` (Forge 1.20.1, Apache-2.0). This file describes what the port changes and lists every file that differs
from that commit.

## Build

- One Gradle build makes all 15 jars: `-Pmc=26.1|26.1.1|26.1.2|26.2|26.3` and `-Ploader=fabric|neoforge|forge`. The
  default is Fabric 26.2.
  - `versions/<mc>.properties` holds each version's dependency versions and ranges. They are applied over
    `gradle.properties`.
  - `gradle/loader-<loader>.gradle` holds each loader's setup.
- Loader plugins:
  - Fabric: Fabric Loom;
  - NeoForge: ModDevGradle;
  - Forge: ForgeGradle 7.
- Java 25 on every version and loader.
- The checked-in sources are the 26.2 variant. Other versions compile a copy made by a line-preserving preprocessor
  (`buildSrc`, `PreprocessTask`):
  - `//#if MC < 26.2` blocks select version-specific code, and inactive lines carry a `//$$ ` marker;
  - `versions/*.remap` rename tables map moved classes per version and loader.
- `ForgeConfigSpec`:
  - on Fabric and NeoForge it comes from Forge Config API Port, which is bundled in the jar;
  - Forge has it built in.
- Release jars relocate every bundled library under `com.elfmcys`. A build check fails on any class outside it, and on
  Fabric also on a nested jar not listed in `fabric.mod.json`.
- The `declaration` source set is part of the mod in dev runs.
- A dev-only harness mod lives in `src/localtest`. It drives scripted scenarios, takes screenshots, and runs
  model-switching soaks, multiplayer, integration and model-collection checks.
- Upstream tests that need Forge-era Minecraft APIs or the Wine Fox models are listed in
  `gradle/test-exclusions.txt`.

## Loaders

Fabric, NeoForge and Forge each have a small glue package. Each one posts the same Forge-shaped platform events at the
same points in the game loop, so the YSM code above them is shared:

- `fabric/` is the original target;
- `neoforge/` and `forge/` hold an entry point, event hooks, client event hooks and a network channel.

Code that differs only slightly between loaders uses branches in the platform layer (`Platform`).

- **Mixins:** the mixins are shared, except for a few loader-only ones:
  - Fabric event mixins;
  - Forge's sound-stream mixin, because Forge 26 has no stream hook on `SoundInstance`.
- **Client code on dedicated servers:** classes used on both sides never reference client classes. A dedicated
  server's JVM loads the types a method uses when it verifies the class, so client-only code lives in client classes
  such as `client/event/ClientCapabilityAttach`.

## Platform layer (`com.elfmcys.ysm.platform`)

The upstream code is written against Forge. Instead of rewriting every caller, the port provides Forge-shaped pieces:

- an event bus with separate mod and game buses;
- entity capabilities, injected into `Entity`;
- `DistExecutor`;
- network contexts and packet targets.

On Fabric, `fabric/FabricEventHooks` and the event mixins post these events from Fabric API callbacks. On NeoForge and
Forge, the `neoforge/` and `forge/` event hooks translate the loader's own events. Each one is posted at the same
point in the game loop where Forge 1.20 posts it:

- **Client setup:**
  - Fabric: runs when the client has started. Fabric client initializers run inside the `Minecraft` constructor,
    before the window and GPU device exist.
  - NeoForge and Forge: runs once after the first frame. Work queued during their setup events was never run.
- **Player login:** posted at the end of `PlayerList.placeNewPlayer`, after the player joined its level.
- **Key and mouse-button input, screen opening and client respawn:** posted from mixins at the same places as Forge.

## Networking

YSM's own frames travel in one custom payload, `YsmFramePayload`:
- on Fabric through Fabric's networking API;
- on NeoForge and Forge through an optional channel, so vanilla clients and servers can still connect.

## Rendering

- 26.x renders in three phases: extract, submit, deferred draw.
  - YSM models are submitted as custom geometry.
  - The native renderer writes vertices straight into the `BufferBuilder` during the deferred draw.
  - The projection matrix is captured when the game uploads it.
- Extracted render states carry the entity, so YSM can reach its animation state.
- Parallel animation updates:
  - they start with level extraction;
  - they are joined before GUI extraction, because 26.x extracts the GUI before it draws the level;
  - they are closed after the level render.
- Netty's `Unsafe` memory access is disabled on Java 24+, so native memory copies use `java.lang.foreign`.

## GUI

- Screens and widgets use the 26.x render-state extraction API.
- Model previews are picture-in-picture renderers. Thumbnails are read back asynchronously.
- Entity previews such as the inventory and the model screen get their inventory angles applied to the entity during
  the draw, as 1.20 did.
- 26.1.x picture-in-picture renderers flush their buffers only after the model has been drawn and its GUI transform
  removed. YSM draws its own batch while the transform still applies; otherwise the HUD player preview was drawn off
  the picture in third-person view.

## Behaviour aligned with YSM 2.6.5

- Models without `origin_ver` are treated as `2.6.5` models.
- The Steve and Alex built-ins use the player's skin.
- Item classes:
  - `mace` comes right after sword;
  - `lance` is used for 26.x spears (tag `ysm:pike`);
  - item tags live in the 26.x `tags/item` folder and use `c:` convention tags.
- Vehicles:
  - riders sit at the model's `PassengerLocator` bones, and vehicle models turn the way 2.6.5 turns them;
  - vehicle and projectile models are drawn at scale 1.0 (3.0-dev used 0.7);
  - the 1.20 ids `minecraft:boat` and `minecraft:chest_boat` match every 26.x boat and chest boat type.
- Server settings `AcceptSoundFX` and `PlayerSyncTimeout` exist again.
- WTHIT and Jade show the YSM model of the player or vehicle you look at. 2.6.5 did this through Jade and The One
  Probe.

## Integrations

Ported from upstream's Forge integrations to the mods' 26.x APIs, on every loader where the mod exists. Each mod's
classes are only used from inner classes that load when the loader reports the mod. README.md lists the builds on
which each integration was tested.

- Iris: YSM writes Iris's entity vertex layout (`IRIS_56_V2`) from the native renderer, so models get shaders.
- WTHIT and Jade: a `YSM Model` tooltip line for players and vehicles.
- First-person Model: the camera follows the model's head height; the model's body is drawn in first person.
- Real Camera: while it is active, the full model is drawn in first person. Its default binding mode looks for the
  vanilla player model, so with a YSM model use its classic mode.
- Better Combat 3.x: an attack starts a synced vanilla swing, and the running attack animation's name is available
  to models. Better Combat now animates through Player Animation Library instead of playerAnimator.
- Player Animation Library: while an external animation asks for the third-person model in first person, the full
  model is drawn there.
- Carry On: `carryon:block`, `carryon:entity` and `carryon:player` animations and the Carry On Molang values.
- Immersive Melodies: melody progress for the `ctrl:im_*` Molang functions.
- Immersive Aircraft: YSM vehicle models follow the aircraft's pitch, roll and wind sway.

## Fixes

- Settings changed in YSM's screens are saved. Forge's config files saved on every change; under Forge Config API
  Port a change stays in memory until it is saved, so the config screen, its checkboxes, the disclaimer, the
  extra-player screen and the loading-indicator position were lost on restart.
- The optional-mod linkage check (`OptionalLinkageVerifier`, upstream) rejected methods that a compat class
  inherits instead of declaring, such as an enum's `name()`. That switched the whole Carry On integration off. It
  now resolves methods through superclasses and interfaces, as the JVM does.
- A published model that is still being converted when a player joins counts as pending, not denied.

## Not included or not yet working

- The Wine Fox built-in models. They are CC BY-NC-SA 4.0 or All Rights Reserved and are not redistributed.
- The jars of Forge mods that upstream compiled against (FirstPerson, NotEnoughAnimations, Oculus, Superb Warfare).
- Integrations whose mods have no 26.x version (Elytra Slot, Trinkets, Accessories and the Forge 1.20-only mods).
  Their compat modules remain as declared Molang stand-ins.
- On Forge, when another mod fails to load, the game can crash in a YSM mixin instead of showing Forge's error
  screen (seen in development runs).

## Tests

The suite passes on the author's Windows machine (Chinese locale). These tests failed before, each for a test or
environment problem, not a product bug, and were fixed:

- `ClientConfigDeviceDefaultsTest`, `ServerConfigSpecTest`: they wrote their files with `CommentedFileConfig.of()`,
  which the night-config version bundled with Forge Config API Port saves asynchronously. They write synchronously now.
- `NativeRuntimeDeviceIntegrationTest`: its child JVM printed in the console code page (`stdout.encoding`), not
  UTF-8, wrote its config asynchronously, and lacked the Fabric launcher stand-in the test JVM has.
- `ModelExporterTest`: its fixture declares `origin_ver`, and a declared `origin_ver` takes precedence over the
  compile default by design, so the test could not force other levels. It compiles a copy without `origin_ver`.
- `ConvertedObjectStoreTest`: it expected `3.0.0` as the fixture's origin, but the built-in default declares its own.
- `CatalogSourceReuseTest`: `solid.7z` is an archive of an older `1_alex`, packed from a CRLF checkout. The other
  forms are now built from the archive's own files.
- `ReloadableModelCatalogTest`: an elevated Windows account with the backup privilege enabled (for example in an SSH
  session) can still list a folder it is denied, so the denied-folder case is skipped there; it runs and passes in a
  desktop session. Temp paths are made canonical for 8.3 TEMP folders such as `C:\Users\ADMINI~1`.

`RawModelImporterTest` expects the port's default: a missing `origin_ver` means `2.6.5`.

## Native library

`native-patches/port.patch` applies to YesSteveModel-Native `9d92c175cb3c4b8db4ecb334281bd13d0b224f47`. It contains:

- `/bigobj` for MSVC;
- two MSVC compile fixes;
- a unique tag per JNI entry point. `__COUNTER__` restarts in every translation unit, so MSVC merged entry points with
  equal signatures, and one native function ran another;
- the Iris 1.11 entity vertex layout on 26.x (`VertexKind::kIris56V2`, Java `IRIS_56_V2`): 56 bytes, with
  `iris_Entity` as four unsigned shorts, the mid-texture coordinate at byte 44 and the tangent at byte 52.

## Modified files

Java, Gradle and properties files in this list carry a notice on their first line. The others (JSON) are only listed
here.

- `build.gradle`
- `buildSrc/build.gradle`
- `docs/architecture/model-management/storage/preferences.md`
- `docs/architecture/network/player-state.md`
- `docs/standards/protocol-v1/README.md`
- `gradle.properties`
- `gradle/builtin-index.gradle`
- `gradle/declaration.gradle`
- `gradle/native.gradle`
- `gradle/schema.gradle`
- `gradle/wrapper/gradle-wrapper.properties`
- `settings.gradle`
- `src/forge-compat-reference/com/elfmcys/ysm/event/InterModMsg.java`
- `src/main/java/com/elfmcys/ysm/AssetPaths.java`
- `src/main/java/com/elfmcys/ysm/YesSteveModel.java`
- `src/main/java/com/elfmcys/ysm/accessor/IArrowExtraInfo.java`
- `src/main/java/com/elfmcys/ysm/api/internal/event/ExtensionEventHandlerLoader.java`
- `src/main/java/com/elfmcys/ysm/api/model/event/RegisterModelLocatorEvent.java`
- `src/main/java/com/elfmcys/ysm/api/rendering/event/RenderLayerEvent.java`
- `src/main/java/com/elfmcys/ysm/api/rendering/event/RenderModelEvent.java`
- `src/main/java/com/elfmcys/ysm/buffer/BorrowedNativeBuffer.java`
- `src/main/java/com/elfmcys/ysm/buffer/MappedFileBuffer.java`
- `src/main/java/com/elfmcys/ysm/buffer/NativeNioBuffer.java`
- `src/main/java/com/elfmcys/ysm/buffer/UniBufferIO.java`
- `src/main/java/com/elfmcys/ysm/capability/AuthModelsCapability.java`
- `src/main/java/com/elfmcys/ysm/capability/AuthModelsCapabilityProvider.java`
- `src/main/java/com/elfmcys/ysm/capability/ClientLazyCapabilityProvider.java`
- `src/main/java/com/elfmcys/ysm/capability/ClientRoamingSession.java`
- `src/main/java/com/elfmcys/ysm/capability/ModelInfoCapability.java`
- `src/main/java/com/elfmcys/ysm/capability/ModelInfoCapabilityProvider.java`
- `src/main/java/com/elfmcys/ysm/capability/PendingRemoteReplay.java`
- `src/main/java/com/elfmcys/ysm/capability/PlayerAnimatableCapability.java`
- `src/main/java/com/elfmcys/ysm/capability/PlayerAnimatableCapabilityProvider.java`
- `src/main/java/com/elfmcys/ysm/capability/PlayerStateTracker.java`
- `src/main/java/com/elfmcys/ysm/capability/ProjectileAnimatableCapability.java`
- `src/main/java/com/elfmcys/ysm/capability/ProjectileAnimatableCapabilityProvider.java`
- `src/main/java/com/elfmcys/ysm/capability/ProjectileModelInfoCapability.java`
- `src/main/java/com/elfmcys/ysm/capability/ProjectileModelInfoCapabilityProvider.java`
- `src/main/java/com/elfmcys/ysm/capability/ServerDrivenPlayerPropertiesTracker.java`
- `src/main/java/com/elfmcys/ysm/capability/VehicleAnimatableCapability.java`
- `src/main/java/com/elfmcys/ysm/capability/VehicleAnimatableCapabilityProvider.java`
- `src/main/java/com/elfmcys/ysm/capability/VehicleModelInfoCapability.java`
- `src/main/java/com/elfmcys/ysm/capability/VehicleModelInfoCapabilityProvider.java`
- `src/main/java/com/elfmcys/ysm/client/ClientStartup.java`
- `src/main/java/com/elfmcys/ysm/client/animation/AnimationParallelTicker.java`
- `src/main/java/com/elfmcys/ysm/client/animation/condition/ConditionArmor.java`
- `src/main/java/com/elfmcys/ysm/client/animation/condition/ConditionTAC.java`
- `src/main/java/com/elfmcys/ysm/client/animation/condition/ConditionalHold.java`
- `src/main/java/com/elfmcys/ysm/client/animation/condition/ConditionalPassenger.java`
- `src/main/java/com/elfmcys/ysm/client/animation/condition/ConditionalSwing.java`
- `src/main/java/com/elfmcys/ysm/client/animation/condition/ConditionalUse.java`
- `src/main/java/com/elfmcys/ysm/client/animation/condition/ConditionalVehicle.java`
- `src/main/java/com/elfmcys/ysm/client/animation/condition/InnerClassify.java`
- `src/main/java/com/elfmcys/ysm/client/animation/debug/CustomDebugSource.java`
- `src/main/java/com/elfmcys/ysm/client/animation/predicate/GunFirePredicate.java`
- `src/main/java/com/elfmcys/ysm/client/animation/predicate/MainhandPredicate.java`
- `src/main/java/com/elfmcys/ysm/client/animation/predicate/OffhandPredicate.java`
- `src/main/java/com/elfmcys/ysm/client/animation/predicate/ProjectileMainPredicate.java`
- `src/main/java/com/elfmcys/ysm/client/animation/predicate/SwingPredicate.java`
- `src/main/java/com/elfmcys/ysm/client/animation/predicate/VehiclePredicate.java`
- `src/main/java/com/elfmcys/ysm/client/command/ClientRootCommand.java`
- `src/main/java/com/elfmcys/ysm/client/command/ResourceDebugCommand.java`
- `src/main/java/com/elfmcys/ysm/client/command/sub/ClientCacheCommand.java`
- `src/main/java/com/elfmcys/ysm/client/command/sub/DebugCommand.java`
- `src/main/java/com/elfmcys/ysm/client/command/sub/MolangCommand.java`
- `src/main/java/com/elfmcys/ysm/client/command/sub/RoamingCommand.java`
- `src/main/java/com/elfmcys/ysm/client/command/sub/SimpleWatchCommand.java`
- `src/main/java/com/elfmcys/ysm/client/compat/ARCompat.java`
- `src/main/java/com/elfmcys/ysm/client/compat/CosmeticArmorCompat.java`
- `src/main/java/com/elfmcys/ysm/client/compat/ElytraSlotCompat.java`
- `src/main/java/com/elfmcys/ysm/client/compat/FirstPersonCompat.java`
- `src/main/java/com/elfmcys/ysm/client/compat/ImmersiveAircraftCompat.java`
- `src/main/java/com/elfmcys/ysm/client/compat/IrisCompat.java`
- `src/main/java/com/elfmcys/ysm/client/compat/OptifineCompat.java`
- `src/main/java/com/elfmcys/ysm/client/compat/PlayerAnimatorCompat.java`
- `src/main/java/com/elfmcys/ysm/client/compat/SimplePlaneCompat.java`
- `src/main/java/com/elfmcys/ysm/client/compat/backpack/sophisticated/SophisticatedBuiltinComponent.java`
- `src/main/java/com/elfmcys/ysm/client/compat/backpack/sophisticated/SophisticatedCompat.java`
- `src/main/java/com/elfmcys/ysm/client/compat/bettercombat/BetterCombatCompat.java`
- `src/main/java/com/elfmcys/ysm/client/compat/bettercombat/BetterCombatCompatInner.java`
- `src/main/java/com/elfmcys/ysm/client/compat/bettercombat/event/PlayerAttackEvent.java`
- `src/main/java/com/elfmcys/ysm/client/compat/carryon/CarryOnCompat.java`
- `src/main/java/com/elfmcys/ysm/client/compat/create/CreateBuiltinComponent.java`
- `src/main/java/com/elfmcys/ysm/client/compat/create/CreateCompat.java`
- `src/main/java/com/elfmcys/ysm/client/compat/curios/CuriosBuiltins.java`
- `src/main/java/com/elfmcys/ysm/client/compat/curios/CuriosCompat.java`
- `src/main/java/com/elfmcys/ysm/client/compat/immersivemelodies/ImmersiveMelodiesCompat.java`
- `src/main/java/com/elfmcys/ysm/client/compat/ironsspellbooks/IronsSpellBooksBuiltinComponent.java`
- `src/main/java/com/elfmcys/ysm/client/compat/ironsspellbooks/IronsSpellBooksCompat.java`
- `src/main/java/com/elfmcys/ysm/client/compat/parcool/ParCoolBuiltinComponent.java`
- `src/main/java/com/elfmcys/ysm/client/compat/parcool/ParCoolCompat.java`
- `src/main/java/com/elfmcys/ysm/client/compat/realcamera/RealCameraCompat.java`
- `src/main/java/com/elfmcys/ysm/client/compat/simplehat/SimpleHatsCompat.java`
- `src/main/java/com/elfmcys/ysm/client/compat/slashblade/SlashBladeBuiltinComponent.java`
- `src/main/java/com/elfmcys/ysm/client/compat/slashblade/SlashBladeCompat.java`
- `src/main/java/com/elfmcys/ysm/client/compat/slashblade/SlashBladeRender.java`
- `src/main/java/com/elfmcys/ysm/client/compat/swarfare/SWarfareCompat.java`
- `src/main/java/com/elfmcys/ysm/client/compat/swem/SwemBuiltinComponent.java`
- `src/main/java/com/elfmcys/ysm/client/compat/swem/SwemCompat.java`
- `src/main/java/com/elfmcys/ysm/client/compat/tacz/TACZCompat.java`
- `src/main/java/com/elfmcys/ysm/client/compat/tacz/TaczBuiltinComponent.java`
- `src/main/java/com/elfmcys/ysm/client/controller/ArmorControllerDiscovery.java`
- `src/main/java/com/elfmcys/ysm/client/demand/ClientDemandCoordinator.java`
- `src/main/java/com/elfmcys/ysm/client/demand/ModelResourceDemand.java`
- `src/main/java/com/elfmcys/ysm/client/entity/AnimatableEntity.java`
- `src/main/java/com/elfmcys/ysm/client/entity/CustomFirstPersonArmEntity.java`
- `src/main/java/com/elfmcys/ysm/client/entity/CustomHumanoidEntity.java`
- `src/main/java/com/elfmcys/ysm/client/entity/CustomProjectileEntity.java`
- `src/main/java/com/elfmcys/ysm/client/entity/CustomVehicleEntity.java`
- `src/main/java/com/elfmcys/ysm/client/event/ClientCommandHandler.java`
- `src/main/java/com/elfmcys/ysm/client/event/ClientLoggedEvent.java`
- `src/main/java/com/elfmcys/ysm/client/event/ClientSetupEvent.java`
- `src/main/java/com/elfmcys/ysm/client/event/ClientTickEvent.java`
- `src/main/java/com/elfmcys/ysm/client/event/DownloadScreenInterModEvent.java`
- `src/main/java/com/elfmcys/ysm/client/event/EntityLoadEvent.java`
- `src/main/java/com/elfmcys/ysm/client/event/LocalPlayerRespawnEvent.java`
- `src/main/java/com/elfmcys/ysm/client/event/ModInputEvent.java`
- `src/main/java/com/elfmcys/ysm/client/event/PlayerMoveEvent.java`
- `src/main/java/com/elfmcys/ysm/client/event/RegisterEntityRenderersEvent.java`
- `src/main/java/com/elfmcys/ysm/client/event/RenderFirstPlayerBackground.java`
- `src/main/java/com/elfmcys/ysm/client/event/ReplacePlayerHandRenderEvent.java`
- `src/main/java/com/elfmcys/ysm/client/event/ReplacePlayerRenderEvent.java`
- `src/main/java/com/elfmcys/ysm/client/event/VanillaPlayerRenderEvent.java`
- `src/main/java/com/elfmcys/ysm/client/gui/AndroidCompat.java`
- `src/main/java/com/elfmcys/ysm/client/gui/AnimationRouletteScreen.java`
- `src/main/java/com/elfmcys/ysm/client/gui/ConfigScreen.java`
- `src/main/java/com/elfmcys/ysm/client/gui/DisclaimerScreen.java`
- `src/main/java/com/elfmcys/ysm/client/gui/DownloadScreen.java`
- `src/main/java/com/elfmcys/ysm/client/gui/ExtraPlayerConfigScreen.java`
- `src/main/java/com/elfmcys/ysm/client/gui/GuiFrameInput.java`
- `src/main/java/com/elfmcys/ysm/client/gui/GuiModelFraming.java`
- `src/main/java/com/elfmcys/ysm/client/gui/ModelInfoScreen.java`
- `src/main/java/com/elfmcys/ysm/client/gui/ModelScreen.java`
- `src/main/java/com/elfmcys/ysm/client/gui/OpenModelFolderScreen.java`
- `src/main/java/com/elfmcys/ysm/client/gui/PlayerModelScreen.java`
- `src/main/java/com/elfmcys/ysm/client/gui/PlayerTextureScreen.java`
- `src/main/java/com/elfmcys/ysm/client/gui/PreviewRenderHost.java`
- `src/main/java/com/elfmcys/ysm/client/gui/button/AuthorButton.java`
- `src/main/java/com/elfmcys/ysm/client/gui/button/CatalogModelButton.java`
- `src/main/java/com/elfmcys/ysm/client/gui/button/CatalogTextureButton.java`
- `src/main/java/com/elfmcys/ysm/client/gui/button/ConfigCheckBox.java`
- `src/main/java/com/elfmcys/ysm/client/gui/button/FailedCatalogModelButton.java`
- `src/main/java/com/elfmcys/ysm/client/gui/button/FlatCheckbox.java`
- `src/main/java/com/elfmcys/ysm/client/gui/button/FlatColorButton.java`
- `src/main/java/com/elfmcys/ysm/client/gui/button/FlatIconButton.java`
- `src/main/java/com/elfmcys/ysm/client/gui/button/FlatRatioBox.java`
- `src/main/java/com/elfmcys/ysm/client/gui/button/FlatSlider.java`
- `src/main/java/com/elfmcys/ysm/client/gui/button/PackButton.java`
- `src/main/java/com/elfmcys/ysm/client/gui/button/PositionButton.java`
- `src/main/java/com/elfmcys/ysm/client/gui/button/StarButton.java`
- `src/main/java/com/elfmcys/ysm/client/gui/overlay/DebugAnimationScreen.java`
- `src/main/java/com/elfmcys/ysm/client/gui/overlay/ExtraPlayerScreen.java`
- `src/main/java/com/elfmcys/ysm/client/gui/overlay/LoadingStateScreen.java`
- `src/main/java/com/elfmcys/ysm/client/input/AnimationRouletteKey.java`
- `src/main/java/com/elfmcys/ysm/client/input/DebugAnimationKey.java`
- `src/main/java/com/elfmcys/ysm/client/input/ExtraAnimationKey.java`
- `src/main/java/com/elfmcys/ysm/client/input/ExtraPlayerConfigKey.java`
- `src/main/java/com/elfmcys/ysm/client/input/PlayerModelScreenKey.java`
- `src/main/java/com/elfmcys/ysm/client/model/locator/FirstPersonLocator.java`
- `src/main/java/com/elfmcys/ysm/client/model/locator/PlayerLocator.java`
- `src/main/java/com/elfmcys/ysm/client/model/locator/ProjectileLocator.java`
- `src/main/java/com/elfmcys/ysm/client/model/locator/VehicleLocator.java`
- `src/main/java/com/elfmcys/ysm/client/particle/ParticleSpawner.java`
- `src/main/java/com/elfmcys/ysm/client/renderer/CustomFirstPersonArmRenderer.java`
- `src/main/java/com/elfmcys/ysm/client/renderer/CustomPlayerRenderer.java`
- `src/main/java/com/elfmcys/ysm/client/renderer/CustomProjectileRenderer.java`
- `src/main/java/com/elfmcys/ysm/client/renderer/CustomVehicleRenderer.java`
- `src/main/java/com/elfmcys/ysm/client/renderer/layer/CustomParrotOnShoulderLayer.java`
- `src/main/java/com/elfmcys/ysm/client/renderer/layer/CustomPlayerElytraLayer.java`
- `src/main/java/com/elfmcys/ysm/client/renderer/layer/CustomPlayerHeadLayer.java`
- `src/main/java/com/elfmcys/ysm/client/renderer/layer/CustomPlayerItemInHandLayer.java`
- `src/main/java/com/elfmcys/ysm/client/renderer/replace/EntityRendererReplace.java`
- `src/main/java/com/elfmcys/ysm/client/renderer/replace/FishingHookRendererReplace.java`
- `src/main/java/com/elfmcys/ysm/client/renderer/replace/ProjectileRendererReplace.java`
- `src/main/java/com/elfmcys/ysm/client/sound/instance/CustomSoundInstance.java`
- `src/main/java/com/elfmcys/ysm/client/sound/instance/SoundInstanceManager.java`
- `src/main/java/com/elfmcys/ysm/client/texture/CustomPBRTextureSet.java`
- `src/main/java/com/elfmcys/ysm/client/texture/CustomTexture.java`
- `src/main/java/com/elfmcys/ysm/client/texture/CustomTextureManager.java`
- `src/main/java/com/elfmcys/ysm/client/texture/TextureHolder.java`
- `src/main/java/com/elfmcys/ysm/command/CommandArguments.java`
- `src/main/java/com/elfmcys/ysm/command/sub/AuthCommand.java`
- `src/main/java/com/elfmcys/ysm/command/sub/CacheCommand.java`
- `src/main/java/com/elfmcys/ysm/command/sub/ExportCommand.java`
- `src/main/java/com/elfmcys/ysm/command/sub/ModelCommand.java`
- `src/main/java/com/elfmcys/ysm/command/sub/PingCommand.java`
- `src/main/java/com/elfmcys/ysm/command/sub/PlayAnimationCommand.java`
- `src/main/java/com/elfmcys/ysm/config/ServerConfig.java`
- `src/main/java/com/elfmcys/ysm/event/CapabilityEvent.java`
- `src/main/java/com/elfmcys/ysm/event/CommandRegistry.java`
- `src/main/java/com/elfmcys/ysm/event/CommonEvent.java`
- `src/main/java/com/elfmcys/ysm/event/LivingShieldBlockEvent.java`
- `src/main/java/com/elfmcys/ysm/event/LoggedOutEvent.java`
- `src/main/java/com/elfmcys/ysm/event/LoginEvent.java`
- `src/main/java/com/elfmcys/ysm/event/MobEffectSyncEvent.java`
- `src/main/java/com/elfmcys/ysm/event/ServerStartingEvent.java`
- `src/main/java/com/elfmcys/ysm/event/api/SpecialPlayerRenderEvent.java`
- `src/main/java/com/elfmcys/ysm/expression/aot/RenderTargetExpressionModuleFactory.java`
- `src/main/java/com/elfmcys/ysm/expression/host/MolangExtensionLoader.java`
- `src/main/java/com/elfmcys/ysm/expression/host/OptionalLinkageVerifier.java`
- `src/main/java/com/elfmcys/ysm/expression/host/builtin/ControllerPredicateBuiltins.java`
- `src/main/java/com/elfmcys/ysm/expression/host/builtin/DebugBuiltins.java`
- `src/main/java/com/elfmcys/ysm/expression/host/builtin/EntityBuiltins.java`
- `src/main/java/com/elfmcys/ysm/expression/host/builtin/InputBuiltins.java`
- `src/main/java/com/elfmcys/ysm/expression/host/builtin/LivingBuiltins.java`
- `src/main/java/com/elfmcys/ysm/expression/host/builtin/ModBuiltins.java`
- `src/main/java/com/elfmcys/ysm/expression/host/builtin/ProjectileBuiltins.java`
- `src/main/java/com/elfmcys/ysm/expression/host/builtin/RenderBuiltins.java`
- `src/main/java/com/elfmcys/ysm/expression/host/builtin/WorldBuiltins.java`
- `src/main/java/com/elfmcys/ysm/expression/inventory/ModelProgramInventory.java`
- `src/main/java/com/elfmcys/ysm/format/parser/CapturedModel.java`
- `src/main/java/com/elfmcys/ysm/format/parser/ModelParser.java`
- `src/main/java/com/elfmcys/ysm/format/parser/RawModelAssembler.java`
- `src/main/java/com/elfmcys/ysm/format/schema/model/ModelFileWriter.java`
- `src/main/java/com/elfmcys/ysm/format/schema/model/ModelOriginVersion.java`
- `src/main/java/com/elfmcys/ysm/format/schema/model/views/ModelInfoView.java`
- `src/main/java/com/elfmcys/ysm/geckolib3/geo/CustomTranslucentRenderType.java`
- `src/main/java/com/elfmcys/ysm/geckolib3/geo/GeoEntityRenderer.java`
- `src/main/java/com/elfmcys/ysm/geckolib3/geo/GeoLayerRenderer.java`
- `src/main/java/com/elfmcys/ysm/geckolib3/geo/GeoProjectilesRenderer.java`
- `src/main/java/com/elfmcys/ysm/geckolib3/geo/GeoRenderData.java`
- `src/main/java/com/elfmcys/ysm/geckolib3/geo/GeoReplacedEntityRenderer.java`
- `src/main/java/com/elfmcys/ysm/geckolib3/geo/IGeoRenderer.java`
- `src/main/java/com/elfmcys/ysm/geckolib3/geo/exception/GeckoLibException.java`
- `src/main/java/com/elfmcys/ysm/geckolib3/geo/render/built/GeoLocatorType.java`
- `src/main/java/com/elfmcys/ysm/geckolib3/geo/render/built/GeoLocatorTypeRegistry.java`
- `src/main/java/com/elfmcys/ysm/geckolib3/util/AnimationUtils.java`
- `src/main/java/com/elfmcys/ysm/info/type/PBRTextureType.java`
- `src/main/java/com/elfmcys/ysm/init/ModItemTags.java`
- `src/main/java/com/elfmcys/ysm/init/ModSounds.java`
- `src/main/java/com/elfmcys/ysm/mixin/AbstractArrowEntityMixin.java`
- `src/main/java/com/elfmcys/ysm/mixin/client/ArrowEntityAccessor.java`
- `src/main/java/com/elfmcys/ysm/mixin/client/BufferSourceMixin.java`
- `src/main/java/com/elfmcys/ysm/mixin/client/InventoryScreenMixin.java`
- `src/main/java/com/elfmcys/ysm/mixin/client/TextureManagerMixin.java`
- `src/main/java/com/elfmcys/ysm/mixin/client/ThrowableItemProjectileAccessor.java`
- `src/main/java/com/elfmcys/ysm/mixin/plugin/MixinTweaker.java`
- `src/main/java/com/elfmcys/ysm/model/ModelRuntime.java`
- `src/main/java/com/elfmcys/ysm/model/catalog/RawModelImporter.java`
- `src/main/java/com/elfmcys/ysm/model/catalog/client/ClientCatalogManager.java`
- `src/main/java/com/elfmcys/ysm/model/catalog/client/ClientCatalogSnapshot.java`
- `src/main/java/com/elfmcys/ysm/model/catalog/source/CatalogRoot.java`
- `src/main/java/com/elfmcys/ysm/model/catalog/source/ModelCatalogSources.java`
- `src/main/java/com/elfmcys/ysm/model/resource/client/ClientDemandResources.java`
- `src/main/java/com/elfmcys/ysm/model/resource/client/ClientModelRenderTargetManager.java`
- `src/main/java/com/elfmcys/ysm/model/resource/client/ModelRenderTarget.java`
- `src/main/java/com/elfmcys/ysm/model/resource/client/TargetSoundResources.java`
- `src/main/java/com/elfmcys/ysm/model/resource/client/failure/ModelFailureNotificationAggregator.java`
- `src/main/java/com/elfmcys/ysm/model/resource/client/preview/ClientPreviewGenerator.java`
- `src/main/java/com/elfmcys/ysm/model/resource/client/render/HostTexturePublisher.java`
- `src/main/java/com/elfmcys/ysm/model/resource/client/render/ModelRenderTargetLoader.java`
- `src/main/java/com/elfmcys/ysm/model/service/ClientModelService.java`
- `src/main/java/com/elfmcys/ysm/model/service/GameTrackingView.java`
- `src/main/java/com/elfmcys/ysm/model/service/ModelExportService.java`
- `src/main/java/com/elfmcys/ysm/model/service/ServerModelService.java`
- `src/main/java/com/elfmcys/ysm/model/session/server/RestrictedModelPolicy.java`
- `src/main/java/com/elfmcys/ysm/model/session/server/ServerModelSession.java`
- `src/main/java/com/elfmcys/ysm/model/storage/CachePruner.java`
- `src/main/java/com/elfmcys/ysm/model/storage/ConvertedScope.java`
- `src/main/java/com/elfmcys/ysm/model/storage/ModelStorageInfra.java`
- `src/main/java/com/elfmcys/ysm/model/storage/PreferenceCodec.java`
- `src/main/java/com/elfmcys/ysm/natives/buffer/NativeHeapBuffer.java`
- `src/main/java/com/elfmcys/ysm/natives/image/Image.java`
- `src/main/java/com/elfmcys/ysm/natives/image/ImageEncoder.java`
- `src/main/java/com/elfmcys/ysm/natives/legacy/LegacyModelImporter.java`
- `src/main/java/com/elfmcys/ysm/natives/legacy/LegacyPayloadBuffer.java`
- `src/main/java/com/elfmcys/ysm/natives/legacy/NativeLegacyProtocol.java`
- `src/main/java/com/elfmcys/ysm/natives/render/BonePoseView.java`
- `src/main/java/com/elfmcys/ysm/natives/render/FallbackVertexWriter.java`
- `src/main/java/com/elfmcys/ysm/natives/render/NativeBakedModel.java`
- `src/main/java/com/elfmcys/ysm/natives/render/NativeRenderer.java`
- `src/main/java/com/elfmcys/ysm/natives/render/VertexFormatType.java`
- `src/main/java/com/elfmcys/ysm/network/NetworkHandler.java`
- `src/main/java/com/elfmcys/ysm/network/ProtocolInbound.java`
- `src/main/java/com/elfmcys/ysm/network/forge/ChannelCompressionBypass.java`
- `src/main/java/com/elfmcys/ysm/network/forge/ClientAssetTransfer.java`
- `src/main/java/com/elfmcys/ysm/network/forge/ClientControlHandler.java`
- `src/main/java/com/elfmcys/ysm/network/forge/ClientDemandSelections.java`
- `src/main/java/com/elfmcys/ysm/network/forge/ClientPlayerPreferences.java`
- `src/main/java/com/elfmcys/ysm/network/forge/ClientPlayerStateHandler.java`
- `src/main/java/com/elfmcys/ysm/network/forge/ClientProtocolGateway.java`
- `src/main/java/com/elfmcys/ysm/network/forge/ClientSessionRuntime.java`
- `src/main/java/com/elfmcys/ysm/network/forge/ControlHandler.java`
- `src/main/java/com/elfmcys/ysm/network/forge/ForgeTransportPort.java`
- `src/main/java/com/elfmcys/ysm/network/forge/ForgeUniBufferIO.java`
- `src/main/java/com/elfmcys/ysm/network/forge/MinecraftStateHandler.java`
- `src/main/java/com/elfmcys/ysm/network/forge/PlayerStateHandler.java`
- `src/main/java/com/elfmcys/ysm/network/forge/SessionProtocolHandler.java`
- `src/main/java/com/elfmcys/ysm/network/protocol/ProtocolLimits.java`
- `src/main/java/com/elfmcys/ysm/network/protocol/ProtocolMessageSpec.java`
- `src/main/java/com/elfmcys/ysm/network/protocol/ProtocolMessages.java`
- `src/main/java/com/elfmcys/ysm/network/protocol/RoamingDictionary.java`
- `src/main/java/com/elfmcys/ysm/network/protocol/SelectionCodec.java`
- `src/main/java/com/elfmcys/ysm/util/AnimatableCacheUtil.java`
- `src/main/java/com/elfmcys/ysm/util/CommandUtil.java`
- `src/main/java/com/elfmcys/ysm/util/EnumUtil.java`
- `src/main/java/com/elfmcys/ysm/util/InputCheckUtil.java`
- `src/main/java/com/elfmcys/ysm/util/ModelIdUtil.java`
- `src/main/java/com/elfmcys/ysm/util/NativeLibUtil.java`
- `src/main/java/com/elfmcys/ysm/util/RenderUtil.java`
- `src/main/resources/assets/ysm/lang/en_us.json`
- `src/main/resources/assets/ysm/lang/ko_kr.json`
- `src/main/resources/assets/ysm/lang/ru_ru.json`
- `src/main/resources/assets/ysm/lang/zh_cn.json`
- `src/main/resources/assets/ysm/lang/zh_tw.json`
- `src/main/resources/data/ysm/tags/item/axes.json`
- `src/main/resources/data/ysm/tags/item/hoes.json`
- `src/main/resources/data/ysm/tags/item/pickaxes.json`
- `src/main/resources/data/ysm/tags/item/shovels.json`
- `src/main/resources/data/ysm/tags/item/slashblade.json`
- `src/main/resources/data/ysm/tags/item/swords.json`
- `src/main/resources/data/ysm/tags/item/tridents.json`
- `src/main/resources/ysm.mixins.json`
- `src/verification/test/java/com/elfmcys/ysm/capability/ModelInfoCapabilityTest.java`
- `src/verification/test/java/com/elfmcys/ysm/capability/RoamingPayloadsTest.java`
- `src/verification/test/java/com/elfmcys/ysm/client/command/ClientRootCommandTest.java`
- `src/verification/test/java/com/elfmcys/ysm/client/demand/ModelResourceDemandTest.java`
- `src/verification/test/java/com/elfmcys/ysm/client/gui/PlayerModelNavigationTest.java`
- `src/verification/test/java/com/elfmcys/ysm/client/texture/CustomTextureTest.java`
- `src/verification/test/java/com/elfmcys/ysm/config/ClientConfigDeviceDefaultsTest.java`
- `src/verification/test/java/com/elfmcys/ysm/config/ServerConfigSpecTest.java`
- `src/verification/test/java/com/elfmcys/ysm/format/schema/model/views/ModelViewPresenceTest.java`
- `src/verification/test/java/com/elfmcys/ysm/model/catalog/RawModelImporterTest.java`
- `src/verification/test/java/com/elfmcys/ysm/model/catalog/ReloadableModelCatalogTest.java`
- `src/verification/test/java/com/elfmcys/ysm/model/catalog/source/CatalogSourceReuseTest.java`
- `src/verification/test/java/com/elfmcys/ysm/model/resource/client/preview/ClientPreviewGeneratorTest.java`
- `src/verification/test/java/com/elfmcys/ysm/model/resource/client/render/ModelRenderTargetCacheTest.java`
- `src/verification/test/java/com/elfmcys/ysm/model/resource/client/render/ModelTextureLifecycleVerificationTest.java`
- `src/verification/test/java/com/elfmcys/ysm/model/resource/client/render/PreparedTextureSetTest.java`
- `src/verification/test/java/com/elfmcys/ysm/model/resource/client/render/PreviewResourceFixture.java`
- `src/verification/test/java/com/elfmcys/ysm/model/storage/CachePrunerTest.java`
- `src/verification/test/java/com/elfmcys/ysm/model/storage/ConvertedObjectStoreTest.java`
- `src/verification/test/java/com/elfmcys/ysm/model/storage/ModelExporterTest.java`
- `src/verification/test/java/com/elfmcys/ysm/model/storage/ModelStorageInfraTest.java`
- `src/verification/test/java/com/elfmcys/ysm/model/storage/PlayerPreferencesTest.java`
- `src/verification/test/java/com/elfmcys/ysm/model/storage/SharedStorageProcessTest.java`
- `src/verification/test/java/com/elfmcys/ysm/natives/NativeRuntimeDeviceIntegrationTest.java`
- `src/verification/test/java/com/elfmcys/ysm/network/ServerSideIsolationTest.java`
- `src/verification/test/java/com/elfmcys/ysm/network/protocol/PlayerStateValidatorTest.java`
- `src/verification/test/java/com/elfmcys/ysm/network/protocol/RoamingDictionaryTest.java`

## Renamed or moved files

- `src/main/java/com/elfmcys/ysm/client/compat/backpack/sophisticated/BackpackCuriosCompat.java` -> `src/forge-compat-reference/com/elfmcys/ysm/client/compat/backpack/sophisticated/BackpackCuriosCompat.java`
- `src/main/java/com/elfmcys/ysm/client/compat/backpack/sophisticated/SophisticatedBackpackLayerRenderer.java` -> `src/forge-compat-reference/com/elfmcys/ysm/client/compat/backpack/sophisticated/SophisticatedBackpackLayerRenderer.java`
- `src/main/java/com/elfmcys/ysm/client/compat/create/CreateCompatInner.java` -> `src/forge-compat-reference/com/elfmcys/ysm/client/compat/create/CreateCompatInner.java`
- `src/main/java/com/elfmcys/ysm/client/compat/curios/CuriosCompatInner.java` -> `src/forge-compat-reference/com/elfmcys/ysm/client/compat/curios/CuriosCompatInner.java`
- `src/main/java/com/elfmcys/ysm/client/compat/curios/MolangCache.java` -> `src/forge-compat-reference/com/elfmcys/ysm/client/compat/curios/MolangCache.java`
- `src/main/java/com/elfmcys/ysm/client/compat/immersivemelodies/ImmersiveMelodiesCompatInner.java` -> `src/forge-compat-reference/com/elfmcys/ysm/client/compat/immersivemelodies/ImmersiveMelodiesCompatInner.java`
- `src/main/java/com/elfmcys/ysm/client/compat/ironsspellbooks/IronsSpellBooksCompatInner.java` -> `src/forge-compat-reference/com/elfmcys/ysm/client/compat/ironsspellbooks/IronsSpellBooksCompatInner.java`
- `src/main/java/com/elfmcys/ysm/client/compat/jade/JadePlugin.java` -> `src/forge-compat-reference/com/elfmcys/ysm/client/compat/jade/JadePlugin.java`
- `src/main/java/com/elfmcys/ysm/client/compat/parcool/ParCoolAnimationManger.java` -> `src/forge-compat-reference/com/elfmcys/ysm/client/compat/parcool/ParCoolAnimationManger.java`
- `src/main/java/com/elfmcys/ysm/client/compat/parcool/ParCoolPredicate.java` -> `src/forge-compat-reference/com/elfmcys/ysm/client/compat/parcool/ParCoolPredicate.java`
- `src/main/java/com/elfmcys/ysm/client/compat/realcamera/RealCameraCompatInner.java` -> `src/forge-compat-reference/com/elfmcys/ysm/client/compat/realcamera/RealCameraCompatInner.java`
- `src/main/java/com/elfmcys/ysm/client/compat/simplehat/HatCuriosCompat.java` -> `src/forge-compat-reference/com/elfmcys/ysm/client/compat/simplehat/HatCuriosCompat.java`
- `src/main/java/com/elfmcys/ysm/client/compat/slashblade/SlashBladeAnimation.java` -> `src/forge-compat-reference/com/elfmcys/ysm/client/compat/slashblade/SlashBladeAnimation.java`
- `src/main/java/com/elfmcys/ysm/client/compat/slashblade/SlashBladeResharped.java` -> `src/forge-compat-reference/com/elfmcys/ysm/client/compat/slashblade/SlashBladeResharped.java`
- `src/main/java/com/elfmcys/ysm/client/compat/slashblade/SlashBladeUnsafe.java` -> `src/forge-compat-reference/com/elfmcys/ysm/client/compat/slashblade/SlashBladeUnsafe.java`
- `src/main/java/com/elfmcys/ysm/client/compat/swarfare/ReplacePlayerArmRender.java` -> `src/forge-compat-reference/com/elfmcys/ysm/client/compat/swarfare/ReplacePlayerArmRender.java`
- `src/main/java/com/elfmcys/ysm/client/compat/swarfare/SWarfareCompatInner.java` -> `src/forge-compat-reference/com/elfmcys/ysm/client/compat/swarfare/SWarfareCompatInner.java`
- `src/main/java/com/elfmcys/ysm/client/compat/swem/SwemCompatInner.java` -> `src/forge-compat-reference/com/elfmcys/ysm/client/compat/swem/SwemCompatInner.java`
- `src/main/java/com/elfmcys/ysm/client/compat/tacz/TacCompatInner.java` -> `src/forge-compat-reference/com/elfmcys/ysm/client/compat/tacz/TacCompatInner.java`
- `src/main/java/com/elfmcys/ysm/client/compat/tacz/TacEvent.java` -> `src/forge-compat-reference/com/elfmcys/ysm/client/compat/tacz/TacEvent.java`
- `src/main/java/com/elfmcys/ysm/client/compat/top/TopPlugin.java` -> `src/forge-compat-reference/com/elfmcys/ysm/client/compat/top/TopPlugin.java`
- `src/main/java/com/elfmcys/ysm/event/InterModMsg.java` -> `src/forge-compat-reference/com/elfmcys/ysm/event/InterModMsg.java` (modified)
- `src/main/resources/data/ysm/tags/items/axes.json` -> `src/main/resources/data/ysm/tags/item/axes.json` (modified)
- `src/main/resources/data/ysm/tags/items/hoes.json` -> `src/main/resources/data/ysm/tags/item/hoes.json` (modified)
- `src/main/resources/data/ysm/tags/items/pickaxes.json` -> `src/main/resources/data/ysm/tags/item/pickaxes.json` (modified)
- `src/main/resources/data/ysm/tags/items/shovels.json` -> `src/main/resources/data/ysm/tags/item/shovels.json` (modified)
- `src/main/resources/data/ysm/tags/items/slashblade.json` -> `src/main/resources/data/ysm/tags/item/slashblade.json` (modified)
- `src/main/resources/data/ysm/tags/items/swords.json` -> `src/main/resources/data/ysm/tags/item/swords.json` (modified)
- `src/main/resources/data/ysm/tags/items/tridents.json` -> `src/main/resources/data/ysm/tags/item/tridents.json` (modified)

## Added files

The Java files written new for the port are All Rights Reserved; [LICENSE-PORT.md](LICENSE-PORT.md) lists them. Copies or moves of upstream files stay Apache-2.0.

- `buildSrc/src/main/java/com/elfmcys/ysm/gradle/PreprocessTask.java`
- `buildSrc/src/main/java/com/elfmcys/ysm/gradle/Preprocessor.java`
- `buildSrc/src/main/java/com/elfmcys/ysm/gradle/RelocationCheck.java`
- `buildSrc/src/main/java/com/elfmcys/ysm/gradle/SrgMappingsTask.java`
- `buildSrc/src/main/java/com/elfmcys/ysm/gradle/SrgRemapTask.java`
- `gradle/loader-fabric.gradle`
- `gradle/loader-forge.gradle`
- `gradle/loader-neoforge.gradle`
- `gradle/test-exclusions-winefox.txt`
- `gradle/test-exclusions.txt`
- `src/forge-compat-reference/com/elfmcys/ysm/client/compat/ARCompat.java`
- `src/forge-compat-reference/com/elfmcys/ysm/client/compat/CosmeticArmorCompat.java`
- `src/forge-compat-reference/com/elfmcys/ysm/client/compat/ElytraSlotCompat.java`
- `src/forge-compat-reference/com/elfmcys/ysm/client/compat/FirstPersonCompat.java`
- `src/forge-compat-reference/com/elfmcys/ysm/client/compat/ImmersiveAircraftCompat.java`
- `src/forge-compat-reference/com/elfmcys/ysm/client/compat/IrisCompat.java`
- `src/forge-compat-reference/com/elfmcys/ysm/client/compat/OptifineCompat.java`
- `src/forge-compat-reference/com/elfmcys/ysm/client/compat/PlayerAnimatorCompat.java`
- `src/forge-compat-reference/com/elfmcys/ysm/client/compat/SimplePlaneCompat.java`
- `src/forge-compat-reference/com/elfmcys/ysm/client/compat/backpack/sophisticated/SophisticatedBuiltinComponent.java`
- `src/forge-compat-reference/com/elfmcys/ysm/client/compat/backpack/sophisticated/SophisticatedCompat.java`
- `src/forge-compat-reference/com/elfmcys/ysm/client/compat/bettercombat/BetterCombatBuiltinComponent.java`
- `src/forge-compat-reference/com/elfmcys/ysm/client/compat/bettercombat/BetterCombatCompat.java`
- `src/forge-compat-reference/com/elfmcys/ysm/client/compat/bettercombat/BetterCombatCompatInner.java`
- `src/forge-compat-reference/com/elfmcys/ysm/client/compat/bettercombat/event/PlayerAttackEvent.java`
- `src/forge-compat-reference/com/elfmcys/ysm/client/compat/carryon/CarryOnBuiltinComponent.java`
- `src/forge-compat-reference/com/elfmcys/ysm/client/compat/carryon/CarryOnCompat.java`
- `src/forge-compat-reference/com/elfmcys/ysm/client/compat/carryon/CarryOnInnerCompat.java`
- `src/forge-compat-reference/com/elfmcys/ysm/client/compat/carryon/CarryOnPredicate.java`
- `src/forge-compat-reference/com/elfmcys/ysm/client/compat/create/CreateBuiltinComponent.java`
- `src/forge-compat-reference/com/elfmcys/ysm/client/compat/create/CreateCompat.java`
- `src/forge-compat-reference/com/elfmcys/ysm/client/compat/curios/CuriosBuiltins.java`
- `src/forge-compat-reference/com/elfmcys/ysm/client/compat/curios/CuriosCompat.java`
- `src/forge-compat-reference/com/elfmcys/ysm/client/compat/immersivemelodies/ImmersiveMelodiesBuiltinComponent.java`
- `src/forge-compat-reference/com/elfmcys/ysm/client/compat/immersivemelodies/ImmersiveMelodiesCompat.java`
- `src/forge-compat-reference/com/elfmcys/ysm/client/compat/ironsspellbooks/IronsSpellBooksBuiltinComponent.java`
- `src/forge-compat-reference/com/elfmcys/ysm/client/compat/ironsspellbooks/IronsSpellBooksCompat.java`
- `src/forge-compat-reference/com/elfmcys/ysm/client/compat/parcool/ParCoolBuiltinComponent.java`
- `src/forge-compat-reference/com/elfmcys/ysm/client/compat/parcool/ParCoolCompat.java`
- `src/forge-compat-reference/com/elfmcys/ysm/client/compat/realcamera/RealCameraCompat.java`
- `src/forge-compat-reference/com/elfmcys/ysm/client/compat/simplehat/SimpleHatsCompat.java`
- `src/forge-compat-reference/com/elfmcys/ysm/client/compat/slashblade/SlashBladeBuiltinComponent.java`
- `src/forge-compat-reference/com/elfmcys/ysm/client/compat/slashblade/SlashBladeCompat.java`
- `src/forge-compat-reference/com/elfmcys/ysm/client/compat/slashblade/SlashBladeRender.java`
- `src/forge-compat-reference/com/elfmcys/ysm/client/compat/swarfare/SWarfareCompat.java`
- `src/forge-compat-reference/com/elfmcys/ysm/client/compat/swem/SwemBuiltinComponent.java`
- `src/forge-compat-reference/com/elfmcys/ysm/client/compat/swem/SwemCompat.java`
- `src/forge-compat-reference/com/elfmcys/ysm/client/compat/tacz/TACZCompat.java`
- `src/forge-compat-reference/com/elfmcys/ysm/client/compat/tacz/TaczBuiltinComponent.java`
- `src/legacy/gui-hud/java/com/elfmcys/ysm/legacy/hud/mixin/GuiHudLegacyMixin.java`
- `src/legacy/gui-hud/resources/ysm.gui-hud.mixins.json`
- `src/legacy/gui-immediate/java/com/elfmcys/ysm/client/gui/PreviewRenderHost.java`
- `src/legacy/gui-immediate/java/com/elfmcys/ysm/client/gui/pip/GuiQuadRenderState.java`
- `src/legacy/gui-immediate/java/com/elfmcys/ysm/client/gui/pip/YsmGuiModelRenderState.java`
- `src/legacy/gui-immediate/java/com/elfmcys/ysm/client/gui/pip/YsmGuiModels.java`
- `src/legacy/gui-immediate/java/com/elfmcys/ysm/legacy/gui/mixin/PreviewOutputMixin.java`
- `src/legacy/gui-immediate/resources/ysm.gui-immediate.mixins.json`
- `src/legacy/pip-readback/java/com/elfmcys/ysm/legacy/pip/mixin/PictureInPictureCopySourceMixin.java`
- `src/legacy/pip-readback/resources/ysm.pip-readback.mixins.json`
- `src/legacy/pre-byte-buffer-builder/java/com/elfmcys/ysm/mixin/fabric/client/BufferBuilderDirectWriteMixin.java`
- `src/legacy/pre-checkbox-builder/java/com/elfmcys/ysm/legacy/gui/CheckboxBuilder.java`
- `src/legacy/pre-data-components/java/com/elfmcys/ysm/legacy/item/ArrowPotions.java`
- `src/legacy/pre-data-components/java/com/elfmcys/ysm/mixin/fabric/ArrowPotionAccessor.java`
- `src/legacy/pre-delta-tracker/java/com/elfmcys/ysm/legacy/DeltaTracker.java`
- `src/legacy/pre-item-models/java/com/elfmcys/ysm/legacy/item/ItemModelResolver.java`
- `src/legacy/pre-item-models/java/com/elfmcys/ysm/legacy/item/ItemStackRenderState.java`
- `src/legacy/pre-payload-types/java/com/elfmcys/ysm/forge/ForgeNetwork.java`
- `src/legacy/pre-payload-types/java/com/elfmcys/ysm/legacy/network/ByteBufCodecs.java`
- `src/legacy/pre-payload-types/java/com/elfmcys/ysm/legacy/network/CustomPacketPayload.java`
- `src/legacy/pre-payload-types/java/com/elfmcys/ysm/legacy/network/StreamCodec.java`
- `src/legacy/pre-pipelines/java/com/elfmcys/ysm/client/texture/YsmTexture.java`
- `src/legacy/pre-pipelines/java/com/elfmcys/ysm/geckolib3/geo/CustomTranslucentRenderType.java`
- `src/legacy/pre-pipelines/java/com/elfmcys/ysm/platform/NbtCompat.java`
- `src/legacy/pre-render-states/java/com/elfmcys/ysm/client/renderer/layer/CustomPlayerElytraLayer.java`
- `src/legacy/pre-render-states/java/com/elfmcys/ysm/legacy/state/EntityRenderState.java`
- `src/legacy/pre-render-states/java/com/elfmcys/ysm/legacy/state/FishingHookRenderState.java`
- `src/legacy/pre-render-states/java/com/elfmcys/ysm/legacy/state/HumanoidRenderState.java`
- `src/legacy/pre-render-states/java/com/elfmcys/ysm/legacy/state/LivingEntityRenderState.java`
- `src/legacy/pre-render-states/java/com/elfmcys/ysm/legacy/state/MinecartRenderState.java`
- `src/legacy/pre-render-states/java/com/elfmcys/ysm/legacy/state/PlayerRenderState.java`
- `src/legacy/pre-render-states/java/com/elfmcys/ysm/legacy/state/StateExtractor.java`
- `src/legacy/pre-render-states/java/com/elfmcys/ysm/legacy/submit/mixin/EntityRenderDispatcherLegacyMixin.java`
- `src/legacy/pre-render-states/java/com/elfmcys/ysm/legacy/submit/mixin/EntityRendererLeashInvoker.java`
- `src/legacy/pre-render-states/java/com/elfmcys/ysm/legacy/submit/mixin/LivingEntityRendererLegacyMixin.java`
- `src/legacy/projection-buffers/java/com/elfmcys/ysm/legacy/projection/mixin/CachedOrthoProjectionMatrixBufferMixin.java`
- `src/legacy/projection-buffers/java/com/elfmcys/ysm/legacy/projection/mixin/CachedPerspectiveProjectionMatrixBufferMixin.java`
- `src/legacy/projection-buffers/java/com/elfmcys/ysm/legacy/projection/mixin/PerspectiveProjectionMatrixBufferMixin.java`
- `src/legacy/projection-buffers/resources/ysm.projection-buffers.mixins.json`
- `src/legacy/submit/java/com/elfmcys/ysm/legacy/input/CharacterEvent.java`
- `src/legacy/submit/java/com/elfmcys/ysm/legacy/input/InputWithModifiers.java`
- `src/legacy/submit/java/com/elfmcys/ysm/legacy/input/KeyEvent.java`
- `src/legacy/submit/java/com/elfmcys/ysm/legacy/input/MouseButtonEvent.java`
- `src/legacy/submit/java/com/elfmcys/ysm/legacy/input/MouseButtonInfo.java`
- `src/legacy/submit/java/com/elfmcys/ysm/legacy/render/CameraRenderState.java`
- `src/legacy/submit/java/com/elfmcys/ysm/legacy/render/SubmitNodeCollector.java`
- `src/legacy/submit/java/com/elfmcys/ysm/legacy/submit/mixin/EntityRenderDispatcherLegacyMixin.java`
- `src/legacy/submit/java/com/elfmcys/ysm/legacy/submit/mixin/EntityRendererLeashInvoker.java`
- `src/legacy/submit/java/com/elfmcys/ysm/legacy/submit/mixin/ItemInHandRendererLegacyMixin.java`
- `src/legacy/submit/java/com/elfmcys/ysm/legacy/submit/mixin/KeyboardHandlerLegacyMixin.java`
- `src/legacy/submit/java/com/elfmcys/ysm/legacy/submit/mixin/LivingEntityRendererLegacyMixin.java`
- `src/legacy/submit/java/com/elfmcys/ysm/legacy/submit/mixin/MouseHandlerLegacyMixin.java`
- `src/legacy/submit/resources/ysm.submit.mixins.json`
- `src/legacy/wthit-initializer/java/com/elfmcys/ysm/client/compat/wthit/YsmWthitCommonPlugin.java`
- `src/legacy/wthit-initializer/java/com/elfmcys/ysm/client/compat/wthit/YsmWthitPlugin.java`
- `src/loader/forge/localtest/META-INF/mods.toml`
- `src/loader/forge/resources/META-INF/mods.toml`
- `src/loader/forge/resources/ysm.forge.mixins.json`
- `src/loader/neoforge/localtest/META-INF/neoforge.mods.toml`
- `src/loader/neoforge/resources/META-INF/neoforge.mods.toml`
- `src/localtest/java/com/elfmcys/ysm/localtest/VisualTestHarness.java`
- `src/localtest/resources/fabric.mod.json`
- `src/localtest/resources/pack.mcmeta`
- `src/main/java/com/elfmcys/ysm/buffer/Memory.java`
- `src/main/java/com/elfmcys/ysm/client/command/ClientFeedback.java`
- `src/main/java/com/elfmcys/ysm/client/compat/ModelInfoTooltip.java`
- `src/main/java/com/elfmcys/ysm/client/compat/jade/YsmJadePlugin.java`
- `src/main/java/com/elfmcys/ysm/client/compat/wthit/YsmWthitCommonPlugin.java`
- `src/main/java/com/elfmcys/ysm/client/compat/wthit/YsmWthitPlugin.java`
- `src/main/java/com/elfmcys/ysm/client/event/ClientCapabilityAttach.java`
- `src/main/java/com/elfmcys/ysm/client/gui/GuiDraw.java`
- `src/main/java/com/elfmcys/ysm/client/gui/overlay/HudLayer.java`
- `src/main/java/com/elfmcys/ysm/client/gui/pip/GuiQuadRenderState.java`
- `src/main/java/com/elfmcys/ysm/client/gui/pip/GuiScissor.java`
- `src/main/java/com/elfmcys/ysm/client/gui/pip/YsmGuiModelRenderState.java`
- `src/main/java/com/elfmcys/ysm/client/gui/pip/YsmGuiModelRenderer.java`
- `src/main/java/com/elfmcys/ysm/client/gui/pip/YsmGuiModels.java`
- `src/main/java/com/elfmcys/ysm/client/texture/YsmTexture.java`
- `src/main/java/com/elfmcys/ysm/fabric/FabricClientEventHooks.java`
- `src/main/java/com/elfmcys/ysm/fabric/FabricEventHooks.java`
- `src/main/java/com/elfmcys/ysm/fabric/YsmFabric.java`
- `src/main/java/com/elfmcys/ysm/fabric/YsmFabricClient.java`
- `src/main/java/com/elfmcys/ysm/fabric/YsmModMenu.java`
- `src/main/java/com/elfmcys/ysm/forge/ForgeClientEventHooks.java`
- `src/main/java/com/elfmcys/ysm/forge/ForgeEventHooks.java`
- `src/main/java/com/elfmcys/ysm/forge/ForgeNetwork.java`
- `src/main/java/com/elfmcys/ysm/forge/YsmForge.java`
- `src/main/java/com/elfmcys/ysm/forge/mixin/SoundEngineStreamMixin.java`
- `src/main/java/com/elfmcys/ysm/mixin/fabric/AbstractArrowExtraInfoMixin.java`
- `src/main/java/com/elfmcys/ysm/mixin/fabric/ArrowPotionAccessor.java`
- `src/main/java/com/elfmcys/ysm/mixin/fabric/ChunkMapAccessor.java`
- `src/main/java/com/elfmcys/ysm/mixin/fabric/ConnectionAccessor.java`
- `src/main/java/com/elfmcys/ysm/mixin/fabric/ConnectionCompressionMixin.java`
- `src/main/java/com/elfmcys/ysm/mixin/fabric/EntityWalkDistanceMixin.java`
- `src/main/java/com/elfmcys/ysm/mixin/fabric/FishingHookAccessor.java`
- `src/main/java/com/elfmcys/ysm/mixin/fabric/LivingEntityEquipmentAccessor.java`
- `src/main/java/com/elfmcys/ysm/mixin/fabric/LivingEntityEventsMixin.java`
- `src/main/java/com/elfmcys/ysm/mixin/fabric/NativeImageAccessor.java`
- `src/main/java/com/elfmcys/ysm/mixin/fabric/PlayerListLoginMixin.java`
- `src/main/java/com/elfmcys/ysm/mixin/fabric/ProjectileOwnerMixin.java`
- `src/main/java/com/elfmcys/ysm/mixin/fabric/ServerCommonPacketListenerAccessor.java`
- `src/main/java/com/elfmcys/ysm/mixin/fabric/ServerPlayerRidingMixin.java`
- `src/main/java/com/elfmcys/ysm/mixin/fabric/ThrowableItemProjectileAccessor.java`
- `src/main/java/com/elfmcys/ysm/mixin/fabric/TrackedEntityAccessor.java`
- `src/main/java/com/elfmcys/ysm/mixin/fabric/YsmMixinConfigPlugin.java`
- `src/main/java/com/elfmcys/ysm/mixin/fabric/client/AbstractSliderButtonAccessor.java`
- `src/main/java/com/elfmcys/ysm/mixin/fabric/client/BufferBuilderDirectWriteMixin.java`
- `src/main/java/com/elfmcys/ysm/mixin/fabric/client/ByteBufferBuilderAccessor.java`
- `src/main/java/com/elfmcys/ysm/mixin/fabric/client/ClientRespawnCloneMixin.java`
- `src/main/java/com/elfmcys/ysm/mixin/fabric/client/EntityRenderDispatcherMixin.java`
- `src/main/java/com/elfmcys/ysm/mixin/fabric/client/EntityRenderDispatcherReloadMixin.java`
- `src/main/java/com/elfmcys/ysm/mixin/fabric/client/EntityRenderStateDataMixin.java`
- `src/main/java/com/elfmcys/ysm/mixin/fabric/client/EntityRendererStateMixin.java`
- `src/main/java/com/elfmcys/ysm/mixin/fabric/client/EntitySubjectSampleMixin.java`
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
- `src/main/java/com/elfmcys/ysm/mixin/fabric/client/PlayerQueryProjectionMixin.java`
- `src/main/java/com/elfmcys/ysm/mixin/fabric/client/ProjectionMatrixBufferMixin.java`
- `src/main/java/com/elfmcys/ysm/mixin/fabric/client/RenderTypeInvoker.java`
- `src/main/java/com/elfmcys/ysm/mixin/fabric/client/ScreenAccessor.java`
- `src/main/java/com/elfmcys/ysm/mixin/platform/EntityCapabilityMixin.java`
- `src/main/java/com/elfmcys/ysm/model/resource/client/SoundAcceptance.java`
- `src/main/java/com/elfmcys/ysm/neoforge/NeoForgeClientEventHooks.java`
- `src/main/java/com/elfmcys/ysm/neoforge/NeoForgeEventHooks.java`
- `src/main/java/com/elfmcys/ysm/neoforge/NeoForgeNetwork.java`
- `src/main/java/com/elfmcys/ysm/neoforge/YsmNeoForge.java`
- `src/main/java/com/elfmcys/ysm/network/NetworkHandlerClient.java`
- `src/main/java/com/elfmcys/ysm/network/SoundPolicyPayload.java`
- `src/main/java/com/elfmcys/ysm/network/YsmFramePayload.java`
- `src/main/java/com/elfmcys/ysm/platform/Connections.java`
- `src/main/java/com/elfmcys/ysm/platform/Dist.java`
- `src/main/java/com/elfmcys/ysm/platform/DistExecutor.java`
- `src/main/java/com/elfmcys/ysm/platform/EntityWalkDistance.java`
- `src/main/java/com/elfmcys/ysm/platform/FMLEnvironment.java`
- `src/main/java/com/elfmcys/ysm/platform/FMLPaths.java`
- `src/main/java/com/elfmcys/ysm/platform/McCompat.java`
- `src/main/java/com/elfmcys/ysm/platform/ModList.java`
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
- `src/main/java/com/elfmcys/ysm/platform/event/YsmEvents.java`
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
- `src/main/java/com/elfmcys/ysm/platform/input/KeyCodes.java`
- `src/main/java/com/elfmcys/ysm/platform/input/KeyConflictContext.java`
- `src/main/java/com/elfmcys/ysm/platform/input/KeyModifier.java`
- `src/main/java/com/elfmcys/ysm/platform/input/YsmKeyMapping.java`
- `src/main/java/com/elfmcys/ysm/platform/network/NetworkDirection.java`
- `src/main/java/com/elfmcys/ysm/platform/network/NetworkEvent.java`
- `src/main/java/com/elfmcys/ysm/platform/network/PacketTarget.java`
- `src/main/java/com/elfmcys/ysm/platform/network/Payloads.java`
- `src/main/java/com/elfmcys/ysm/platform/render/EntitySubmitHelper.java`
- `src/main/java/com/elfmcys/ysm/platform/render/YsmRenderMatrices.java`
- `src/main/java/com/elfmcys/ysm/platform/render/YsmRenderStateData.java`
- `src/main/java/com/elfmcys/ysm/platform/render/YsmRenderStateKeys.java`
- `src/main/resources/data/ysm/tags/item/bows.json`
- `src/main/resources/data/ysm/tags/item/crossbows.json`
- `src/main/resources/data/ysm/tags/item/fishing_rods.json`
- `src/main/resources/data/ysm/tags/item/mace.json`
- `src/main/resources/data/ysm/tags/item/pike.json`
- `src/main/resources/data/ysm/tags/item/shields.json`
- `src/main/resources/data/ysm/tags/item/throwable_potion.json`
- `src/main/resources/fabric.mod.json`
- `src/main/resources/waila_plugins.json`
- `src/verification/test/java/com/elfmcys/ysm/expression/host/OptionalLinkageVerifierTest.java`
- `src/verification/test/java/com/elfmcys/ysm/model/resource/client/SoundAcceptanceTest.java`
- `src/verification/test/java/com/elfmcys/ysm/testsupport/FabricTestEnvironment.java`
- `src/verification/test/resources/META-INF/services/org.junit.platform.launcher.LauncherSessionListener`
- `versions/1.20.2.properties`
- `versions/1.20.3.properties`
- `versions/1.20.4.properties`
- `versions/1.20.6.properties`
- `versions/1.21.1.properties`
- `versions/1.21.10.properties`
- `versions/1.21.11.properties`
- `versions/1.21.3.properties`
- `versions/1.21.4.properties`
- `versions/1.21.5.properties`
- `versions/1.21.6.properties`
- `versions/1.21.7.properties`
- `versions/1.21.8.properties`
- `versions/1.21.9.properties`
- `versions/1.21.properties`
- `versions/26.1.1.properties`
- `versions/26.1.2.properties`
- `versions/26.1.properties`
- `versions/26.3.properties`
- `versions/26.3.remap`
- `versions/below-1.20.3.remap`
- `versions/below-1.20.5.remap`
- `versions/below-1.21.11.remap`
- `versions/below-1.21.2.remap`
- `versions/below-1.21.4.remap`
- `versions/below-1.21.5.remap`
- `versions/below-1.21.6.remap`
- `versions/below-1.21.9.remap`
- `versions/below-1.21.remap`
- `versions/below-26.1.remap`
- `versions/forge-eventbus6.remap`
- `versions/loader-forge.remap`
- `versions/loader-neoforge.remap`
- `versions/pre-rl-factories.remap`
- `versions/pre-tick-subevents.remap`

## Deleted upstream files

- `gradle/benchmark.gradle`
- `gradle/callable-qualification.gradle`
- `gradle/catalog-links-verification.gradle`
- `gradle/expression-verification.gradle`
- `gradle/fast-run.gradle`
- `gradle/sound-verification.gradle`
- `gradle/spotless.gradle`
- `src/main/java/com/elfmcys/ysm/accessor/ILivingRenderer.java`
- `src/main/resources/META-INF/mods.toml`
- `src/main/resources/data/ysm/tags/items/bows.json`
- `src/main/resources/data/ysm/tags/items/crossbows.json`
- `src/main/resources/data/ysm/tags/items/fishing_rods.json`
- `src/main/resources/data/ysm/tags/items/shields.json`
- `src/main/resources/data/ysm/tags/items/throwable_potion.json`

## Upstream content left out of this repository

- `src/main/resources/assets/ysm/builtin/wine_fox`
- `libs/firstperson-forge-2.2.3-mc1.20.jar`
- `libs/notenoughanimations-forge-1.6.4-mc1.20.jar`
- `libs/oculus-mc1.20.1-1.7.0.jar`
- `libs/oculus_legacy-mc1.20.1-1.6.13.jar`
- `libs/superbwarfare-1.20.1-0.8.7.1-c6d401e0e.jar`
