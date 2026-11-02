package dev.polv.xcraft.config;

import java.util.List;

/**
 * Where multiplier values live: either xCraft's own JSON file, or MidnightLib when it is installed
 * (which then also provides the config screen). Both use the same {@code config/xcraft.json} format.
 */
public interface ConfigStore {
	int get(Multiplier multiplier);

	void set(Multiplier multiplier, int value);

	/** The {@code "id=multiplier"} entries of a list, as stored. */
	List<String> getList(Overrides list);

	void setList(Overrides list, List<String> entries);

	void load();

	void save();
}
