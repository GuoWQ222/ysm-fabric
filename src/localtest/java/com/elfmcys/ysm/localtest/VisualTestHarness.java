/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.localtest;

import com.elfmcys.ysm.platform.capability.ICapabilityHolder;
import com.elfmcys.ysm.capability.PlayerAnimatableCapabilityProvider;
import com.elfmcys.ysm.client.gui.PlayerModelScreen;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Locale;
//#if LOADER == fabric
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
//#endif
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;

/**
 * LOCAL TEST HARNESS (dev runs only, not in the release jar). Active only when <gameDir>/ysm_visualtest.enable exists:
 * creates or loads a flat test world, switches the player through every built-in model with /ysm model set, takes
 * front and back screenshots, screenshots the model screen, then runs a timed model-switching soak with memory samples
 * and quits. Trigger file options: prefix=, soakTicks=, shots=, only=a,b.
 */
//#if LOADER == fabric
public final class VisualTestHarness implements ClientModInitializer {
//#elseif LOADER == neoforge
//$$ @net.neoforged.fml.common.Mod(value = "ysm_localtest", dist = net.neoforged.api.distmarker.Dist.CLIENT)
//$$ public final class VisualTestHarness {
//$$     public VisualTestHarness() {
//$$         net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
//$$             (net.neoforged.neoforge.client.event.ClientTickEvent.Post event) -> onTick(Minecraft.getInstance()));
//$$         net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
//$$             (net.neoforged.neoforge.client.event.ClientChatReceivedEvent event) -> {
//$$                 if (enabled())
//$$                     log("chat: " + event.getMessage().getString());
//$$             });
//$$     }
//#else
//$$ @net.minecraftforge.fml.common.Mod("ysm_localtest")
//$$ public final class VisualTestHarness {
//$$     public VisualTestHarness() {
//$$         if (!com.elfmcys.ysm.platform.Platform.isClient())
//$$             return;
//$$         net.minecraftforge.event.TickEvent.ClientTickEvent.Post.BUS.addListener(event -> {
//$$             onTick(Minecraft.getInstance());
//$$         });
//$$         net.minecraftforge.client.event.ClientChatReceivedEvent.BUS.addListener(event -> {
//$$             if (enabled())
//$$                 log("chat: " + event.getMessage().getString());
//$$         });
//$$     }
//#endif
    private static final String WORLD = "ysmtest";
    private static final List<String> MODELS = List.of("default", "misc/1_alex", "misc/2_steve",
        "misc/3_default_boy", "misc/4_default_controllers", "wine_fox/01_taisho_maid", "wine_fox/02_new_year",
        "wine_fox/03_astronaut", "wine_fox/04_kongfu", "wine_fox/05_magical", "wine_fox/06_hanfu", "wine_fox/07_jk",
        "wine_fox/08_sta", "wine_fox/09_hailuo", "wine_fox/10_zhiban", "wine_fox/11_salesperson",
        "wine_fox/12_little", "wine_fox/13_matured", "wine_fox/14_momo", "wine_fox/15_kluonoa", "wine_fox/16_tactics",
        "wine_fox/17_mini", "wine_fox/18_wedding", "wine_fox/19_nine_tailed", "wine_fox/20_survivor",
        "wine_fox/21_saint", "wine_fox/22_elf");
    private static final int SETTLE_TICKS = 300;
    private static final int LOAD_TICKS = 120;
    private static final int SOAK_TICKS = 12000;
    private static final int SOAK_SWITCH_TICKS = 40;
    private static final int SAMPLE_TICKS = 1200;

    private static Boolean enabled;
    private static List<String> models = MODELS;
    private static String prefix = "builtin:";
    private static String shotPrefix = "";
    private static int soakTicks = SOAK_TICKS;
    private static boolean worldRequested;
    private static int stage;
    private static int index;
    private static int wait;
    private static int soakTick;
    private static int soakIndex;
    private static final long STARTED = System.nanoTime();

