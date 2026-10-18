package dev.polv.xcraft.platform.fabric;

//? fabric {

import dev.polv.xcraft.Xcraft;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class FabricEntrypoint implements ModInitializer {

	@Override
	public void onInitialize() {
		Xcraft.init(FabricLoader.getInstance().getConfigDir());
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> Xcraft.registerCommands(dispatcher));
	}
}
//?}
