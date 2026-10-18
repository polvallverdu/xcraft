package dev.polv.xcraft.ornithe;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/** {@code config/xcraft.json}, same keys as the main build so the file is interchangeable. */
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

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
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
			try (Reader reader = Files.newBufferedReader(path)) {
				// Instance parse(): Minecraft 1.7.10 ships Gson 2.2.4, which has no static parseReader
				JsonObject json = new JsonParser().parse(reader).getAsJsonObject();
				for (int i = 0; i < MULTIPLIERS.length; i++) {
					JsonElement value = json.get(MULTIPLIERS[i][1]);
					if (value != null && value.isJsonPrimitive()) VALUES[i] = value.getAsInt();
				}
			} catch (Exception e) {
				OrnitheXcraft.LOGGER.error("Failed to read " + path + ", keeping current values", e);
			}
		}
		save();
	}

	private static void save() {
		JsonObject json = new JsonObject();
		for (int i = 0; i < MULTIPLIERS.length; i++) json.addProperty(MULTIPLIERS[i][1], VALUES[i]);
		try {
			Files.createDirectories(path.getParent());
			try (Writer writer = Files.newBufferedWriter(path)) {
				writer.write(GSON.toJson(json));
			}
		} catch (Exception e) {
			OrnitheXcraft.LOGGER.error("Failed to write " + path, e);
		}
	}

	public static String summary() {
		return "ores x" + ores() + ", mobs x" + mobs() + ", xp x" + xp() + ", furnace x" + furnace();
	}
}
