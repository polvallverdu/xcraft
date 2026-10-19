package dev.polv.xcraft.config;

import dev.polv.xcraft.Xcraft;

import java.nio.file.Path;

/**
 * Multiplier values, stored in {@code config/xcraft.json}. When MidnightLib is installed it manages the
 * file and adds an in-game config screen; otherwise xCraft reads and writes the file itself.
 */
public final class XcraftConfig {
	public static final int MIN = 1;
	public static final int MAX = 1000;

	private static ConfigStore store;

	private XcraftConfig() {
	}

	public static void init(Path configDir) {
		store = createMidnightStore();
		if (store == null) store = new JsonConfigStore(configDir.resolve(Xcraft.MOD_ID + ".json"));
		store.load();
	}

	/** The multiplier's current value, clamped to {@link #MIN}..{@link #MAX} (the file can be edited by hand). */
	public static int get(Multiplier multiplier) {
		if (store == null) return 1;
		return Math.max(MIN, Math.min(MAX, store.get(multiplier)));
	}

	public static void set(Multiplier multiplier, int value) {
		store.set(multiplier, value);
		store.save();
	}

	public static void reload() {
		store.load();
	}

	public static boolean usingMidnightLib() {
		return !(store instanceof JsonConfigStore);
	}

	public static String summary() {
		return "ores x" + get(Multiplier.ORES) + ", mobs x" + get(Multiplier.MOBS)
			+ ", xp x" + get(Multiplier.XP) + ", furnace x" + get(Multiplier.FURNACE);
	}

	private static ConfigStore createMidnightStore() {
		try {
			Class.forName("eu.midnightdust.lib.config.MidnightConfig", false, XcraftConfig.class.getClassLoader());
			Class<?> cls = Class.forName("dev.polv.xcraft.config.MidnightConfigStore");
			if (!ConfigStore.class.isAssignableFrom(cls)) return null;
			return (ConfigStore) cls.getDeclaredConstructor().newInstance();
		} catch (ClassNotFoundException e) {
			return null;
		} catch (ReflectiveOperationException | LinkageError e) {
			Xcraft.LOGGER.warn("MidnightLib is installed but could not be used; falling back to plain JSON config", e);
			return null;
		}
	}
}
