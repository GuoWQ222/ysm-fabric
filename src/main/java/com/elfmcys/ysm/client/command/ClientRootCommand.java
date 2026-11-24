// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.command;

import com.elfmcys.ysm.platform.capability.ICapabilityHolder;
import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.capability.PlayerAnimatableCapabilityProvider;
import com.elfmcys.ysm.client.command.sub.DebugCommand;
import com.elfmcys.ysm.client.command.sub.MolangCommand;
import com.elfmcys.ysm.client.command.sub.SimpleWatchCommand;
import com.elfmcys.ysm.client.entity.AnimatableEntity;
import com.elfmcys.ysm.client.gui.overlay.DebugAnimationScreen;
import com.elfmcys.ysm.expression.host.RoamingMember;
import com.elfmcys.ysm.geckolib3.core.controller.IAnimationController;
import com.elfmcys.ysm.util.CommandUtil;
import com.google.common.collect.Sets;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.resources.Identifier;
import com.elfmcys.ysm.platform.Dist;
import com.elfmcys.ysm.platform.FMLEnvironment;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("removal")
public class ClientRootCommand {
    private static final String ROOT_NAME = "ysmclient";

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        LiteralArgumentBuilder<FabricClientCommandSource> root =
            ClientCommands.literal(ROOT_NAME).requires(source -> CommandUtil.isLocalPlayer(source.getEntity()));
        root.then(MolangCommand.get());
        root.then(SimpleWatchCommand.get());
        root.then(DebugCommand.get());
        root.then(com.elfmcys.ysm.client.command.sub.RoamingCommand.get());
        root.then(com.elfmcys.ysm.client.command.sub.ClientCacheCommand.get());
        dispatcher.register(root);
    }

    public static final SuggestionProvider<FabricClientCommandSource> ALL_VARS =
        (source, builder) -> {
            if (FMLEnvironment.dist == Dist.CLIENT) {
                return getTarget().map(cap -> {
                    cap.waitForAsyncUpdate();
                    var target = cap.getModelRenderTarget();
                    if (target == null)
                        return Suggestions.empty();
                    Set<String> vars = Sets.newHashSet();
                    // v.roaming 变量
                    if (cap.getExpressionRuntime().roamingState().isPresent()) {
                        for (var member : RoamingMember.values()) {
                            vars.add(String.format("v.roaming.%s", member.memberName()));
                        }
                    }

                    // 其他上下文变量
                    for (var symbol : cap.getExpressionRuntime().metadata()
                        .map(com.elfmcys.ysm.model.resource.client.AnimationStore.Metadata::publishedSymbols)
                        .orElse(java.util.List.of())) {
                        var sourceName = sourceName(symbol);
                        if (sourceName != null) {
                            vars.add(sourceName);
                        }
                    }

                    // 自定义函数
                    for (var name : target.animationStore().functions().keySet()) {
                        vars.add(String.format("fn.%s", name));
                    }

                    return SharedSuggestionProvider.suggest(vars, builder);
                }).orElseGet(Suggestions::empty);
            }
            return Suggestions.empty();
        };

    public static final SuggestionProvider<FabricClientCommandSource> ALL_CONTROLLERS =
        (source, builder) -> {
            if (FMLEnvironment.dist == Dist.CLIENT) {
                return getTarget().map(target -> {
                    target.waitForAsyncUpdate();
                    if (target.getModelRenderTarget() == null)
                        return Suggestions.empty();
                    Set<String> vars = target.getAnimationData().getAnimationControllers().stream()
                        .map(IAnimationController::getName).collect(Collectors.toSet());
                    return SharedSuggestionProvider.suggest(vars, builder);
                }).orElseGet(Suggestions::empty);
            }
            return Suggestions.empty();
        };

    private static Optional<AnimatableEntity<?>> getTarget() {
        var target = DebugAnimationScreen.getTarget();
        if (target == null) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                target = ICapabilityHolder.of(player).getCapability(PlayerAnimatableCapabilityProvider.CAP).orElse(null);
            }
        }
        return Optional.ofNullable(target);
    }

    static String sourceName(String stableId) {
        if (stableId.startsWith("query:")) {
            return "q." + stableId.substring("query:".length());
        }
        var delimiter = stableId.indexOf(':');
        if (delimiter < 0)
            return null;
        var root = stableId.substring(0, delimiter);
        var member = stableId.substring(delimiter + 1);
        return root.equals("global") ? member : root + '.' + member;
    }
}
