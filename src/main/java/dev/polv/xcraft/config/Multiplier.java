package dev.polv.xcraft.config;

public enum Multiplier {
	ORES("ores", "oreDropMultiplier"),
	MOBS("mobs", "entityDropMultiplier"),
	XP("xp", "xpMultiplier"),
	FURNACE("furnace", "furnaceSpeedMultiplier");

	/** Name used by the {@code /xcraft} command. */
	public final String commandName;
	/** Key in {@code config/xcraft.json}; shared with the MidnightLib screen. */
	public final String key;

	Multiplier(String commandName, String key) {
		this.commandName = commandName;
		this.key = key;
	}
}
