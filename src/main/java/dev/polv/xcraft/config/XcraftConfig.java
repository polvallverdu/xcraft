package dev.polv.xcraft.config;

import dev.polv.xcraft.Xcraft;
import eu.midnightdust.lib.config.MidnightConfig;

/**
 * Stored in {@code config/xcraft.json} by MidnightLib, which also provides the in-game config screen
 * (Mod Menu on Fabric, the mod list's Config button on Forge/NeoForge).
 */
public class XcraftConfig extends MidnightConfig {
	public static final String MULTIPLIERS = "multipliers";
	public static final int MIN = 1;
	public static final int MAX = 1000;

	@Entry(category = MULTIPLIERS, min = MIN, max = MAX) public static int oreDropMultiplier = 1;
	@Entry(category = MULTIPLIERS, min = MIN, max = MAX) public static int entityDropMultiplier = 1;
	@Entry(category = MULTIPLIERS, min = MIN, max = MAX) public static int xpMultiplier = 1;
	@Entry(category = MULTIPLIERS, min = MIN, max = MAX) public static int furnaceSpeedMultiplier = 1;

	public static void load() {
		MidnightConfig.init(Xcraft.MOD_ID, XcraftConfig.class);
		// The screen enforces the bounds, but the file can be edited by hand.
		oreDropMultiplier = clamp(oreDropMultiplier);
		entityDropMultiplier = clamp(entityDropMultiplier);
		xpMultiplier = clamp(xpMultiplier);
		furnaceSpeedMultiplier = clamp(furnaceSpeedMultiplier);
	}

	public static void save() {
		MidnightConfig.write(Xcraft.MOD_ID);
	}

	private static int clamp(int value) {
		return Math.max(MIN, Math.min(MAX, value));
	}

	public static String summary() {
		return "ores x" + oreDropMultiplier + ", mobs x" + entityDropMultiplier
			+ ", xp x" + xpMultiplier + ", furnace x" + furnaceSpeedMultiplier;
	}
}
