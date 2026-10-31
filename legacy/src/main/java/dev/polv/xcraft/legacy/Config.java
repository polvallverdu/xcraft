package dev.polv.xcraft.legacy;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@code config/xcraft.json}, same keys as the main build so the file is interchangeable. Read and written by hand:
 * Minecraft only ships Gson from 1.6, and the file is a flat object of numbers plus two lists of "id=multiplier".
 * <p>
 * A mob's drops use its own entry in mobDropMultipliers if it has one, else the hostile or passive multiplier if
 * that is set (not 0), else the mobs multiplier. A block's drops use its own entry in blockDropMultipliers (any
 * block, not only ores) if it has one, else the ores multiplier for ores.
 */
public final class Config {
	public static final int MIN = 1;
	public static final int MAX = 1000;
	/** Command name -> JSON key. */
	public static final String[][] MULTIPLIERS = {
		{"ores", "oreDropMultiplier"},
		{"mobs", "entityDropMultiplier"},
		{"hostile", "hostileMobDropMultiplier"},
		{"passive", "passiveMobDropMultiplier"},
		{"xp", "xpMultiplier"},
		{"furnace", "furnaceSpeedMultiplier"},
	};
	public static final int ORES = 0, MOBS = 1, HOSTILE = 2, PASSIVE = 3, XP = 4, FURNACE = 5;
	/** Command name -> JSON key of a list of per-id overrides ("id=multiplier"). */
	public static final String[][] OVERRIDES = {
		{"block", "blockDropMultipliers"},
		{"mob", "mobDropMultipliers"},
	};
	public static final int BLOCK = 0, MOB = 1;

	/** hostile and passive default to 0: "same as mobs". */
	private static final int[] VALUES = {1, 1, 0, 0, 1, 1};
	/** Per override list: id as written -> multiplier, in file order. */
	private static final List<Map<String, Integer>> ENTRIES = new ArrayList<Map<String, Integer>>();
	private static final Pattern STRING = Pattern.compile("\"((?:[^\"\\\\]|\\\\.)*)\"");
	private static Path path;

	static {
		for (int i = 0; i < OVERRIDES.length; i++) ENTRIES.add(new LinkedHashMap<String, Integer>());
	}

	private Config() {
	}

	public static int ores() {
		return get(ORES);
	}

	public static int mobs() {
		return get(MOBS);
	}

	public static int xp() {
		return get(XP);
	}

	public static int furnace() {
		return get(FURNACE);
	}

	/** Lowest value of a multiplier: hostile and passive accept 0 (same as mobs). */
	public static int min(int index) {
		return index == HOSTILE || index == PASSIVE ? 0 : MIN;
	}

	public static int get(int index) {
		return Math.max(min(index), Math.min(MAX, VALUES[index]));
	}

	public static int indexOf(String commandName) {
		for (int i = 0; i < MULTIPLIERS.length; i++) if (MULTIPLIERS[i][0].equals(commandName)) return i;
		return -1;
	}

	public static int overrideIndexOf(String commandName) {
		for (int i = 0; i < OVERRIDES.length; i++) if (OVERRIDES[i][0].equals(commandName)) return i;
		return -1;
	}

	public static void set(int index, int value) {
		VALUES[index] = value;
		save();
	}

	/** The drop multiplier of a mob, by its id (any of the game's names for it) and whether it is hostile. */
	public static int mobMultiplier(String id, boolean hostile) {
		Integer own = override(MOB, id);
		if (own != null) return own;
		int category = get(hostile ? HOSTILE : PASSIVE);
		return category > 0 ? category : mobs();
	}

	/** The drop multiplier of a block, by its ids (e.g. registry name, numeric id), and whether it is an ore. */
	public static int blockMultiplier(boolean ore, String... ids) {
		for (String id : ids) {
			Integer own = override(BLOCK, id);
			if (own != null) return own;
		}
		return ore ? ores() : 1;
	}

	/** The override for {@code id}, or null. Ids match ignoring case and the "minecraft:" namespace. */
	public static Integer override(int list, String id) {
		if (id == null) return null;
		String key = normalize(id);
		for (Map.Entry<String, Integer> entry : ENTRIES.get(list).entrySet()) {
			if (normalize(entry.getKey()).equals(key)) return Math.max(MIN, Math.min(MAX, entry.getValue()));
		}
		return null;
	}

	public static Map<String, Integer> overrides(int list) {
		return Collections.unmodifiableMap(ENTRIES.get(list));
	}

