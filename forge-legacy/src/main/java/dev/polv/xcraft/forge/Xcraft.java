package dev.polv.xcraft.forge;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;

/** Shared state of xCraft for Forge 1.7.2 - 1.16.5. The loader entrypoint is XcraftMod (fml/ or ml/). */
public final class Xcraft {
	public static final String MOD_ID = "xcraft";
	public static final String VERSION = "2.1.0";
	public static final Logger LOGGER = LogManager.getLogger("xCraft");
	/** -Dxcraft.debug=true logs every multiplication (used by the CI server tests: old versions can't read item NBT). */
	public static final boolean DEBUG = Boolean.getBoolean("xcraft.debug");

	private Xcraft() {
	}

	public static void debug(String message) {
		if (DEBUG) LOGGER.info("[xcraft-debug] " + message);
	}

	public static void init(Path configDir) {
		Config.init(configDir.resolve("xcraft.json"));
		// Plain concatenation: Minecraft 1.9-1.11 ship a Log4j beta without the (String, Object, Object) overload
		LOGGER.info("xCraft " + VERSION + " loaded (JSON config): " + Config.summary());
	}

	/**
	 * Vanilla adds 1 to the cook time each tick while smelting; add the remaining multiplier - 1, stopping one
	 * short of the total so vanilla still finishes the item on its next tick. Returns the new cook time.
	 */
	public static int boostCookTime(int burnTime, int cookTime, int totalCookTime) {
		int multiplier = Config.furnace();
		if (multiplier <= 1 || burnTime <= 0 || cookTime <= 0 || cookTime >= totalCookTime - 1) return cookTime;
		int boosted = Math.min(cookTime + multiplier - 1, totalCookTime - 1);
		debug("furnace x" + multiplier + ": cook " + boosted + "/" + totalCookTime);
		return boosted;
	}

	public static int multiplyXp(int amount) {
		return (int) Math.min(Integer.MAX_VALUE, (long) amount * Config.xp());
	}

	/** Splits {@code count * multiplier} items into stack sizes no larger than {@code max}. */
	public static int[] split(int count, int multiplier, int max) {
		long total = (long) count * multiplier;
		max = Math.max(1, max);
		int[] parts = new int[(int) ((total + max - 1) / max)];
		for (int i = 0; i < parts.length; i++) {
			parts[i] = (int) Math.min(total, max);
			total -= parts[i];
		}
		return parts;
	}
}
