package dev.polv.xcraft;

import com.mojang.brigadier.CommandDispatcher;
import dev.polv.xcraft.command.XcraftCommand;
import dev.polv.xcraft.config.XcraftConfig;
import net.minecraft.commands.CommandSourceStack;
//? if >=1.17 {
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//?} else {
/*import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
*///?}

import java.nio.file.Path;

public final class Xcraft {
	public static final String MOD_ID = /*$ mod_id*/ "xcraft";
	public static final String MOD_VERSION = /*$ mod_version*/ "2.1.0";
	//? if >=1.17 {
	public static final Logger LOGGER = LoggerFactory.getLogger("xCraft");
	//?} else {
	/*// Minecraft ships SLF4J only from 1.17; before that it is Log4j 2 (same message API)
	public static final Logger LOGGER = LogManager.getLogger("xCraft");
	*///?}

	private Xcraft() {
	}

	/**
	 * Called once by every loader entrypoint.
	 *
	 * @param configDir the loader's config directory
	 */
	public static void init(Path configDir) {
		XcraftConfig.init(configDir);
		LOGGER.info("xCraft {} loaded ({} config): {}", MOD_VERSION,
			XcraftConfig.usingMidnightLib() ? "MidnightLib" : "JSON", XcraftConfig.summary());
	}

	public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
		XcraftCommand.register(dispatcher);
	}
}
