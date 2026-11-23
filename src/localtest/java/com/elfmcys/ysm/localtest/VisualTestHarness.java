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

    private static void setHudHidden(Minecraft mc, boolean hidden) {
        if (mc.gui.hud.isHidden() != hidden)
            mc.gui.hud.toggle();
    }

    private static void onTick(Minecraft mc) {
        if (!enabled())
            return;
        if (!worldRequested && mc.gui.screen() instanceof TitleScreen title) {
            worldRequested = true;
            if (mc.getLevelSource().levelExists(WORLD)) {
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
                command("gamerule advance_time false");
                command("gamerule advance_weather false");
                command("time set 6000");
                command("weather clear");
                command("tp @s 0.5 -60 0.5 0 0");
                setHudHidden(mc, true);
                mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
                sample("before-models", true);
                wait = 40;
                stage = 2;
            }
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
