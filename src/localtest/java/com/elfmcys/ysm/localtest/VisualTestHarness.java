/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.localtest;

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
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
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
public final class VisualTestHarness implements ClientModInitializer {
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

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(VisualTestHarness::onTick);
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (enabled())
                log("chat: " + message.getString());
        });
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
                            case "server" -> serverAddress = kv[1].trim();
                            case "observe" -> observeTarget = kv[1].trim();
                            case "observeEvery" -> observeEvery = Integer.parseInt(kv[1].trim());
                            case "observeTicks" -> observeTicks = Integer.parseInt(kv[1].trim());
                            case "observeHud" -> observeHud = Boolean.parseBoolean(kv[1].trim());
                            case "waitFor" -> waitForPlayer = kv[1].trim();
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
        Screenshot.grab(mc.gameDirectory, "ysmtest_" + shotPrefix + name + ".png", mc.gameRenderer.mainRenderTarget(), 1,
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
            command("ysm model set @s " + (model.contains(":") ? model : prefix + model) + " -");
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
        return scenarioModels.get(scenarioModel).replace('/', '_').replace(':', '_');
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
                var ysm = player.getCapability(PlayerAnimatableCapabilityProvider.CAP).orElse(null);
                if (ysm != null)
                    log("  ysm: extra=" + ysm.isPlayingExtraAnimation() + " name=" + ysm.getExtraAnimationName());
                var vehicle = player.getVehicle();
                if (vehicle != null) {
                    var v = vehicle.getCapability(com.elfmcys.ysm.capability.VehicleAnimatableCapabilityProvider.CAP)
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
                var cap = player.getCapability(PlayerAnimatableCapabilityProvider.CAP).orElse(null);
                var failure = cap == null ? null : cap.modelLoadFailure();
                if (failure != null)
                    log("  model failure: " + failure);
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
                mapping.setDown(down);
                if (down)
                    net.minecraft.client.KeyMapping.click(
                        net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper.getBoundKeyOf(mapping));
            }
            case "swing" -> player.swing("off".equals(arg) ? net.minecraft.world.InteractionHand.OFF_HAND
                : net.minecraft.world.InteractionHand.MAIN_HAND);
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
            case "screen" -> mc.gui.setScreen(null);
            case "press" -> pressMapping(mc, arg);
            case "hud" -> setHudHidden(mc, !"show".equals(arg));
            case "log" -> log(String.format(java.util.Locale.ROOT,
                "%s: flying=%b mayfly=%b ground=%b y=%.3f dy=%.3f mode=%s",
                arg, player.getAbilities().flying, player.getAbilities().mayfly, player.onGround(), player.getY(),
                player.getDeltaMovement().y, mc.gameMode.getPlayerMode()));
            default -> log("unknown step: " + step);
        }
    }

private static void logState(Minecraft mc) {
        var player = mc.player;
        var vehicle = player.getVehicle();
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
            var key = net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper.getBoundKeyOf(mapping);
            try {
                var method = net.minecraft.client.KeyboardHandler.class.getDeclaredMethod("keyPress", long.class,
                    int.class, net.minecraft.client.input.KeyEvent.class);
                method.setAccessible(true);
                pretendFocused(mc);
                var event = new net.minecraft.client.input.KeyEvent(key.getValue(), 0, 0);
                method.invoke(mc.keyboardHandler, mc.getWindow().handle(), 1, event);
                method.invoke(mc.keyboardHandler, mc.getWindow().handle(), 0, event);
                releaseMouse(mc);
                log("  pressed " + name + " (" + key.getName() + ")");
            } catch (ReflectiveOperationException error) {
                log("  press failed " + name + ": " + error);
            }
            return;
        }
        log("  no key mapping " + name);
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
        var ysm = target.getCapability(PlayerAnimatableCapabilityProvider.CAP).orElse(null);
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

    private static void setHudHidden(Minecraft mc, boolean hidden) {
        if (mc.gui.hud.isHidden() != hidden)
            mc.gui.hud.toggle();
    }

    private static void onTick(Minecraft mc) {
        if (!enabled())
            return;
        if (!worldRequested && mc.gui.screen() instanceof TitleScreen title) {
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
                stage = observeTarget != null ? 30 : scenarioFile != null ? 20 : 2;
                if (observeTarget != null) {
                    mc.options.setCameraType(CameraType.FIRST_PERSON);
                    if (observeHud)
                        setHudHidden(mc, false);
                    log("observing " + observeTarget + " every " + observeEvery + " ticks");
                }
            }
            case 20 -> scenarioTick(mc);
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
                var cap = mc.player.getCapability(PlayerAnimatableCapabilityProvider.CAP).orElse(null);
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
                mc.gui.setScreen(new PlayerModelScreen());
                log("opened PlayerModelScreen");
                wait = 100;
                stage = 6;
            }
            case 6 -> {
                shot("model_screen");
                mc.gui.setScreen(null);
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
