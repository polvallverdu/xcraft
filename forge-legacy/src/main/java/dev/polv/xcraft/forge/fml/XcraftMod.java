package dev.polv.xcraft.forge.fml;

import dev.polv.xcraft.forge.Xcraft;
//? if >=1.8 {
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
//?} else {
/*import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
*///?}
import net.minecraftforge.common.MinecraftForge;

/** xCraft for Forge 1.7.2 - 1.12.2 (FML). acceptableRemoteVersions = "*" lets vanilla clients join. */
@Mod(modid = Xcraft.MOD_ID, name = "xCraft", version = Xcraft.VERSION, acceptableRemoteVersions = "*")
public class XcraftMod {

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		Xcraft.init(event.getModConfigurationDirectory().toPath());
		Events events = new Events();
		MinecraftForge.EVENT_BUS.register(events);
		// TickEvent is on FML's own bus up to 1.8 (the same bus from 1.8.8; registering twice is a no-op)
		FMLCommonHandler.instance().bus().register(events);
	}

	@Mod.EventHandler
	public void serverStarting(FMLServerStartingEvent event) {
		event.registerServerCommand(new XcraftCommand());
	}
}
