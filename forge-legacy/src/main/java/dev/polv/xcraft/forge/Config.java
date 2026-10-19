package dev.polv.xcraft.forge;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@code config/xcraft.json}, same keys as the main build so the file is interchangeable. Read and written by hand:
 * Minecraft only ships Gson from 1.6, and the file is a flat object of four numbers.
 */
public final class Config {
	public static final int MIN = 1;
	public static final int MAX = 1000;
	/** Command name -> JSON key. */
	public static final String[][] MULTIPLIERS = {
		{"ores", "oreDropMultiplier"},
		{"mobs", "entityDropMultiplier"},
		{"xp", "xpMultiplier"},
		{"furnace", "furnaceSpeedMultiplier"},
	};

	private static final int[] VALUES = {1, 1, 1, 1};
	private static Path path;

	private Config() {
	}

	public static int ores() {
		return get(0);
	}

	public static int mobs() {
		return get(1);
	}

	public static int xp() {
		return get(2);
	}

	public static int furnace() {
		return get(3);
	}

	public static int get(int index) {
		return Math.max(MIN, Math.min(MAX, VALUES[index]));
	}

	public static int indexOf(String commandName) {
		for (int i = 0; i < MULTIPLIERS.length; i++) if (MULTIPLIERS[i][0].equals(commandName)) return i;
		return -1;
	}

	public static void set(int index, int value) {
		VALUES[index] = value;
		save();
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
			} catch (Exception e) {
				Xcraft.error("Failed to read " + path + ", keeping current values", e);
			}
		}
		save();
	}

	private static void save() {
		// Same layout as Gson's pretty printing in the main build
		StringBuilder json = new StringBuilder("{\n");
		for (int i = 0; i < MULTIPLIERS.length; i++) {
			json.append("  \"").append(MULTIPLIERS[i][1]).append("\": ").append(VALUES[i]);
			json.append(i < MULTIPLIERS.length - 1 ? ",\n" : "\n");
		}
		json.append("}");
		try {
			Files.createDirectories(path.getParent());
			Files.write(path, json.toString().getBytes(StandardCharsets.UTF_8));
		} catch (Exception e) {
			Xcraft.error("Failed to write " + path, e);
		}
	}

	public static String summary() {
		return "ores x" + ores() + ", mobs x" + mobs() + ", xp x" + xp() + ", furnace x" + furnace();
	}
}
