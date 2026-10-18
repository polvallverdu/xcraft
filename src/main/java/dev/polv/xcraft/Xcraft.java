package dev.polv.xcraft;

import com.mojang.brigadier.CommandDispatcher;
import dev.polv.xcraft.command.XcraftCommand;
import dev.polv.xcraft.config.XcraftConfig;
import net.minecraft.commands.CommandSourceStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Xcraft {
	public static final String MOD_ID = /*$ mod_id*/ "xcraft";
	public static final String MOD_VERSION = /*$ mod_version*/ "2.0.0";
	public static final Logger LOGGER = LoggerFactory.getLogger("xCraft");

	private Xcraft() {
	}

	/**
	 * Called once by every loader entrypoint.
	 */
	public static void init() {
		XcraftConfig.load();
		LOGGER.info("xCraft {} loaded: {}", MOD_VERSION, XcraftConfig.summary());
	}

	public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
		XcraftCommand.register(dispatcher);
	}
}
