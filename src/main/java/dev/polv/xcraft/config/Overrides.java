package dev.polv.xcraft.config;

/** Lists of per-id multipliers, written {@code "id=multiplier"} (e.g. {@code "minecraft:zombie=5"}). */
public enum Overrides {
	/** Any block (not only ores), by its id; wins over the ores multiplier. */
	BLOCK("block", "blockDropMultipliers"),
	/** A mob, by its id; wins over the hostile, passive and mobs multipliers. */
	MOB("mob", "mobDropMultipliers");

	/** Name used by the {@code /xcraft} command. */
	public final String commandName;
	/** Key in {@code config/xcraft.json}; shared with the MidnightLib screen. */
	public final String key;

	Overrides(String commandName, String key) {
		this.commandName = commandName;
		this.key = key;
	}
}
