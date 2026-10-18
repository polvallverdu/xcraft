<div align="center">

<img src=".github/assets/banner.png" alt="xCraft — Multiply drops, XP & smelting speed" width="820">

**Skip the grind. Mine one ore, get four diamonds.**<br>
A lightweight, server-side mod that multiplies ore drops, mob drops, XP and furnace speed.

[![Modrinth downloads](https://img.shields.io/modrinth/dt/xcraft_real?logo=modrinth&label=Modrinth&color=00AF5C)](https://modrinth.com/mod/xcraft_real)
[![GitHub release](https://img.shields.io/github/v/release/polvallverdu/xcraft?logo=github&label=Release&color=1b97ad)](https://github.com/polvallverdu/xcraft/releases)
[![Build](https://img.shields.io/github/actions/workflow/status/polvallverdu/xcraft/build.yml?branch=main&logo=githubactions&logoColor=white&label=Build)](https://github.com/polvallverdu/xcraft/actions/workflows/build.yml)
[![License](https://img.shields.io/github/license/polvallverdu/xcraft?color=ff9a1f&label=License)](LICENSE.txt)

![Minecraft](https://img.shields.io/badge/Minecraft-1.7.2_→_26.3-3fdcdc?style=flat-square)
![Fabric](https://img.shields.io/badge/Fabric-supported-dbd0b4?style=flat-square)
![NeoForge](https://img.shields.io/badge/NeoForge-supported-f16436?style=flat-square)
![Forge](https://img.shields.io/badge/Forge-1.17.1_→_1.20.1-1e2d40?style=flat-square)

</div>

---

## ✨ Features

<table>
<tr>
<td width="72" align="center"><img src=".github/assets/feature-ores.png" width="56" alt=""></td>
<td>

### Ore drops multiplier
Every block that is an ore (`*_ore`, or tagged `c:ores` / `forge:ores`, so modded ores work too) drops **×N** its normal loot when mined. It stacks on top of Fortune, and **Silk Touch is left alone**, so you won't get stacks of ore blocks.

<details><summary>Watch it in action</summary>

https://github.com/user-attachments/assets/5d976689-09b9-4892-acba-49f4bda2ba78
</details>
</td>
</tr>
<tr>
<td align="center"><img src=".github/assets/feature-mobs.png" width="56" alt=""></td>
<td>

### Mob drops multiplier
Every mob's loot table drops **×N** items. Player inventories are never duplicated.

<details><summary>Watch it in action</summary>

https://github.com/user-attachments/assets/36f655c1-0416-4e70-ac5b-02a376f5ecdb
</details>
</td>
</tr>
<tr>
<td align="center"><img src=".github/assets/feature-xp.png" width="56" alt=""></td>
<td>

### XP multiplier
Experience orbs are worth **×N** when picked up. The boost also works with Mending repairs.

<details><summary>Watch it in action</summary>

https://github.com/user-attachments/assets/0c60c929-d51f-460d-a582-5975fcc12a12
</details>
</td>
</tr>
<tr>
<td align="center"><img src=".github/assets/feature-furnace.png" width="56" alt=""></td>
<td>

### Furnace speed multiplier
Furnaces, blast furnaces and smokers cook **×N** faster. Outputs, recipe XP and hoppers keep working as in vanilla.

<details><summary>Watch it in action</summary>

https://github.com/user-attachments/assets/89ecc56a-2ee7-4ffb-b339-367703afd615
</details>
</td>
</tr>
</table>

> [!TIP]
> xCraft is **server-side only**. Install it on a server and players can join with a plain vanilla client. It also works in singleplayer.

## 🎮 Commands

All commands need operator permissions (level 2). Changes apply instantly and are saved to the config file.

| Command                         | What it does                                  |
|---------------------------------|-----------------------------------------------|
| `/xcraft`                       | Show all current multipliers                  |
| `/xcraft <ores\|mobs\|xp\|furnace>` | Show one multiplier                           |
| `/xcraft <ores\|mobs\|xp\|furnace> <1-1000>` | Set a multiplier, e.g. `/xcraft ores 4`  |
| `/xcraft reload`                | Re-read `config/xcraft.json` from disk         |

## ⚙️ Configuration

**In game:** open the mod list and click **Config** next to xCraft. On Fabric this needs [Mod Menu](https://modrinth.com/mod/modmenu); Forge and NeoForge have the button built in. The screen needs [MidnightLib](https://modrinth.com/mod/midnightlib) installed; without it, use the commands or the file.

**On a server:** use the `/xcraft` commands above, or edit `config/xcraft.json`, which is created on first launch. Every value defaults to `1` (vanilla).

```json
{
  "oreDropMultiplier": 4,
  "entityDropMultiplier": 2,
  "xpMultiplier": 3,
  "furnaceSpeedMultiplier": 8
}
```

Config files from xCraft 1.x keep working with no changes.

## 📦 Supported versions

**Every Minecraft release from 1.7.2 to 26.3** (84 versions). Each one is booted on a real server in CI with the published jar.

| Minecraft | Loaders |
|---|---|
| 1.20.2 → 26.3 | Fabric, NeoForge |
| 1.17.1 → 1.20.1 | Fabric, Forge |
| 1.14 → 1.17 | Fabric |
| 1.7.2 → 1.13.2 | [Legacy Fabric](https://legacyfabric.net/) (1.7.x, 1.8.x, 1.9.4, 1.10.2, 1.11.2, 1.12.2, 1.13.2) or [Ornithe](https://ornithemc.net/) (1.9–1.9.3, 1.10–1.10.1, 1.11–1.11.1, 1.12–1.12.1, 1.13–1.13.1) |

xCraft has **no required dependencies**: [MidnightLib](https://modrinth.com/mod/midnightlib) is optional and only adds the in-game config screen (1.17.1+).

## 📥 Installation

1. Install the loader for your Minecraft version: [Fabric](https://fabricmc.net/use/), [NeoForge](https://neoforged.net/), [Forge](https://files.minecraftforge.net/), [Legacy Fabric](https://legacyfabric.net/) or [Ornithe](https://ornithemc.net/). Optionally add [MidnightLib](https://modrinth.com/mod/midnightlib) (and [Mod Menu](https://modrinth.com/mod/modmenu) on Fabric) for the config screen.
2. Download the jar for your version and loader from [Modrinth](https://modrinth.com/mod/xcraft_real) or [GitHub Releases](https://github.com/polvallverdu/xcraft/releases).
3. Drop it into your `mods/` folder (the server's folder for multiplayer) and start the game.

## 🤝 Contributing

Want to build xCraft yourself, add a Minecraft version or cut a release? See [CONTRIBUTING.md](CONTRIBUTING.md).

## 💡 Planned

- Per-player / permission-based multipliers
- Per-ore and per-mob multipliers
- More multipliers ([suggestions welcome!](https://github.com/polvallverdu/xcraft/issues))

## 📄 License

[Apache 2.0](LICENSE.txt). Feel free to use xCraft in any modpack; a credit is appreciated.

<div align="center">

Made by [Pol Vallverdu](https://github.com/polvallverdu) · [☕ Support on Ko-fi](https://ko-fi.com/pol_vallverdu)

</div>
