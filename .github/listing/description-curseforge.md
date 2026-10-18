![xCraft — Multiply drops, XP & smelting speed](https://raw.githubusercontent.com/polvallverdu/xcraft/main/.github/assets/banner.png)

**Skip the grind. Mine one ore, get four diamonds.**
A lightweight, server-side mod that multiplies ore drops, mob drops, XP and furnace speed.

[![GitHub release](https://img.shields.io/github/v/release/polvallverdu/xcraft?logo=github&label=Release&color=1b97ad)](https://github.com/polvallverdu/xcraft/releases)
[![License](https://img.shields.io/github/license/polvallverdu/xcraft?color=ff9a1f&label=License)](https://github.com/polvallverdu/xcraft/blob/main/LICENSE.txt)
![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1_→_26.3-3fdcdc?style=flat-square)
![Fabric](https://img.shields.io/badge/Fabric-supported-dbd0b4?style=flat-square)
![NeoForge](https://img.shields.io/badge/NeoForge-supported-f16436?style=flat-square)
![Forge](https://img.shields.io/badge/Forge-1.20.1-1e2d40?style=flat-square)

---

## ✨ Features

### ![](https://raw.githubusercontent.com/polvallverdu/xcraft/main/.github/assets/feature-ores.png) Ore drops multiplier
Every block that is an ore (`*_ore`, or tagged `c:ores` / `forge:ores`, so modded ores work too) drops **×N** its normal loot when mined. It stacks on top of Fortune, and **Silk Touch is left alone**, so you won't get stacks of ore blocks.

[Watch it in action](https://github.com/user-attachments/assets/5d976689-09b9-4892-acba-49f4bda2ba78)

### ![](https://raw.githubusercontent.com/polvallverdu/xcraft/main/.github/assets/feature-mobs.png) Mob drops multiplier
Every mob's loot table drops **×N** items. Player inventories are never duplicated.

[Watch it in action](https://github.com/user-attachments/assets/36f655c1-0416-4e70-ac5b-02a376f5ecdb)

### ![](https://raw.githubusercontent.com/polvallverdu/xcraft/main/.github/assets/feature-xp.png) XP multiplier
Experience orbs are worth **×N** when picked up. The boost also works with Mending repairs.

[Watch it in action](https://github.com/user-attachments/assets/0c60c929-d51f-460d-a582-5975fcc12a12)

### ![](https://raw.githubusercontent.com/polvallverdu/xcraft/main/.github/assets/feature-furnace.png) Furnace speed multiplier
Furnaces, blast furnaces and smokers cook **×N** faster. Outputs, recipe XP and hoppers keep working as in vanilla.

[Watch it in action](https://github.com/user-attachments/assets/89ecc56a-2ee7-4ffb-b339-367703afd615)


> 💡 xCraft is **server-side only**. Install it (with MidnightLib) on a server and players can join with a plain vanilla client. It also works in singleplayer.

## 🎮 Commands

All commands need operator permissions (level 2). Changes apply instantly and are saved to the config file.

| Command                         | What it does                                  |
|---------------------------------|-----------------------------------------------|
| `/xcraft`                       | Show all current multipliers                  |
| `/xcraft <ores\|mobs\|xp\|furnace>` | Show one multiplier                           |
| `/xcraft <ores\|mobs\|xp\|furnace> <1-1000>` | Set a multiplier, e.g. `/xcraft ores 4`  |
| `/xcraft reload`                | Re-read `config/xcraft.json` from disk         |

## ⚙️ Configuration

**In game:** open the mod list and click **Config** next to xCraft. On Fabric this needs [Mod Menu](https://modrinth.com/mod/modmenu); Forge and NeoForge have the button built in. The screen is provided by [MidnightLib](https://modrinth.com/mod/midnightlib).

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

| Minecraft      | Fabric | NeoForge | Forge |
|----------------|:------:|:--------:|:-----:|
| 26.3           | ✅ | ✅ |   |
| 26.1 – 26.1.2  | ✅ | ✅ |   |
| 1.21.11        | ✅ | ✅ |   |
| 1.21.1         | ✅ | ✅ |   |
| 1.20.1         | ✅ |    | ✅ |

Every build needs [MidnightLib](https://modrinth.com/mod/midnightlib), and the Fabric builds also need [Fabric API](https://modrinth.com/mod/fabric-api).

## 📥 Installation

1. Install [Fabric](https://fabricmc.net/use/), [NeoForge](https://neoforged.net/) or [Forge](https://files.minecraftforge.net/) for your Minecraft version, plus [MidnightLib](https://modrinth.com/mod/midnightlib). On Fabric, also install [Fabric API](https://modrinth.com/mod/fabric-api), and [Mod Menu](https://modrinth.com/mod/modmenu) if you want the config screen.
2. Download the jar for your version and loader from [Modrinth](https://modrinth.com/mod/xcraft_real) or [GitHub Releases](https://github.com/polvallverdu/xcraft/releases).
3. Drop it into your `mods/` folder (the server's folder for multiplayer) and start the game.

## 💡 Planned

- Per-player / permission-based multipliers
- Per-ore and per-mob multipliers
- More multipliers ([suggestions welcome!](https://github.com/polvallverdu/xcraft/issues))

## 📄 License

[Apache 2.0](https://github.com/polvallverdu/xcraft/blob/main/LICENSE.txt). Feel free to use xCraft in any modpack; a credit is appreciated.

Made by [Pol Vallverdu](https://github.com/polvallverdu) · [☕ Support on Ko-fi](https://ko-fi.com/pol_vallverdu)
