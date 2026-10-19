package dev.polv.xcraft.legacy;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
//? if >=1.7 {
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
//?} else {
/*import java.util.logging.Level;
import java.util.logging.Logger;
*///?}

/** xCraft for Minecraft 1.3.1 - 1.13.2 (Legacy Fabric). Same features and config file as the main build. */
public class LegacyXcraft implements ModInitializer {
	public static final String VERSION = "2.1.0";
	// Minecraft ships Log4j from 1.7; older versions log through java.util.logging
	//? if >=1.7 {
	private static final Logger LOGGER = LogManager.getLogger("xCraft");
	//?} else {
	/*private static final Logger LOGGER = Logger.getLogger("xCraft");
	*///?}
	/** -Dxcraft.debug=true logs every multiplication (used by the CI server tests: old versions can't read item NBT). */
	public static final boolean DEBUG = Boolean.getBoolean("xcraft.debug");

	public static void info(String message) {
		LOGGER.info(message);
	}

	public static void error(String message, Throwable error) {
		//? if >=1.7 {
		LOGGER.error(message, error);
		//?} else {
		/*LOGGER.log(Level.SEVERE, message, error);
		*///?}
	}

	public static void debug(String message) {
		if (DEBUG) info("[xcraft-debug] " + message);
	}

	@Override
	public void onInitialize() {
		Config.init(FabricLoader.getInstance().getConfigDir().resolve("xcraft.json"));
		// Plain concatenation: Minecraft 1.9-1.11 ship a Log4j beta without the (String, Object, Object) overload
		info("xCraft " + VERSION + " loaded (JSON config): " + Config.summary());
	}
}
