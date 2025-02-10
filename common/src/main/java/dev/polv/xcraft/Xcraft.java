package dev.polv.xcraft;

import dev.polv.xcraft.config.ModConfig;
import eu.midnightdust.lib.config.MidnightConfig;

public final class Xcraft {
    public static final String MOD_ID = "xcraft";

    public static void init() {
        MidnightConfig.init(MOD_ID, ModConfig.class);
    }
}
