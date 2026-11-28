/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.legacy.input;

/** 1.21.9's key event record for older Minecraft (see InputWithModifiers). */
public record KeyEvent(int key, int scancode, int modifiers) implements InputWithModifiers {}
