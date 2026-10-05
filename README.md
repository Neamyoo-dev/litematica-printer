# Litematica Printer 4 (打印机四改)

**English** | [简体中文](README.zh_CN.md)

![Minecraft](https://img.shields.io/badge/Minecraft-26.2-blue)

A Minecraft Fabric mod that adds automatic schematic building to [Litematica](https://modrinth.com/mod/litematica). This project supports and receives updates for **Minecraft 26.2 only**. The default development branch is `ver/26.2`.

This fork continues the work of earlier Litematica Printer forks, focusing on missing placements in creative mode, incorrect hit positions, and stale scanning bounds. The project remains free and open source.

## Downloads and dependencies

Download a 26.2 build from this repository's [Releases](https://github.com/Neamyoo-dev/litematica-printer/releases), or build it yourself.

Required mods:

- [Fabric API](https://modrinth.com/mod/fabric-api)
- [MaLiLib](https://modrinth.com/mod/malilib)
- [Litematica](https://modrinth.com/mod/litematica)

Optional mods:

- [Tweakeroo](https://modrinth.com/mod/tweakeroo): block breaking restrictions and automatic tool selection.
- [Quick Shulker](https://modrinth.com/mod/quick-shulker) or AxShulkers: material retrieval from shulker boxes.
- [Fabric-Bedrock-Miner](https://github.com/bunnyi116/fabric-bedrock-miner): bedrock breaking support.
- Remote Inventory Next: material retrieval from remote containers and returning items afterward.

Use versions of all dependencies that are compatible with Minecraft 26.2.

## Features

- Automatically print schematics, adjust block states, and remove incorrect or extra blocks.
- Fill selections, optionally destroying obstacles first; remove fluids, grow crops, and break bedrock.
- Packet printing mode, lag detection, and 48 iteration orders for the working area.
- Progress display, missing material notifications, and highlights for blocks being processed.
- Automatic material retrieval from shulker boxes and remote containers, with ordered item return.
- Placement orientation for stairs, doors, trapdoors, hoppers, banners, skulls, and other special blocks.
- Interfaces in Simplified Chinese, Traditional Chinese, Classical Chinese, English, and Russian.

## Usage

1. Load a schematic and enable the placement and subregions you want to print.
2. Move within interaction range of the target blocks.
3. Press `Caps Lock` to enable the printer.
4. Adjust speed, working range, and visible layers in the printer settings as needed.

Most settings include explanations in game. Creative mode automatically picks materials; if no pick block hotbar slots are configured, it uses the selected slot.

To replace obstacles while filling a selection, enable **Destroy Blocking Blocks** under **Fill**. This option is disabled by default. Creative mode instantly breaks obsidian and other breakable obstacles before placing the fill block. Speed follows the placement interval and blocks per tick settings. Survival mode uses normal mining speed and restores the fill material after mining. Blocks whose types are already in the fill list are preserved to prevent repeated breaking and placement.

The option requires a block item and available fill materials. It respects the selection, working range, and block breaking restrictions. Packet breaking mode waits for the obstacle to disappear from the client world before filling. Servers may still reject breaking or placement in protected locations.

## Why are blue missing blocks left unfilled?

Blue indicates a missing block relative to the schematic. Placement still depends on the working range, layers, subregions, and block placement conditions. Server placement rules also apply in creative mode.

Fixes included in this branch:

- Air placement previously ignored existing support faces. Placement now prefers real support, falling back only when no support exists and air placement is enabled.
- Hit offsets were rotated using degrees as radians. Rotation now uses the correct radians.
- Reused scan bounds did not refresh the player's eye position. Reach checks now refresh every tick.
- Creative material selection failed when no pick block slots were configured. It can now use the selected hotbar slot.
- Ice for water mode skipped water placement in creative mode. Creative mode now uses normal water bucket placement.

If blocks are still missing, check:

- The printer toggle, skip lists, skip waterlogged blocks setting, schematic subregions, and visible layers.
- Whether torches, plants, and redstone have real support, and whether falling blocks have support below them.
- Player or entity collisions, server protection, anti-cheat rules, and placement rate limits.
- Try one placement per tick, increase the interval, and move closer to the missing block. Creative mode and a larger client working range cannot bypass server restrictions.

Not yet supported: filled cauldrons, lily pads, entities such as item frames, armor stands, and paintings, and some modded content.

Report problems in this repository's [Issues](https://github.com/Neamyoo-dev/litematica-printer/issues), including block names, printer settings, and whether you are playing in single player or on a server.

## Building

Use **JDK 25**. The build retrieves the 26.2 remote inventory dependency from a public release, without requiring upstream GitHub Packages credentials.

```bash
./gradlew :26.2:build
```

On Windows:

```powershell
.\gradlew.bat :26.2:build
```

Artifacts are written to `versions/26.2/build/libs/`. Put the regular mod jar in your game's `mods` directory. The build produces only the 26.2 mod, with no multi-version wrapper.

## Contributing

Repository working conventions are in [AGENTS.md](AGENTS.md). Use English [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/) for new commits, and update both the English and Simplified Chinese READMEs when changing documentation.

## Special thanks

- [aleksilassila](https://github.com/aleksilassila/litematica-printer): the original Litematica Printer.
- [zhaixianyu](https://github.com/zhaixianyu/litematica-printer): the second fork.
- [BiliXWhite / BlinkWhite](https://github.com/BiliXWhite/litematica-printer): the third fork and feature extensions.
- [bunny_i / bunnyi116](https://github.com/bunnyi116): development contributions and bedrock breaking support.
- [MoRanpcy](https://github.com/MoRanpcy/quickshulker): Quick Shulker support.
- [Rofumer](https://github.com/Rofumer): Russian localization, performance improvements, and fixes.
- [Cjsah](https://github.com/Cjsah): material identification in selected containers.
- [EnderPhantomWing](https://github.com/EnderPhantomWing-Fork): support for newer versions and fixes.

Thanks to everyone who has contributed or provided feedback. The project continues to use the [AGPL-3.0](LICENSE.md) license.
