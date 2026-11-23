/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.platform;

import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.server.permissions.Permissions;

/** Fabric port: 1.20's numeric permission levels on top of 26.2's permission sets. */
public final class PermissionHelper {
    private PermissionHelper() {}

    public static Permission forLevel(int level) {
        return switch (level) {
            case 0 -> null;
            case 1 -> Permissions.COMMANDS_MODERATOR;
            case 2 -> Permissions.COMMANDS_GAMEMASTER;
            case 3 -> Permissions.COMMANDS_ADMIN;
            default -> Permissions.COMMANDS_OWNER;
        };
    }

    public static boolean has(PermissionSet permissions, int level) {
        var permission = forLevel(level);
        return permission == null || permissions.hasPermission(permission);
    }
}
