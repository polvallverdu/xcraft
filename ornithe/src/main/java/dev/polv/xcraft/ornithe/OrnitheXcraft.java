package dev.polv.xcraft.ornithe;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** xCraft for the 1.9-1.13.1 releases Legacy Fabric lacks (Ornithe). Same features and config file as the main build. */
public class OrnitheXcraft implements ModInitializer {
	public static final String VERSION = "2.1.0";
	public static final Logger LOGGER = LogManager.getLogger("xCraft");
	/** -Dxcraft.debug=true logs every multiplication (used by the CI server tests: old versions can't read item NBT). */
	public static final boolean DEBUG = Boolean.getBoolean("xcraft.debug");

	public static void debug(String message) {
		if (DEBUG) LOGGER.info("[xcraft-debug] " + message);
	}

	@Override
	public void onInitialize() {
		Config.init(FabricLoader.getInstance().getConfigDir().resolve("xcraft.json"));
		// Plain concatenation: Minecraft 1.9-1.11 ship a Log4j beta without the (String, Object, Object) overload
		LOGGER.info("xCraft " + VERSION + " loaded (JSON config): " + Config.summary());
	}
}
