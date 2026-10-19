package dev.polv.xcraft.config;

//? if midnightlib {
import dev.polv.xcraft.Xcraft;
import eu.midnightdust.lib.config.MidnightConfig;

/**
 * Used only when MidnightLib is installed; it is loaded reflectively so xCraft never touches
 * MidnightLib classes otherwise. MidnightLib reads and writes the same {@code config/xcraft.json}.
 */
public class MidnightConfigStore extends MidnightConfig implements ConfigStore {
	public static final String MULTIPLIERS = "multipliers";

	//? if >=1.19.4 {
	@Entry(category = MULTIPLIERS, min = XcraftConfig.MIN, max = XcraftConfig.MAX) public static int oreDropMultiplier = 1;
	@Entry(category = MULTIPLIERS, min = XcraftConfig.MIN, max = XcraftConfig.MAX) public static int entityDropMultiplier = 1;
	@Entry(category = MULTIPLIERS, min = XcraftConfig.MIN, max = XcraftConfig.MAX) public static int xpMultiplier = 1;
	@Entry(category = MULTIPLIERS, min = XcraftConfig.MIN, max = XcraftConfig.MAX) public static int furnaceSpeedMultiplier = 1;
	//?} else {
	/*// MidnightLib before 1.3 has no config categories
	@Entry(min = XcraftConfig.MIN, max = XcraftConfig.MAX) public static int oreDropMultiplier = 1;
	@Entry(min = XcraftConfig.MIN, max = XcraftConfig.MAX) public static int entityDropMultiplier = 1;
	@Entry(min = XcraftConfig.MIN, max = XcraftConfig.MAX) public static int xpMultiplier = 1;
	@Entry(min = XcraftConfig.MIN, max = XcraftConfig.MAX) public static int furnaceSpeedMultiplier = 1;
	*///?}

	@Override
	public int get(Multiplier multiplier) {
		return switch (multiplier) {
			case ORES -> oreDropMultiplier;
			case MOBS -> entityDropMultiplier;
			case XP -> xpMultiplier;
			case FURNACE -> furnaceSpeedMultiplier;
		};
	}

	@Override
	public void set(Multiplier multiplier, int value) {
		switch (multiplier) {
			case ORES -> oreDropMultiplier = value;
			case MOBS -> entityDropMultiplier = value;
			case XP -> xpMultiplier = value;
			case FURNACE -> furnaceSpeedMultiplier = value;
		}
	}

	@Override
	public void load() {
		MidnightConfig.init(Xcraft.MOD_ID, MidnightConfigStore.class);
	}

	@Override
	public void save() {
		MidnightConfig.write(Xcraft.MOD_ID);
	}
}
//?} else {
/*public final class MidnightConfigStore {
	// MidnightLib does not exist for this Minecraft version and loader.
}
*///?}
