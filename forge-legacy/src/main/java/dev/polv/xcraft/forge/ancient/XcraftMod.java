package dev.polv.xcraft.forge.ancient;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.TickRegistry;
import dev.polv.xcraft.forge.Commands;
import dev.polv.xcraft.forge.Xcraft;
import net.minecraftforge.common.MinecraftForge;
//? if >=1.4.6 {
import cpw.mods.fml.relauncher.Side;
//?} else {
/*import cpw.mods.fml.common.Side;
*///?}

/**
 * xCraft for Forge 1.3.2 - 1.6.4 (FML relauncher). @NetworkMod with nothing required lets vanilla clients join.
 * Ore drops before Forge 1.6.2 need the coremod in this jar (core/), which hooks Block.harvestBlock.
 */
@Mod(modid = Xcraft.MOD_ID, name = "xCraft", version = Xcraft.VERSION)
@NetworkMod(clientSideRequired = false, serverSideRequired = false)
public class XcraftMod {

	//? if >=1.6 {
	@Mod.EventHandler
	//?} else {
	/*@Mod.PreInit
	*///?}
	public void preInit(FMLPreInitializationEvent event) {
		Xcraft.init(event.getModConfigurationDirectory().toPath());
		MinecraftForge.EVENT_BUS.register(new Events());
		//? if >=1.6.2 {
		// Forge 1.6.2 added BlockEvent.HarvestDropsEvent; this jar also runs on 1.6.1, which lacks it
		if (OreHarvest.HAS_HARVEST_EVENT) MinecraftForge.EVENT_BUS.register(new HarvestEvents());
		//?}
		TickRegistry.registerTickHandler(new FurnaceTicker(), Side.SERVER);
		if (Xcraft.DEBUG) Commands.selfTest = new SelfTest(this);
		if (!OreHarvest.HAS_HARVEST_EVENT && !OreHarvest.HOOKED) {
			Xcraft.info("Ore drops are not multiplied: on Forge before 1.6.2 put the xCraft jar in the coremods folder"
				+ " (1.3.2 - 1.5.2) so it can hook block harvesting");
		}
	}

	//? if >=1.6 {
	@Mod.EventHandler
	//?} else {
	/*@Mod.ServerStarting
	*///?}
	public void serverStarting(FMLServerStartingEvent event) {
		event.registerServerCommand(new XcraftCommand());
	}
}
