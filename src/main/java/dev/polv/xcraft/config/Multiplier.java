package dev.polv.xcraft.config;

public enum Multiplier {
	ORES("ores", "oreDropMultiplier", 1),
	MOBS("mobs", "entityDropMultiplier", 1),
	/** 0 = same as {@link #MOBS}. */
	HOSTILE("hostile", "hostileMobDropMultiplier", 0),
	/** 0 = same as {@link #MOBS}. */
	PASSIVE("passive", "passiveMobDropMultiplier", 0),
	XP("xp", "xpMultiplier", 1),
	FURNACE("furnace", "furnaceSpeedMultiplier", 1);

	/** Name used by the {@code /xcraft} command. */
	public final String commandName;
	/** Key in {@code config/xcraft.json}; shared with the MidnightLib screen. */
	public final String key;
	/** Lowest and default value: 0 ("same as mobs") for hostile and passive, else 1. */
	public final int min;

	Multiplier(String commandName, String key, int min) {
		this.commandName = commandName;
		this.key = key;
		this.min = min;
	}
}
