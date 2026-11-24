# ysm-fabric

An unofficial port of the open-source **Yes Steve Model 3.0-dev** to **Minecraft 26.1, 26.1.1, 26.1.2, 26.2 and
26.3**, on **Fabric, NeoForge and Forge**. That makes 15 builds. The repository name dates from the first release,
which was Fabric 26.2 only.

> **This is not an official Yes Steve Model release.** It is not affiliated with or endorsed by the Yes Steve Model
> authors (YS Group). "Yes Steve Model" and "YSM" are the names of their project. Please report problems with this
> port here, not to the YSM authors.

## What this is

- The source is [YesSteveModel/YesSteveModel](https://github.com/YesSteveModel/YesSteveModel), branch `dev/1.20`,
  commit `3ec7068136f9a6a07d0eb227d69b20b8bcc00d85`, released by its authors under the Apache License 2.0. The original README is kept as
  [README.upstream.md](README.upstream.md).
- The Forge 1.20.1 code was ported to Minecraft 26.x:
  - a small platform layer for Forge-style events, capabilities, networking and config, with glue code for each
    loader;
  - the 26.x render pipeline (extract, submit, deferred draw);
  - the 26.x GUI (render-state extraction, picture-in-picture model previews);
  - one source tree for all five Minecraft versions, with version-specific lines selected at build time.
- Behaviour follows the released YSM 2.6.5 where 3.0-dev differs. For example, models without `origin_ver` are treated
  as 2.6.5 models, and maces and spears get their own item classes. No code from released YSM builds is included.
- [PORTING.md](PORTING.md) describes the changes and lists every modified, added, renamed and deleted file.

## Status

Experimental. Upstream 3.0-dev is itself unfinished: its formats, schemas and caches are not frozen and may change
without backward compatibility (see README.upstream.md).

**Tested in game on all 15 builds** (Windows x64):
- an in-game suite of about 120 scenario screenshots, compared side by side with YSM 2.6.5 on 26.2 and between all
  builds. It covers:
  - built-in models, model switching and world rendering;
  - the model screen and first person;
  - the HUD player preview;
  - items, riding and vehicles;
  - emotes and menus;
- the release jar on a real dedicated server installed with the loader's official installer, with a client playing
  the suite against it: other players' models and animations, and a server-only model sent to the client;
- the community model collection [Elaina69/Yes-Steve-Model-Repo](https://github.com/Elaina69/Yes-Steve-Model-Repo):
  1,121 models load. The 32 files that do not are 26 duplicates of a model already loaded and 6 archives with file
  names in a legacy Chinese encoding, which YSM 2.6.5 also rejects. These models are not included here;
- config screen settings saved and restored after a restart.

**Integrations tested in game.** "All" means all five Minecraft versions. Each mod was tested where it has a build
that loads; a dash means the mod has no build for that loader.

| Mod | Fabric | NeoForge | Forge |
| --- | --- | --- | --- |
| Carry On | all | 26.1.2, 26.2, 26.3 | 26.1.2, 26.2, 26.3 |
| Better Combat (with Player Animation Library) | all | all | - |
| First-person Model | all | 26.1.2, 26.2, 26.3 | - |
| Real Camera (classic mode) | all | all | - |
| Iris with Sodium and a shader pack | all | 26.1, 26.1.1, 26.1.2 | - |
| WTHIT | all | all | 26.1.1, 26.1.2, 26.2, 26.3 |
| Jade | all | 26.1.2, 26.2, 26.3 | - |
| Immersive Melodies, Immersive Aircraft | 26.1.2, 26.2 | 26.1.2, 26.2 | - |

The Fabric 26.2 build was also run with Mod Menu and about 30 other common mods (EMF/ETF, Flashback, Voxy, C2ME and
others).

Where a version is missing from a cell, no build of that mod loads there. Some builds are listed for older versions
on Modrinth but are really made for a newer version:
- Carry On on NeoForge and Forge 26.1 and 26.1.1;
- WTHIT on Forge 26.1;
- Jade on NeoForge 26.1 and 26.1.1.

**Known problems in other mods:**
- **Iris on NeoForge 26.2:** Iris 1.11.4 with Sodium 0.9.2 crashes on the first frame in a world. It crashes the same
  way without YSM installed.
- **Iris on NeoForge 26.3:** no Iris and Sodium pair that works together exists yet.
  - Sodium 0.9.2, which Iris 1.11.7 requires, fails to start on NeoForge 26.3.
  - Newer Sodium builds require Iris 1.12.1 or later, which has not been released for 26.3.

**Not done yet:**
- integrations whose mods have no 26.x version (Elytra Slot, Trinkets, Accessories, and the Forge 1.20-only mods);
  their compat modules remain inactive stand-ins;
- on Forge, when another mod fails to load, the game can crash in a YSM mixin instead of showing Forge's error screen
  (seen in development runs);
- macOS and Linux builds.

## Built-in models

Only the five built-in models licensed CC0 are included: `default`, `misc/1_alex`, `misc/2_steve`,
`misc/3_default_boy` and `misc/4_default_controllers`.

The Wine Fox models in the upstream repository are licensed CC BY-NC-SA 4.0 or All Rights Reserved, and they are not
redistributed here. Each model's license is in its `ysm.json` (`metadata.license`).

## Requirements

- Java 25.
- One of:
  - **Fabric:** Fabric Loader 0.20.1 or newer and Fabric API. Forge Config API Port is bundled inside the jar.
  - **NeoForge:** 26.1.0 or newer for 26.1 and 26.1.2, 26.1.1 for 26.1.1, 26.2.0 for 26.2, and 26.3.0 for 26.3.
    Forge Config API Port is bundled inside the jar.
  - **Forge:** 62 or newer for 26.1, 26.1.1 and 26.1.2, 65 for 26.2, and 66 for 26.3.
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
   The same library serves all 15 builds.
2. Copy the library to `src/main/resources/META-INF/native/` (for example `ysm.dll`). Only a library placed there is
   packed into the mod jar. For tests and dev runs, setting `YSM_NATIVE_PATH` or `ysm.native_path=<absolute path>`
   in `local.properties` is enough.
3. Run `./gradlew assemble -Pmc=<version> -Ploader=<loader>` with JDK 25:
   - `<version>` is `26.1`, `26.1.1`, `26.1.2`, `26.2` or `26.3`;
   - `<loader>` is `fabric`, `neoforge` or `forge`;
   - without them, the build is Fabric 26.2.
   The mod jar is written to `build/libs/`.
   - Each 26.1.x point release is compiled separately. Build the jar for the version you play.
   - The jar build fails if a bundled library is not relocated under `com.elfmcys`, so it cannot clash with
     other mods.

`./gradlew test` (part of `./gradlew build`) runs about 1,400 upstream tests and passes. PORTING.md describes the
tests that were fixed for this port. Some tests are skipped:

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
