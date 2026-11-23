# ysm-fabric

An unofficial port of the open-source **Yes Steve Model 3.0-dev** to **Minecraft 26.2 on Fabric**.

> **This is not an official Yes Steve Model release.** It is not affiliated with or endorsed by the Yes Steve Model
> authors (YS Group). "Yes Steve Model" and "YSM" are the names of their project. Please report problems with this
> port here, not to the YSM authors.

## What this is

- The source is [YesSteveModel/YesSteveModel](https://github.com/YesSteveModel/YesSteveModel), branch `dev/1.20`,
  commit `3ec7068136f9a6a07d0eb227d69b20b8bcc00d85`, released by its authors under the Apache License 2.0. The original README is kept as
  [README.upstream.md](README.upstream.md).
- The Forge 1.20.1 code was ported to Fabric 26.2:
  - a small platform layer for Forge-style events, capabilities, networking and config;
  - the 26.x render pipeline (extract, submit, deferred draw);
  - the 26.x GUI (render-state extraction, picture-in-picture model previews);
  - Fabric mixins.
- Behaviour follows the released YSM 2.6.5 where 3.0-dev differs. For example, models without `origin_ver` are treated
  as 2.6.5 models, and maces and spears get their own item classes. No code from released YSM builds is included.
- [PORTING.md](PORTING.md) describes the changes and lists every modified, added, renamed and deleted file.

## Status

Experimental. Upstream 3.0-dev is itself unfinished: its formats, schemas and caches are not frozen and may change
without backward compatibility (see README.upstream.md).

**Tested in game** on 26.2 (Windows x64):
- loading and switching the built-in models;
- world rendering;
- the model screen;
- a 10-minute model-switching soak;
- scenario screenshots compared side by side with YSM 2.6.5 (movement, items, riding, vehicles, first person, HUD,
  emotes, menus);
- a dedicated server with one and with two clients: other players' models and animations, and a server-only model
  sent to a client;
- Iris 1.11 with Sodium and a shader pack;
- alongside about 30 common 26.2 mods (EMF/ETF, Flashback, Voxy, C2ME, WTHIT and others).

- integrations with the 26.2 Fabric versions of Iris, Mod Menu, WTHIT, First-person Model, Real Camera, Better
  Combat (with Player Animation Library), Carry On, Immersive Melodies and Immersive Aircraft.

**Not done yet:**
- integrations whose mods have no 26.2 Fabric version (Elytra Slot, Trinkets, Accessories, and the Forge-only
  mods); their compat modules remain inactive stand-ins;
- macOS and Linux builds.

## Built-in models

Only the five built-in models licensed CC0 are included: `default`, `misc/1_alex`, `misc/2_steve`,
`misc/3_default_boy` and `misc/4_default_controllers`.

The Wine Fox models in the upstream repository are licensed CC BY-NC-SA 4.0 or All Rights Reserved, and they are not
redistributed here. Each model's license is in its `ysm.json` (`metadata.license`).

## Requirements

- Minecraft 26.2, Fabric Loader 0.20.1 or newer, Fabric API, Java 25.
- Forge Config API Port is bundled inside the jar.
- The YSM native library (`ysm.dll`, `libysm.so` or `libysm.dylib`), built from
  [YesSteveModel/YesSteveModel-Native](https://github.com/YesSteveModel/YesSteveModel-Native).

## Building

The port's own files are All Rights Reserved (see [License](#license)), so building or running this port needs the
author's permission. The steps are documented for reference.

1. Build the native library from YesSteveModel-Native, commit `9d92c175cb3c4b8db4ecb334281bd13d0b224f47`.
   First apply `native-patches/port.patch` (`git apply native-patches/port.patch` in the native checkout). The
   patch:
   - enables big object files for MSVC;
   - contains two MSVC compile fixes;
   - fixes JNI entry points that MSVC merged across translation units, which made one native function run another;
   - adds the entity vertex layout of Iris 1.11 on 26.x, which the port selects when Iris is installed.
2. Copy the library to `src/main/resources/META-INF/native/` (for example `ysm.dll`). Only a library placed there is
   packed into the mod jar. For tests and dev runs, setting `YSM_NATIVE_PATH` or `ysm.native_path=<absolute path>`
   in `local.properties` is enough.
3. Run `./gradlew assemble` with JDK 25. The mod jar is written to `build/libs/`.

`./gradlew test` (part of `./gradlew build`) runs about 1,300 upstream tests and currently reports 6 known failures;
PORTING.md lists them and their causes. Some tests are skipped:

- tests bound to Forge-era Minecraft APIs (`gradle/test-exclusions.txt`);
- tests that read the Wine Fox models, which are absent here (`gradle/test-exclusions-winefox.txt`).

## License

This repository is **mixed-license**. [LICENSE-PORT.md](LICENSE-PORT.md) has the details and the full file list.

- **Upstream code:** the Yes Steve Model 3.0-dev code, including the files changed for this port, is under the
  Apache License 2.0 ([LICENSE](LICENSE)). Changed files carry a notice at the top, and PORTING.md lists them all.
- **New port code:** the Java files written new for this port are **Copyright (c) 2026 GuoWQ222, All Rights
  Reserved**. They are not Apache-licensed, and each says so in its header. You may view them here, but you need the
  author's written permission to copy, modify, build, run or distribute them.
- **Third-party code:** [NOTICE.md](NOTICE.md) (from upstream) lists the third-party code and its licenses. The license
  texts are in `src/main/resources/licenses/`.
- **Built-in model assets:** these are not covered by the Apache license. Each model has its own license, as
  described in NOTICE.md. All models included here are CC0.
- **`native-patches/`:** these are changes to YesSteveModel-Native and are Apache-2.0.
