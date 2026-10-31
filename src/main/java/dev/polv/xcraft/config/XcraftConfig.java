package dev.polv.xcraft.config;

import dev.polv.xcraft.Xcraft;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Multiplier values, stored in {@code config/xcraft.json}. When MidnightLib is installed it manages the
 * file and adds an in-game config screen; otherwise xCraft reads and writes the file itself.
 * <p>
 * A mob's drops use its own entry in {@link Overrides#MOB} if it has one, else the hostile or passive multiplier
 * if that is set (not 0), else the mobs multiplier. A block's drops use its own entry in {@link Overrides#BLOCK}
 * (any block, not only ores) if it has one, else the ores multiplier for ores.
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
		warnInvalidEntries();
	}

	/** The multiplier's current value, clamped to its min..{@link #MAX} (the file can be edited by hand). */
	public static int get(Multiplier multiplier) {
		if (store == null) return multiplier.min;
		return Math.max(multiplier.min, Math.min(MAX, store.get(multiplier)));
	}

	public static void set(Multiplier multiplier, int value) {
		store.set(multiplier, value);
		store.save();
	}

	/** The drop multiplier of a mob, by its id (e.g. "minecraft:zombie") and whether it is hostile. */
	public static int mobMultiplier(String id, boolean hostile) {
		Integer own = override(Overrides.MOB, id);
		if (own != null) return own;
		int category = get(hostile ? Multiplier.HOSTILE : Multiplier.PASSIVE);
		return category > 0 ? category : get(Multiplier.MOBS);
	}

	/** The drop multiplier of a block, by its id (e.g. "minecraft:gravel") and whether it is an ore. */
	public static int blockMultiplier(String id, boolean ore) {
		Integer own = override(Overrides.BLOCK, id);
		if (own != null) return own;
		return ore ? get(Multiplier.ORES) : 1;
	}

	/** The override for {@code id}, or null. Ids match ignoring case and the "minecraft:" namespace. */
	public static Integer override(Overrides list, String id) {
		if (id == null) return null;
		String key = normalize(id);
		for (Map.Entry<String, Integer> entry : overrides(list).entrySet()) {
			if (normalize(entry.getKey()).equals(key)) return entry.getValue();
		}
		return null;
	}

	/**
	 * The valid {@code "id=multiplier"} entries of a list, in order, as id -> multiplier (clamped to
	 * {@link #MIN}..{@link #MAX}). Parsed on every call: the config screen can change the list at any time.
	 */
	public static Map<String, Integer> overrides(Overrides list) {
		Map<String, Integer> out = new LinkedHashMap<>();
		if (store == null) return out;
		for (String entry : new ArrayList<>(store.getList(list))) {
			if (entry == null) continue;
			int split = entry.lastIndexOf('=');
			if (split <= 0) continue;
			String id = entry.substring(0, split).trim();
			try {
				int value = Integer.parseInt(entry.substring(split + 1).trim());
				if (!id.isEmpty() && value > 0) out.put(id, Math.min(MAX, value));
			} catch (NumberFormatException ignored) {
				// not "id=multiplier": ignored, and reported when the config is loaded
			}
		}
		return Collections.unmodifiableMap(out);
	}

	/** Sets the override for {@code id}; 0 removes it. */
	public static void setOverride(Overrides list, String id, int value) {
		String key = normalize(id);
		List<String> entries = new ArrayList<>();
		for (String entry : store.getList(list)) {
			int split = entry == null ? -1 : entry.lastIndexOf('=');
			if (split > 0 && normalize(entry.substring(0, split)).equals(key)) continue;
			if (entry != null) entries.add(entry);
		}
		if (value > 0) entries.add(id.trim() + "=" + value);
		store.setList(list, entries);
		store.save();
	}

	private static String normalize(String id) {
		String key = id.trim().toLowerCase(Locale.ROOT);
		return key.startsWith("minecraft:") ? key.substring("minecraft:".length()) : key;
	}

	public static void reload() {
		store.load();
		warnInvalidEntries();
	}

	public static boolean usingMidnightLib() {
		return !(store instanceof JsonConfigStore);
	}

	public static String summary() {
		StringBuilder s = new StringBuilder();
		for (Multiplier m : Multiplier.values()) {
			if (s.length() > 0) s.append(", ");
			int value = get(m);
			s.append(m.commandName).append(value == 0 ? " =mobs" : " x" + value);
		}
		return s.append(", ").append(overrides(Overrides.BLOCK).size()).append(" block and ")
			.append(overrides(Overrides.MOB).size()).append(" mob overrides").toString();
	}

	/** Logs list entries that are not "id=multiplier" (they are ignored). */
	static void warnInvalidEntries() {
		if (store == null) return;
		for (Overrides list : Overrides.values()) {
			int valid = overrides(list).size();
			int total = 0;
			for (String entry : store.getList(list)) if (entry != null) total++;
			if (valid < total) {
				Xcraft.LOGGER.warn("{}: {} entries are not \"id=multiplier\" (multiplier 1-{}) and are ignored",
					list.key, total - valid, MAX);
			}
		}
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
