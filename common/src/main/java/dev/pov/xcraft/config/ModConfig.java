package dev.pov.xcraft.config;

import eu.midnightdust.lib.config.MidnightConfig;

public class ModConfig extends MidnightConfig {

    public static final String MULTIPLIERS_CATEGORY = "multipliers";

    @Entry(category = MULTIPLIERS_CATEGORY, min=0) public static int blockDropMultiplier = 1;
    // TODO
    @Entry(category = MULTIPLIERS_CATEGORY, min=0) public static int entityDropMultiplier = 1;
    // TODO
    @Entry(category = MULTIPLIERS_CATEGORY, min=0) public static int xpMultiplier = 1;
    // TODO
    @Entry(category = MULTIPLIERS_CATEGORY, min=0) public static int furnaceSpeedMultiplier = 1;
    // TODO
    @Entry(category = MULTIPLIERS_CATEGORY, min=0) public static int villagerTradeMultiplier = 1;

}