    //#if LOADER == fabric
    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(VisualTestHarness::onTick);
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (enabled())
                log("chat: " + message.getString());
        });
    }
    //#endif

    /** The physical key a mapping is bound to (loader-specific accessor). */
    static com.mojang.blaze3d.platform.InputConstants.Key boundKey(net.minecraft.client.KeyMapping mapping) {
        //#if LOADER == fabric
        return net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper.getBoundKeyOf(mapping);
        //#else
        //$$ return mapping.getKey();
        //#endif
    }

    private static boolean enabled() {
        if (enabled == null) {
            var trigger = dir().getParent().resolve("ysm_visualtest.enable");
            enabled = Files.exists(trigger);
            if (enabled) {
                try {
                    for (var line : Files.readAllLines(trigger)) {
                        var kv = line.split("=", 2);
                        if (kv.length != 2)
                            continue;
                        switch (kv[0].trim()) {
                            case "prefix" -> prefix = kv[1].trim();
                            case "soakTicks" -> soakTicks = Integer.parseInt(kv[1].trim());
                            case "shots" -> shotPrefix = kv[1].trim();
                            case "only" -> {
                                var wanted = List.of(kv[1].trim().split(","));
                                models = MODELS.stream().filter(m -> wanted.stream().anyMatch(m::contains)).toList();
                            }
                            case "scenarios" -> scenarioFile = kv[1].trim();
                            case "scenarioModels" -> scenarioModels = List.of(kv[1].trim().split(","));
                            case "mixinAudit" -> mixinAudit = Boolean.parseBoolean(kv[1].trim());
                            case "scenarioModelsFile" -> {
                                try {
                                    scenarioModels = java.nio.file.Files.readAllLines(
                                        dir().getParent().resolve(kv[1].trim()), java.nio.charset.StandardCharsets.UTF_8)
                                        .stream().map(String::trim).filter(entry -> !entry.isEmpty()).toList();
                                } catch (java.io.IOException error) {
                                    log("scenarioModelsFile unreadable: " + error);
                                }
                            }
                            case "server" -> serverAddress = kv[1].trim();
                            case "observe" -> observeTarget = kv[1].trim();
                            case "observeEvery" -> observeEvery = Integer.parseInt(kv[1].trim());
                            case "observeTicks" -> observeTicks = Integer.parseInt(kv[1].trim());
                            case "observeHud" -> observeHud = Boolean.parseBoolean(kv[1].trim());
                            case "waitFor" -> waitForPlayer = kv[1].trim();
                            case "configProbe" -> configProbe = kv[1].trim();
                            case "observeDistance" -> observeDistance = Double.parseDouble(kv[1].trim());
                            default -> {
                            }
                        }
                    }
                } catch (IOException | NumberFormatException ignored) {
                }
            }
        }
        return enabled;
    }

    private static Path dir() {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("ysm_visualtest");
    }

    private static void log(String line) {
        try {
            Files.createDirectories(dir());
            Files.writeString(dir().resolve("log.txt"),
                String.format(Locale.ROOT, "[%7.1fs] %s%n", (System.nanoTime() - STARTED) / 1e9, line),
                StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException ignored) {
        }
    }

    private static void sample(String label, boolean gc) {
        if (gc)
            for (int i = 0; i < 3; i++)
                System.gc();
        var heap = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
        var os = (com.sun.management.OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean();
        var classes = ManagementFactory.getClassLoadingMXBean();
        var line = String.format(Locale.ROOT, "%.1f,%s,%b,%.1f,%.1f,%d,%d,%d", (System.nanoTime() - STARTED) / 1e9,
            label, gc, heap / 1048576.0, os.getCommittedVirtualMemorySize() / 1048576.0,
            classes.getLoadedClassCount(), classes.getUnloadedClassCount(), Minecraft.getInstance().getFps());
        try {
            var csv = dir().resolve("memory.csv");
            if (!Files.exists(csv))
                Files.writeString(csv, "seconds,label,gc,heap_mb,private_mb,classes_loaded,classes_unloaded,fps\n");
            Files.writeString(csv, line + "\n", StandardOpenOption.APPEND);
        } catch (IOException ignored) {
        }
    }

    private static void shot(String name) {
        var mc = Minecraft.getInstance();
        Screenshot.grab(mc.gameDirectory, "ysmtest_" + shotPrefix + name + ".png", com.elfmcys.ysm.localtest.VisualTestHarness.mainTarget(mc), 1,
            message -> {});
        log("screenshot " + name);
    }

    private static void command(String command) {
        var mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.connection.sendCommand(command);
            log("command /" + command);
        }
    }

    // ---- Scenario mode: the step list in run/<scenarios> is shared with the 2.6.5 harness (26.1 NeoForge), so both
    // ---- versions run the same actions and their screenshots pair up. Steps (one per line, # comments):
    // ----   scene NAME | cmd COMMAND | wait TICKS | shot NAME | key forward|back|left|right|jump|sneak|sprint|use|attack
    // ----   down|up | swing main|off | camera front|back|first | body square | fly on|off | sleep X Y Z | wake
    // ----   | respawn | screen close | press KEYMAPPING_NAME | hud show|hide | log TEXT
    private static String scenarioFile;
    private static List<String> scenarioModels = List.of("default");
    /** host:port of a dedicated server to join instead of the singleplayer test world (multiplayer test). */
    private static String serverAddress = null;
    /** Observer mode: name of the remote player to watch (two-client multiplayer test). */
    private static String observeTarget = null;
    private static int observeEvery = 100;
    private static int observeTicks = 9000;
    private static boolean observeHud;
    /** Two-client test: do not start until this player has joined. */
    private static String waitForPlayer = null;
    /** "set" or "check": in-game config persistence probe. */
    private static String configProbe = null;
    private static int configProbePhase;
    private static int waitForTicks;
    private static double observeDistance = 5.0;
    private static int observeElapsed;
    private static int observeShot;
    private static boolean observeSeen;
    private static boolean observeAwaitShot;
    private static int observeMissing;
    private static List<String> scenarioSteps;
    private static int scenarioModel = -1;
    private static int scenarioStep;
    private static String scene = "";

    private static void scenarioTick(Minecraft mc) {
        if (scenarioSteps == null) {
            try {
                scenarioSteps = Files.readAllLines(dir().getParent().resolve(scenarioFile)).stream()
                    .map(line -> line.replaceAll("\\s+#.*$", "").strip())
                    .filter(line -> !line.isEmpty() && !line.startsWith("#")).toList();
            } catch (IOException error) {
                log("cannot read scenarios " + scenarioFile + ": " + error);
                stage = 8;
                return;
            }
            log("scenarios: " + scenarioSteps.size() + " steps x " + scenarioModels.size() + " model(s)");
        }
        if (scenarioModel < 0 || scenarioStep >= scenarioSteps.size()) {
            scenarioModel++;
            scenarioStep = 0;
            if (scenarioModel >= scenarioModels.size()) {
                stage = 8;
                return;
            }
            var model = scenarioModels.get(scenarioModel);
            // "id#texture" selects a texture; plain ids use the model's default ("-")
            var texture = "-";
            var hashIndex = model.lastIndexOf('#');
            if (hashIndex > 0) {
                texture = quoted(model.substring(hashIndex + 1));
                model = model.substring(0, hashIndex);
            }
            command("ysm model set @s " + quoted(model.contains(":") ? model : prefix + model) + " " + texture);
            wait = LOAD_TICKS;
            return;
        }
        var step = scenarioSteps.get(scenarioStep++);
        try {
            runStep(mc, step);
        } catch (RuntimeException error) {
            log("step failed [" + scene + "] " + step + ": " + error);
        }
    }

    private static String modelTag() {
        var id = scenarioModels.get(scenarioModel);
        var tag = id.replaceAll("[^0-9A-Za-z_.+-]", "_");
        // non-ASCII names would collide once replaced ("万敌", "风堇" -> "__"): keep them apart with a hash
        return id.matches("[0-9A-Za-z_.+:/#-]*") ? tag : tag + "_" + Integer.toHexString(id.hashCode());
    }

    private static void runStep(Minecraft mc, String step) {
        var parts = step.split("\\s+", 2);
        var arg = parts.length > 1 ? parts[1] : "";
        var player = mc.player;
        switch (parts[0]) {
            case "scene" -> {
                scene = arg;
                log("scene " + arg);
            }
            case "cmd" -> command(arg);
            case "wait" -> wait = Integer.parseInt(arg);
            case "shot" -> {
                shot(modelTag() + "__" + arg);
                logState(mc);
                var ysm = ICapabilityHolder.of(player).getCapability(PlayerAnimatableCapabilityProvider.CAP).orElse(null);
                if (ysm != null)
                    log("  ysm: extra=" + ysm.isPlayingExtraAnimation() + " name=" + ysm.getExtraAnimationName());
                var vehicle = player.getVehicle();
                if (vehicle != null) {
                    var v = ICapabilityHolder.of(vehicle).getCapability(com.elfmcys.ysm.capability.VehicleAnimatableCapabilityProvider.CAP)
                        .orElse(null);
                    if (v == null) {
                        log("  vehicle model: no capability");
                    } else {
                        v.waitForAsyncUpdate();
                        log("  vehicle model: assigned=" + v.isAssigned() + " installed=" + v.hasMatchingInstallation()
                            + " allowed=" + v.isPresentationAllowed() + " block=" + v.getSelectionBlockReason()
                            + " seat: " + v.seatDiagnostic + " seat0=" + (v.passengerSeat(0) != null));
                        for (Throwable c = v.modelLoadFailure(); c != null; c = c.getCause())
                            log("    failure: " + c);
                    }
                }
                var cap = ICapabilityHolder.of(player).getCapability(PlayerAnimatableCapabilityProvider.CAP).orElse(null);
                var failure = cap == null ? null : cap.modelLoadFailure();
                if (failure != null)
                    log("  model failure: " + failure);
            }
            case "melody" -> startMelody(mc, arg);
            case "vehicle" -> logVehicle(mc, arg);
            case "catalog" -> {
                // repeat this step every second until the server model catalog is settled
                if (!catalogSettled(arg)) {
                    scenarioStep--;
                    wait = 20;
                }
            }
            case "click" -> {
                var mapping = "attack".equals(arg) ? mc.options.keyAttack : mc.options.keyUse;
                net.minecraft.client.KeyMapping.click(boundKey(mapping));
                log("  clicked " + arg);
            }
            case "key" -> {
                var a = arg.split("\\s+");
                var mapping = switch (a[0]) {
                    case "forward" -> mc.options.keyUp;
                    case "back" -> mc.options.keyDown;
                    case "left" -> mc.options.keyLeft;
                    case "right" -> mc.options.keyRight;
                    case "jump" -> mc.options.keyJump;
                    case "sneak" -> mc.options.keyShift;
                    case "sprint" -> mc.options.keySprint;
                    case "use" -> mc.options.keyUse;
                    case "attack" -> mc.options.keyAttack;
                    default -> throw new IllegalArgumentException("unknown key " + a[0]);
                };
                boolean down = "down".equals(a[1]);
                // Press the physical key: every binding on it activates, as with real input (e.g. Carry On's own
                // carry key shares Shift with sneak).
                net.minecraft.client.KeyMapping.set(
                    boundKey(mapping), down);
                if (down)
                    net.minecraft.client.KeyMapping.click(
                        boundKey(mapping));
            }
            //#if MC >= 26.3
            //$$ case "swing" -> player.swing("off".equals(arg) ? net.minecraft.world.InteractionHand.OFF_HAND
            //$$     : net.minecraft.world.InteractionHand.MAIN_HAND, net.minecraft.world.item.component.SwingAnimation.DEFAULT, false);
            //#else
            case "swing" -> player.swing("off".equals(arg) ? net.minecraft.world.InteractionHand.OFF_HAND
                : net.minecraft.world.InteractionHand.MAIN_HAND);
            //#endif
            case "camera" -> mc.options.setCameraType(switch (arg) {
                case "first" -> CameraType.FIRST_PERSON;
                case "back" -> CameraType.THIRD_PERSON_BACK;
                default -> CameraType.THIRD_PERSON_FRONT;
            });
            case "body" -> {
                player.setYBodyRot(player.getYRot());
                player.yBodyRotO = player.yBodyRot;
            }
            case "fly" -> {
                player.getAbilities().flying = "on".equals(arg);
                player.onUpdateAbilities();
            }
            case "sleep", "wake" -> {
                var server = mc.getSingleplayerServer();
                var id = player.getUUID();
                var a = arg.split("\\s+");
                server.execute(() -> {
                    var sp = server.getPlayerList().getPlayer(id);
                    if (sp == null)
                        return;
                    if ("wake".equals(parts[0]))
                        sp.stopSleepInBed(true, true);
                    else
                        sp.startSleeping(new net.minecraft.core.BlockPos(Integer.parseInt(a[0]),
                            Integer.parseInt(a[1]), Integer.parseInt(a[2])));
                });
            }
            case "respawn" -> player.respawn();
            case "screen" -> com.elfmcys.ysm.platform.McCompat.setScreen(null);
            case "press" -> {
                if (arg.matches("\\d+"))
                    pressKeyCode(mc, Integer.parseInt(arg), "key code " + arg);
                else
                    pressMapping(mc, arg);
            }
            case "hud" -> setHudHidden(mc, !"show".equals(arg));
            case "log" -> log(String.format(java.util.Locale.ROOT,
                "%s: flying=%b mayfly=%b ground=%b y=%.3f dy=%.3f mode=%s camera=%s",
                arg, player.getAbilities().flying, player.getAbilities().mayfly, player.onGround(), player.getY(),
                player.getDeltaMovement().y, mc.gameMode.getPlayerMode(), mc.options.getCameraType()));
            default -> log("unknown step: " + step);
        }
    }

private static void logState(Minecraft mc) {
        var player = mc.player;
        var vehicle = player.getVehicle();
        logIntegrations(player);
        for (var entity : mc.level.entitiesForRendering()) {
            if (entity.displayFireAnimation())
                log(String.format(java.util.Locale.ROOT, "  burning: %s at %.1f %.1f %.1f fireTicks=%d onFireFlag=%b age=%d",
                    net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()),
                    entity.getX(), entity.getY(), entity.getZ(), entity.getRemainingFireTicks(), entity.isOnFire(),
                    entity.tickCount));
        }
        log(String.format(java.util.Locale.ROOT,
            "  state: flying=%b fallFlying=%b pose=%s ground=%b sleeping=%b yaw=%.1f body=%.1f head=%.1f vehicle=%s vyaw=%.1f",
            player.getAbilities().flying, player.isFallFlying(), player.getPose(), player.onGround(), player.isSleeping(),
            player.getYRot(), player.yBodyRot, player.yHeadRot,
            vehicle == null ? "none" : vehicle.getClass().getSimpleName(), vehicle == null ? 0f : vehicle.getYRot()));
        if (vehicle instanceof net.minecraft.world.entity.vehicle.minecart.AbstractMinecart cart)
            log("  minecart: behavior=" + cart.getBehavior().getClass().getSimpleName() + " xRot=" + cart.getXRot());
    }

    /** Undoes the fake grab (a screen opened by the key keeps its own cursor state). */
    private static void releaseMouse(Minecraft mc) {
        try {
            var grabbed = net.minecraft.client.MouseHandler.class.getDeclaredField("mouseGrabbed");
            grabbed.setAccessible(true);
            grabbed.setBoolean(mc.mouseHandler, false);
        } catch (ReflectiveOperationException error) {
            log("  mouse release failed: " + error);
        }
    }

    /** Test-only: YSM's key handlers need an active window with a grabbed mouse; the test window is often unfocused. */
    private static void pretendFocused(Minecraft mc) {
        try {
            var focused = com.mojang.blaze3d.platform.Window.class.getDeclaredField("focused");
            focused.setAccessible(true);
            focused.setBoolean(mc.getWindow(), true);
            var grabbed = net.minecraft.client.MouseHandler.class.getDeclaredField("mouseGrabbed");
            grabbed.setAccessible(true);
            grabbed.setBoolean(mc.mouseHandler, true);
        } catch (ReflectiveOperationException error) {
            log("  focus override failed: " + error);
        }
    }

    /** A real key press and release of the binding, through the keyboard handler (fires YSM's key events). */
    private static void pressMapping(Minecraft mc, String name) {
        for (var mapping : mc.options.keyMappings) {
            if (!mapping.getName().equals(name))
                continue;
            var key = boundKey(mapping);
            pressKeyCode(mc, key.getValue(), name + " (" + key.getName() + ")");
            return;
        }
        log("  no key mapping " + name);
    }

    /** A real key press and release of one GLFW key code through the keyboard handler. */
    private static void pressKeyCode(Minecraft mc, int keyCode, String label) {
        try {
            var method = net.minecraft.client.KeyboardHandler.class.getDeclaredMethod("keyPress", long.class,
                int.class, net.minecraft.client.input.KeyEvent.class);
            method.setAccessible(true);
            pretendFocused(mc);
            var event = new net.minecraft.client.input.KeyEvent(keyCode, 0, 0);
            method.invoke(mc.keyboardHandler, mc.getWindow().handle(), 1, event);
            method.invoke(mc.keyboardHandler, mc.getWindow().handle(), 0, event);
            releaseMouse(mc);
            log("  pressed " + label);
        } catch (ReflectiveOperationException error) {
            log("  press failed " + label + ": " + error);
        }
    }

    /** Integration state, only for mods that are present. */
    private static void logIntegrations(net.minecraft.client.player.LocalPlayer player) {
        var loader = new Object() {
            boolean isModLoaded(String id) {
                return com.elfmcys.ysm.platform.Platform.isModLoaded(id);
            }
        };
        if (loader.isModLoaded("carryon")) {
            try {
                var manager = Class.forName("tschipp.carryon.common.carry.CarryOnDataManager");
                var data = manager.getMethod("getCarryData", net.minecraft.world.entity.player.Player.class)
                    .invoke(null, player);
                boolean carrying = (Boolean)data.getClass().getMethod("isCarrying").invoke(data);
                log("  carryon: carrying=" + carrying + (carrying ? " type=" + data.getClass().getMethod("getType").invoke(data) : ""));
            } catch (ReflectiveOperationException error) {
                log("  carryon: state unreadable " + error);
            }
        }
        if (loader.isModLoaded("bettercombat"))
            log("  bettercombat: attack='" + com.elfmcys.ysm.client.compat.bettercombat.BetterCombatCompat
                .getAttackAnimation(player) + "' swinging=" + com.elfmcys.ysm.platform.McCompat.isSwinging(player));
        if (loader.isModLoaded("player_animation_library"))
            log("  playeranim: thirdPersonModelInFirstPerson="
                + com.elfmcys.ysm.client.compat.PlayerAnimatorCompat.hasThirdPersonModelAnim(player));
        if (loader.isModLoaded("firstperson"))
            log("  firstperson: enabled=" + com.elfmcys.ysm.client.compat.FirstPersonCompat.isEnabled()
                + " renderingPlayer=" + com.elfmcys.ysm.client.compat.FirstPersonCompat.isRenderingPlayer());
        log("  gui: hud calls=" + com.elfmcys.ysm.client.gui.overlay.ExtraPlayerScreen.calls + " pip draws="
            + com.elfmcys.ysm.client.gui.pip.YsmGuiModelRenderer.draws);
        if (loader.isModLoaded("immersive_melodies")) {
            var data = new com.elfmcys.ysm.client.compat.immersivemelodies.ImmersiveMelodiesCompat.ImmersiveMelodiesData();
            com.elfmcys.ysm.client.compat.immersivemelodies.ImmersiveMelodiesCompat.updateMelodyProgress(player, data);
            log(String.format(java.util.Locale.ROOT, "  melodies: pitch=%.3f volume=%.3f current=%.3f delta=%.0f time=%.0f",
                data.pitch, data.volume, data.current, data.delta, data.time));
            // Immersive Melodies' own state (reflection: the mod is optional for this source set)
            try {
                var manager = Class.forName("immersive_melodies.client.MelodyProgressManager").getField("INSTANCE").get(null);
                var progress = manager.getClass().getMethod("getProgress", net.minecraft.world.entity.Entity.class)
                    .invoke(manager, player);
                var playing = progress.getClass().getMethod("getCurrentlyPlaying").invoke(progress);
                var melody = progress.getClass().getMethod("getMelody").invoke(progress);
                String notes = "no melody";
                if (melody != null) {
                    var tracks = (java.util.List<?>)melody.getClass().getMethod("getTracks").invoke(melody);
                    int count = 0;
                    int first = Integer.MAX_VALUE;
                    for (var track : tracks) {
                        var list = (java.util.List<?>)track.getClass().getMethod("getNotes").invoke(track);
                        count += list.size();
                        if (!list.isEmpty())
                            first = Math.min(first, (int)list.get(0).getClass().getMethod("getTime").invoke(list.get(0)));
                    }
                    notes = tracks.size() + " tracks, " + count + " notes, first at " + first + " ms";
                }
                log("  melodies state: playing=" + playing + " isPlaying="
                    + progress.getClass().getMethod("isPlaying").invoke(progress) + " " + notes);
            } catch (ReflectiveOperationException | RuntimeException error) {
                log("  melodies state unavailable: " + error);
            }
        }
        if (player.getVehicle() != null)
        {
            var vehicle = player.getVehicle();
            String roll = "";
            try {
                roll = String.format(java.util.Locale.ROOT, " roll=%.1f",
                    (float)vehicle.getClass().getMethod("getRoll", float.class).invoke(vehicle, 1.0f));
            } catch (ReflectiveOperationException | RuntimeException ignored) {
                // not an aircraft
            }
            log(String.format(java.util.Locale.ROOT, "  vehicle: %s xRot=%.1f yRot=%.1f y=%.1f ground=%s%s",
                net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(vehicle.getType()),
                vehicle.getXRot(), vehicle.getYRot(), vehicle.getY(), vehicle.onGround(), roll));
        }
        if (loader.isModLoaded("realcamera"))
            log("  realcamera: active=" + com.elfmcys.ysm.client.compat.realcamera.RealCameraCompat.isActive());
    }

    /** Quotes a command argument unless it is a plain token (as the 2.6.5 harness). */
    private static String quoted(String value) {
        return value.matches("[0-9A-Za-z_.+:/-]+") ? value
            : "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    /** Immersive Melodies: starts a melody on the instrument the player holds (server side, singleplayer). */
    private static boolean loadingErrorSeen;
    private static int giveUpTicks;
    private static int noWorldTicks;
    private static int catalogLastCount = -1;
    private static int catalogStableTicks;
    private static int catalogWaitedTicks;

    /**
     * "catalog [timeoutTicks]": the integrated server's model catalog (all scanned and converted models, e.g. a
     * community collection in ysm/models). Settled once its model count stays unchanged for 20 s (or at the timeout);
     * then logs the count, warnings and every scan error. Reads the private catalog by reflection (test only).
     */
    private static boolean catalogSettled(String arg) {
        // "catalog <timeoutTicks> [expectedModels]": with an expected count, settled only once it is reached (a busy
        // PC can pause the scan for longer than any stability window); without one, after 60 s without change
        var parts = arg.isBlank() ? new String[0] : arg.trim().split("\\s+");
        int timeout = parts.length > 0 ? Integer.parseInt(parts[0]) : 12000;
        int expected = parts.length > 1 ? Integer.parseInt(parts[1]) : -1;
        try {
            var service = com.elfmcys.ysm.model.service.ServerModelService.instance();
            var field = service.getClass().getDeclaredField("catalog");
            field.setAccessible(true);
            var snapshot = ((com.elfmcys.ysm.model.catalog.ReloadableModelCatalog)field.get(service)).current();
            int count = snapshot.byModelId().size();
            if (count != catalogLastCount) {
                catalogLastCount = count;
                catalogStableTicks = 0;
            } else {
                catalogStableTicks += 20;
            }
            catalogWaitedTicks += 20;
            boolean settled = expected > 0 ? count >= expected && catalogStableTicks >= 400 : catalogStableTicks >= 1200;
            if (!settled && catalogWaitedTicks < timeout)
                return false;
            var report = snapshot.report();
            log("  catalog: " + count + " models, " + report.errorCount() + " errors, " + report.warningCount()
                + " warnings (waited " + catalogWaitedTicks + " ticks" + (settled ? "" : ", TIMEOUT") + ")");
            for (var error : report.errors())
                log("  catalog error [" + error.rootKind() + "] " + error.source() + ": " + error.message());
        } catch (ReflectiveOperationException | RuntimeException failure) {
            log("  catalog failed: " + failure);
        }
        return true;
    }

    /** Logs the ridden vehicle's own pitch, yaw and roll (Immersive Aircraft getRoll(float), if present). */
    private static void logVehicle(Minecraft mc, String label) {
        var vehicle = mc.player.getVehicle();
        if (vehicle == null) {
            log("  vehicle " + label + ": none");
            return;
        }
        String roll = "-";
        try {
            roll = String.valueOf(vehicle.getClass().getMethod("getRoll", float.class).invoke(vehicle, 1.0F));
        } catch (ReflectiveOperationException ignored) {
        }
        log(String.format(Locale.ROOT, "  vehicle %s: %s pitch=%.1f yaw=%.1f roll=%s y=%.1f", label,
            vehicle.getType(), vehicle.getXRot(), vehicle.getYRot(), roll, vehicle.getY()));
    }

    private static void startMelody(Minecraft mc, String melodyId) {
        var server = mc.getSingleplayerServer();
        var id = mc.player.getUUID();
        server.execute(() -> {
            try {
                var player = server.getPlayerList().getPlayer(id);
                var stack = player.getMainHandItem();
                var item = stack.getItem();
                java.lang.reflect.Method play = null;
                for (var method : item.getClass().getMethods())
                    if (method.getName().equals("play") && method.getParameterCount() == 4
                        && method.getParameterTypes()[2] == net.minecraft.world.level.Level.class)
                        play = method;
                if (play == null) {
                    log("  melody: held item is not an instrument: " + item);
                    return;
                }
                play.invoke(item, stack, net.minecraft.resources.Identifier.parse(melodyId), player.level(), player);
                log("  melody: started " + melodyId);
            } catch (ReflectiveOperationException | RuntimeException error) {
                log("  melody failed: " + error);
            }
        });
    }

    /** In-game config persistence probe (see configProbe option). */
    private static void configProbeTick(Minecraft mc) {
        var volume = com.elfmcys.ysm.config.ClientConfig.SOUND_VOLUME;
        var hands = com.elfmcys.ysm.config.ClientConfig.DISABLE_SELF_HANDS;
        var memo = mc.gameDirectory.toPath().resolve("ysm_configprobe.txt");
        try {
            if (configProbePhase == 0) {
                log("configProbe " + configProbe + ": values now SoundVolume=" + volume.get() + " DisableSelfHands="
                    + hands.get());
                if (configProbe.equals("set")) {
                    java.nio.file.Files.writeString(memo, volume.get() + "\n" + hands.get() + "\n");
                    // Drive the real config screen: press its "disable self hands" checkbox, move its volume slider.
                    var screen = new com.elfmcys.ysm.client.gui.ConfigScreen(
                        (com.elfmcys.ysm.client.gui.PlayerModelScreen)null);
                    com.elfmcys.ysm.platform.McCompat.setScreen(screen);
                    boolean pressed = false, slid = false;
                    for (var child : screen.children()) {
                        if (child instanceof net.minecraft.client.gui.components.Checkbox box
                            && box.getMessage().getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents key
                            && key.getKey().equals("gui.yes_steve_model.config.disable_self_hands")) {
                            box.onPress(new net.minecraft.client.input.KeyEvent(257, 0, 0));
                            pressed = true;
                        } else if (child instanceof net.minecraft.client.gui.components.AbstractSliderButton slider) {
                            var setValue = net.minecraft.client.gui.components.AbstractSliderButton.class
                                .getDeclaredMethod("setValue", double.class);
                            setValue.setAccessible(true);
                            setValue.invoke(slider, 0.37);
                            slid = true;
                        }
                    }
                    com.elfmcys.ysm.platform.McCompat.setScreen(null);
                    log("configProbe set via ConfigScreen widgets: checkbox pressed=" + pressed + " slider moved=" + slid
                        + " -> SoundVolume=" + volume.get() + " DisableSelfHands=" + hands.get());
                } else {
                    var original = java.nio.file.Files.readAllLines(memo);
                    boolean kept = volume.get() == 37.0 && hands.get() != Boolean.parseBoolean(original.get(1).trim());
                    log("configProbe check: " + (kept ? "PERSISTED" : "LOST") + " after restart");
                    volume.set(Double.parseDouble(original.get(0).trim()));
                    hands.set(Boolean.parseBoolean(original.get(1).trim()));
                    hands.save();
                    log("configProbe restored SoundVolume=" + volume.get() + " DisableSelfHands=" + hands.get());
                }
                configProbePhase = 1;
                wait = 20;
                return;
            }
            var file = mc.gameDirectory.toPath().resolve("config").resolve("ysm-client.toml");
            for (var line : java.nio.file.Files.readAllLines(file))
                if (line.contains("SoundVolume") || line.contains("DisableSelfHands"))
                    log("configProbe file: " + line.trim());
        } catch (java.io.IOException | ReflectiveOperationException | RuntimeException failure) {
            log("configProbe failed: " + failure);
        }
        stage = 8;
    }

    /** Observer mode: follows the remote player, alternating a teleport in front of it and a screenshot + state log. */
    private static void observeTick(Minecraft mc) {
        observeElapsed++;
        net.minecraft.world.entity.player.Player target = null;
        for (var other : mc.level.players())
            if (other != mc.player && other.getGameProfile().name().equals(observeTarget))
                target = other;
        if (target == null) {
            // Entities can drop out of the client level for a moment (teleports, tracking); quit after 5 s unseen.
            if (observeSeen && ++observeMissing < 100)
                return;
            if (observeSeen || observeElapsed > observeTicks) {
                log(observeSeen ? "observe: " + observeTarget + " left; quitting" : "observe: target never seen; quitting");
                stage = 8;
            } else if (observeElapsed % 200 == 1) {
                log("observe: waiting for " + observeTarget);
            }
            return;
        }
        observeMissing = 0;
        if (!observeSeen) {
            observeSeen = true;
            log("observe: " + observeTarget + " in view range");
        }
        if (observeElapsed > observeTicks) {
            log("observe: time limit; quitting");
            stage = 8;
            return;
        }
        if (observeAwaitShot) {
            observeAwaitShot = false;
            shot(String.format(java.util.Locale.ROOT, "observe_%03d", observeShot++));
            logRemote(target);
            wait = Math.max(0, observeEvery - 10);
            return;
        }
        // observeDistance blocks in front of the target (which faces its own yaw), looking back at its chest.
        double yaw = Math.toRadians(target.getYRot());
        double x = target.getX() - Math.sin(yaw) * observeDistance;
        double z = target.getZ() + Math.cos(yaw) * observeDistance;
        net.minecraft.world.entity.Entity aim = target.getVehicle() != null ? target.getVehicle() : target;
        double pitch = Math.toDegrees(Math.atan2(mc.player.getEyeHeight() - aim.getBbHeight() * 0.5, observeDistance));
        command(String.format(java.util.Locale.ROOT, "tp @s %.2f %.2f %.2f %.1f %.1f", x, aim.getY(), z,
            target.getYRot() + 180.0F, pitch));
        observeAwaitShot = true;
        wait = 10;
    }

    private static void logRemote(net.minecraft.world.entity.player.Player target) {
        var vehicle = target.getVehicle();
        log(String.format(java.util.Locale.ROOT, "  remote: %s pose=%s ground=%b pos=%.1f %.1f %.1f vehicle=%s main=%s",
            target.getGameProfile().name(), target.getPose(), target.onGround(), target.getX(), target.getY(),
            target.getZ(), vehicle == null ? "none" : vehicle.getType().toShortString(),
            target.getMainHandItem().getItem()));
        var ysm = ICapabilityHolder.of(target).getCapability(PlayerAnimatableCapabilityProvider.CAP).orElse(null);
        if (ysm == null) {
            log("  remote ysm: no capability");
            return;
        }
        ysm.waitForAsyncUpdate();
        var selection = ysm.remoteSelection();
        log("  remote ysm: assigned=" + ysm.isAssigned() + " installed=" + ysm.hasMatchingInstallation()
            + " allowed=" + ysm.isPresentationAllowed() + " block=" + ysm.getSelectionBlockReason()
            + " extra=" + ysm.isPlayingExtraAnimation() + " name=" + ysm.getExtraAnimationName()
            + " selection=" + (selection == null ? "none"
                : selection.model() + " texture=" + selection.texture() + " animation=" + selection.animation()));
    }

    static com.mojang.blaze3d.pipeline.RenderTarget mainTarget(Minecraft mc) {
        //#if MC >= 26.2
        return mc.gameRenderer.mainRenderTarget();
        //#else
        //$$ return mc.getMainRenderTarget();
        //#endif
    }

    private static void setHudHidden(Minecraft mc, boolean hidden) {
        //#if MC >= 26.2
        if (mc.gui.hud.isHidden() != hidden)
            mc.gui.hud.toggle();
        //#else
        //$$ mc.options.hideGui = hidden;
        //#endif
    }

    private static boolean loadingScreenHandled;
    private static boolean mixinAudit;
    private static boolean mixinAudited;

    private static void onTick(Minecraft mc) {
        if (!enabled())
            return;
        if (mixinAudit && !mixinAudited) {
            // Loads every mixin target once (with -Pmixin_lenient failures are logged, not fatal)
            mixinAudited = true;
            log("mixin audit start");
            try {
                org.spongepowered.asm.mixin.MixinEnvironment.getCurrentEnvironment().audit();
                log("mixin audit done");
            } catch (Throwable failure) {
                log("mixin audit failed: " + failure);
            }
        }
        // NeoForge/Forge show loading warnings on a screen that waits for "Proceed": continue past warnings-only
        // screens (as the button does), but never past a loading error
        var startScreen = com.elfmcys.ysm.platform.McCompat.screen();
        if (!worldRequested && startScreen != null && startScreen.getClass().getSimpleName().equals("LoadingErrorScreen")
            && !loadingScreenHandled) {
            loadingScreenHandled = true;
            boolean warningsOnly = startScreen.children().stream().anyMatch(child ->
                child instanceof net.minecraft.client.gui.components.AbstractButton button && button.getMessage().getString()
                    .toLowerCase(java.util.Locale.ROOT).matches(".*(proceed|continue).*"));
            log("loading " + (warningsOnly ? "warnings screen: continuing" : "ERROR screen: quitting in 10 s"));
            if (warningsOnly)
                com.elfmcys.ysm.platform.McCompat.setScreen(null);
            else
                loadingErrorSeen = true;
        }
        // Never leave a client behind (user 11-24: disconnected clients stayed open): a loading error, a lost or
        // refused server connection, or no world 2 min after connecting ends the client instead of waiting forever
        if (loadingErrorSeen && ++giveUpTicks == 200) {
            log("loading error: quitting");
            mc.stop();
            return;
        }
        if (worldRequested && serverAddress != null && mc.level == null) {
            var current = com.elfmcys.ysm.platform.McCompat.screen();
            String screenName = current == null ? "none" : current.getClass().getSimpleName();
            if ((screenName.equals("DisconnectedScreen") || ++noWorldTicks > 2400) && ++giveUpTicks == 100) {
                log("connection lost or refused (" + screenName + "): quitting");
                mc.stop();
                return;
            }
        } else if (mc.level != null) {
            noWorldTicks = 0;
            if (!loadingErrorSeen)
                giveUpTicks = 0;
        }
        if (!worldRequested && com.elfmcys.ysm.platform.McCompat.screen() instanceof TitleScreen title) {
            worldRequested = true;
            if (serverAddress != null) {
                log("connecting to server " + serverAddress);
                net.minecraft.client.gui.screens.ConnectScreen.startConnecting(title, mc,
                    net.minecraft.client.multiplayer.resolver.ServerAddress.parseString(serverAddress),
                    new net.minecraft.client.multiplayer.ServerData("ysmtest", serverAddress,
                        net.minecraft.client.multiplayer.ServerData.Type.OTHER), false, null);
            } else if (mc.getLevelSource().levelExists(WORLD)) {
                log("loading world " + WORLD);
                mc.createWorldOpenFlows().openWorld(WORLD, () -> log("world load cancelled"));
            } else {
                log("creating flat world " + WORLD);
                var settings = new LevelSettings(WORLD, GameType.CREATIVE,
                    new LevelSettings.DifficultySettings(Difficulty.PEACEFUL, false, false), true,
                    WorldDataConfiguration.DEFAULT);
                mc.createWorldOpenFlows().createFreshLevel(WORLD, settings, new WorldOptions(20261122L, false, false),
                    registries -> registries.lookupOrThrow(Registries.WORLD_PRESET).getOrThrow(WorldPresets.FLAT)
                        .value().createWorldDimensions(), title);
            }
            return;
        }
        if (mc.player == null || mc.level == null)
            return;
        if (wait > 0) {
            wait--;
            return;
        }
        switch (stage) {
            case 0 -> {
                mc.getWindow().setWindowed(1280, 720);
                log("in world; settling");
                sample("world-joined", false);
                wait = SETTLE_TICKS;
                stage = 1;
            }
            case 1 -> {
                if (waitForPlayer != null) {
                    boolean present = mc.level.players().stream()
                        .anyMatch(other -> other.getGameProfile().name().equals(waitForPlayer));
                    if (!present && waitForTicks < 3600) {
                        if (waitForTicks == 0)
                            log("waiting for " + waitForPlayer + " to join");
                        waitForTicks += 20;
                        wait = 20;
                        return;
                    }
                    log(present ? waitForPlayer + " joined; starting" : waitForPlayer + " never joined; starting anyway");
                    waitForPlayer = null;
                    wait = 60;
                    return;
                }
                command("gamerule advance_time false");
                command("gamerule advance_weather false");
                command("time set 6000");
                command("weather clear");
                command("tp @s 0.5 -60 0.5 0 0");
                setHudHidden(mc, true);
                mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
                sample("before-models", true);
                wait = 40;
                stage = configProbe != null ? 40 : observeTarget != null ? 30 : scenarioFile != null ? 20 : 2;
                if (observeTarget != null) {
                    mc.options.setCameraType(CameraType.FIRST_PERSON);
                    if (observeHud)
                        setHudHidden(mc, false);
                    log("observing " + observeTarget + " every " + observeEvery + " ticks");
                }
            }
            case 20 -> scenarioTick(mc);
            case 40 -> configProbeTick(mc);
            case 30 -> observeTick(mc);
            case 2 -> {
                if (index >= models.size()) {
                    stage = 5;
                    return;
                }
                // A standing body keeps its spawn yaw (within 50 degrees) after the tp; square it with the head so
                // shots compare with the 2.6.5 references.
                mc.player.setYBodyRot(mc.player.getYRot());
                mc.player.yBodyRotO = mc.player.yBodyRot;
                command("ysm model set @s " + prefix + models.get(index) + " -");
                wait = LOAD_TICKS;
                stage = 3;
            }
            case 3 -> {
                var name = String.format(Locale.ROOT, "%02d_%s", index, models.get(index).replace('/', '_'));
                shot(name + "_front");
                sample("model " + models.get(index), false);
                var cap = ICapabilityHolder.of(mc.player).getCapability(PlayerAnimatableCapabilityProvider.CAP).orElse(null);
                if (cap != null) {
                    var failure = cap.modelLoadFailure();
                    log("installed=" + cap.getInstalledModelId() + " failure=" + (failure == null ? "none" : failure));
                    if (failure != null) {
                        for (Throwable c = failure; c != null; c = c.getCause())
                            log("  cause: " + c);
                        var text = new java.io.StringWriter();
                        failure.printStackTrace(new java.io.PrintWriter(text));
                        var lines = text.toString().split("\\R");
                        for (int i = 0; i < Math.min(lines.length, 60); i++)
                            log("  " + lines[i]);
                    }
                } else {
                    log("no player capability");
                }
                mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
                wait = 10;
                stage = 4;
            }
            case 4 -> {
                var name = String.format(Locale.ROOT, "%02d_%s", index, models.get(index).replace('/', '_'));
                shot(name + "_back");
                mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
                index++;
                wait = 5;
                stage = 2;
            }
            case 5 -> {
                setHudHidden(mc, false);
                com.elfmcys.ysm.platform.McCompat.setScreen(new PlayerModelScreen());
                log("opened PlayerModelScreen");
                wait = 100;
                stage = 6;
            }
            case 6 -> {
                shot("model_screen");
                com.elfmcys.ysm.platform.McCompat.setScreen(null);
                setHudHidden(mc, true);
                sample("soak-start", true);
                log("in-game soak: switching every " + SOAK_SWITCH_TICKS + " ticks for " + soakTicks + " ticks");
                stage = soakTicks > 0 ? 7 : 8;
            }
            case 7 -> {
                if (soakTick % SOAK_SWITCH_TICKS == 0) {
                    command("ysm model set @s " + prefix + models.get(soakIndex % models.size()) + " -");
                    soakIndex++;
                }
                if (soakTick % SAMPLE_TICKS == 0)
                    sample("soak " + soakIndex, true);
                soakTick++;
                if (soakTick > soakTicks) {
                    sample("soak-end", true);
                    stage = 8;
                    wait = 40;
                }
            }
            case 8 -> {
                log("done; quitting");
                mc.stop();
                stage = 9;
            }
            default -> {
            }
        }
    }
}
