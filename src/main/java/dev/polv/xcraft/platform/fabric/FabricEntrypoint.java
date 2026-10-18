package dev.polv.xcraft.platform.fabric;

//? fabric {

import dev.polv.xcraft.Xcraft;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.api.ModInitializer;
//? if >=1.19 {
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
//?} else {
/*import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
*///?}

public class FabricEntrypoint implements ModInitializer {

	@Override
	public void onInitialize() {
		Xcraft.init(FabricLoader.getInstance().getConfigDir());
		//? if >=1.19 {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> Xcraft.registerCommands(dispatcher));
		//?} else {
		/*CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> Xcraft.registerCommands(dispatcher));
		*///?}
	}
}
//?}
