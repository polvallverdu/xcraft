package dev.polv.xcraft.platform.forge;

//? forge {

/*import dev.polv.xcraft.Xcraft;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.common.Mod;

@Mod(Xcraft.MOD_ID)
public class ForgeEntrypoint {

	public ForgeEntrypoint() {
		Xcraft.init(FMLPaths.CONFIGDIR.get());
		// EventBus 7 (Forge 1.21.6+) moved listeners onto each event's own BUS
		//? if >=1.21.6 {
		/^RegisterCommandsEvent.BUS.addListener(event -> Xcraft.registerCommands(event.getDispatcher()));
		^///?} else {
		net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener((RegisterCommandsEvent event) -> Xcraft.registerCommands(event.getDispatcher()));
		//?}
	}
}
*///?}
