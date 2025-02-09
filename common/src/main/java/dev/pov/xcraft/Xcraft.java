package dev.pov.xcraft;

import dev.pov.xcraft.config.ModConfig;
import dev.pov.xcraft.listeners.ChunkListener;
import eu.midnightdust.lib.config.MidnightConfig;

public final class Xcraft {
    public static final String MOD_ID = "xcraft";

    public static void init() {
        MidnightConfig.init(MOD_ID, ModConfig.class);
        ChunkListener.initializeChunkEvents();
    }
}
