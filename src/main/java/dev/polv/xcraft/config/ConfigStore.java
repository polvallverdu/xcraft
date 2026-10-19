package dev.polv.xcraft.config;

/**
 * Where multiplier values live: either xCraft's own JSON file, or MidnightLib when it is installed
 * (which then also provides the config screen). Both use the same {@code config/xcraft.json} format.
 */
public interface ConfigStore {
	int get(Multiplier multiplier);

	void set(Multiplier multiplier, int value);

	void load();

	void save();
}