	/** Sets the override for {@code id}; 0 removes it. */
	public static void setOverride(int list, String id, int value) {
		Map<String, Integer> entries = ENTRIES.get(list);
		String key = normalize(id);
		for (String existing : new ArrayList<String>(entries.keySet())) {
			if (normalize(existing).equals(key)) entries.remove(existing);
		}
		if (value > 0) entries.put(id.trim(), value);
		save();
	}

	private static String normalize(String id) {
		String key = id.trim().toLowerCase(Locale.ROOT);
		return key.startsWith("minecraft:") ? key.substring("minecraft:".length()) : key;
	}

	public static void init(Path file) {
		path = file;
		reload();
	}

	public static void reload() {
		if (Files.exists(path)) {
			try {
				String json = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
				for (int i = 0; i < MULTIPLIERS.length; i++) {
					Matcher value = Pattern.compile("\"" + MULTIPLIERS[i][1] + "\"\\s*:\\s*(-?\\d+)").matcher(json);
					if (value.find()) VALUES[i] = Integer.parseInt(value.group(1));
				}
				for (int i = 0; i < OVERRIDES.length; i++) {
					Matcher list = Pattern.compile("\"" + OVERRIDES[i][1] + "\"\\s*:\\s*\\[([^\\]]*)\\]").matcher(json);
					if (!list.find()) continue;
					Map<String, Integer> entries = ENTRIES.get(i);
					entries.clear();
					Matcher entry = STRING.matcher(list.group(1));
					while (entry.find()) parseEntry(entries, unescape(entry.group(1)));
				}
			} catch (Exception e) {
				LegacyXcraft.error("Failed to read " + path + ", keeping current values", e);
			}
		}
		save();
	}

	/** Decodes a JSON string's escapes (Gson, e.g. MidnightLib's, writes '=' as \\u003d). */
	private static String unescape(String s) {
		StringBuilder out = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c != '\\' || i + 1 >= s.length()) {
				out.append(c);
				continue;
			}
			char e = s.charAt(++i);
			if (e == 'u' && i + 4 < s.length()) {
				out.append((char) Integer.parseInt(s.substring(i + 1, i + 5), 16));
				i += 4;
			} else {
				out.append(e == 'n' ? '\n' : e == 't' ? '\t' : e == 'r' ? '\r' : e == 'b' ? '\b' : e == 'f' ? '\f' : e);
			}
		}
		return out.toString();
	}

	/** Adds an "id=multiplier" entry; malformed entries are reported and dropped. */
	private static void parseEntry(Map<String, Integer> entries, String entry) {
		int split = entry.lastIndexOf('=');
		try {
			String id = entry.substring(0, split).trim();
			int value = Integer.parseInt(entry.substring(split + 1).trim());
			if (id.length() > 0 && value > 0) {
				entries.put(id, value);
				return;
			}
		} catch (RuntimeException ignored) {
			// reported below
		}
		LegacyXcraft.info("Ignoring \"" + entry + "\" in " + path + ": expected \"id=multiplier\"");
	}

	private static void save() {
		// Same layout as Gson's pretty printing in the main build
		StringBuilder json = new StringBuilder("{\n");
		for (int i = 0; i < MULTIPLIERS.length; i++) {
			json.append("  \"").append(MULTIPLIERS[i][1]).append("\": ").append(VALUES[i]).append(",\n");
		}
		for (int i = 0; i < OVERRIDES.length; i++) {
			json.append("  \"").append(OVERRIDES[i][1]).append("\": [");
			Map<String, Integer> entries = ENTRIES.get(i);
			int n = 0;
			for (Map.Entry<String, Integer> entry : entries.entrySet()) {
				json.append(n++ == 0 ? "\n" : ",\n").append("    \"")
					.append((entry.getKey() + "=" + entry.getValue()).replace("\\", "\\\\").replace("\"", "\\\"")).append('"');
			}
			json.append(n > 0 ? "\n  ]" : "]").append(i < OVERRIDES.length - 1 ? ",\n" : "\n");
		}
		json.append("}");
		try {
			Files.createDirectories(path.getParent());
			Files.write(path, json.toString().getBytes(StandardCharsets.UTF_8));
		} catch (Exception e) {
			LegacyXcraft.error("Failed to write " + path, e);
		}
	}

	public static String summary() {
		StringBuilder s = new StringBuilder();
		for (int i = 0; i < MULTIPLIERS.length; i++) {
			if (i > 0) s.append(", ");
			s.append(MULTIPLIERS[i][0]).append(' ').append(get(i) == 0 ? "=mobs" : "x" + get(i));
		}
		return s.append(", ").append(ENTRIES.get(BLOCK).size()).append(" block and ")
			.append(ENTRIES.get(MOB).size()).append(" mob overrides").toString();
	}
}
