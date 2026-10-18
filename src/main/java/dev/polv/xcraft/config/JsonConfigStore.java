package dev.polv.xcraft.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import dev.polv.xcraft.Xcraft;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;

/** Used when MidnightLib is not installed. */
public final class JsonConfigStore implements ConfigStore {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private final Path path;
	private final Map<Multiplier, Integer> values = new EnumMap<>(Multiplier.class);

	public JsonConfigStore(Path path) {
		this.path = path;
		for (Multiplier m : Multiplier.values()) values.put(m, 1);
	}

	@Override
	public int get(Multiplier multiplier) {
		return values.get(multiplier);
	}

	@Override
	public void set(Multiplier multiplier, int value) {
		values.put(multiplier, value);
	}

	@Override
	public void load() {
		if (Files.exists(path)) {
			try (Reader reader = Files.newBufferedReader(path)) {
				//? if >=1.18 {
				JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
				//?} else {
				/*// Gson before 2.8.6 (Minecraft <1.18) has no static parseReader
				JsonObject json = new JsonParser().parse(reader).getAsJsonObject();
				*///?}
				for (Multiplier m : Multiplier.values()) {
					JsonElement value = json.get(m.key);
					if (value != null && value.isJsonPrimitive()) values.put(m, value.getAsInt());
				}
			} catch (IOException | JsonParseException | IllegalStateException | NumberFormatException e) {
				Xcraft.LOGGER.error("Failed to read {}, keeping current values", path, e);
			}
		}
		save();
	}

	@Override
	public void save() {
		JsonObject json = new JsonObject();
		for (Multiplier m : Multiplier.values()) json.addProperty(m.key, values.get(m));
		try {
			Files.createDirectories(path.getParent());
			try (Writer writer = Files.newBufferedWriter(path)) {
				GSON.toJson(json, writer);
			}
		} catch (IOException e) {
			Xcraft.LOGGER.error("Failed to write {}", path, e);
		}
	}
}
