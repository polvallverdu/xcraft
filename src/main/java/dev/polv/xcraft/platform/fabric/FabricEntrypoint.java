package dev.polv.xcraft.platform.fabric;

//? fabric {

import dev.polv.xcraft.Xcraft;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.api.ModInitializer;

public class FabricEntrypoint implements ModInitializer {

	@Override
	public void onInitialize() {
		Xcraft.init(FabricLoader.getInstance().getConfigDir());
		// /xcraft is registered by CommandsMixin (no Fabric API needed)
	}
}
//?}
