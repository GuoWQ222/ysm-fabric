/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.platform.event.forge;

import com.elfmcys.ysm.platform.event.Event;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

/** Fabric port: posted from Fabric's CommandRegistrationCallback. */
public class RegisterCommandsEvent extends Event {
    private final CommandDispatcher<CommandSourceStack> dispatcher;
    private final CommandBuildContext context;
    private final Commands.CommandSelection selection;

    public RegisterCommandsEvent(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context,
        Commands.CommandSelection selection) {
        this.dispatcher = dispatcher;
        this.context = context;
        this.selection = selection;
    }

    public CommandDispatcher<CommandSourceStack> getDispatcher() {
        return dispatcher;
    }

    public CommandBuildContext getBuildContext() {
        return context;
    }

    public Commands.CommandSelection getCommandSelection() {
        return selection;
    }
}
