package dev.polv.xcraft.forge.ml;

import dev.polv.xcraft.forge.Xcraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import org.apache.commons.lang3.tuple.Pair;

/** xCraft for Forge 1.13.2 - 1.16.5 (ModLauncher). */
@Mod(Xcraft.MOD_ID)
public class XcraftMod {

	public XcraftMod() {
		Xcraft.init(FMLPaths.CONFIGDIR.get());
		MinecraftForge.EVENT_BUS.register(new Events());
		// Server-side only: vanilla clients and clients without xCraft may join (mods.toml has no displayTest yet)
		ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST,
			() -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (remote, isServer) -> true));
	}
}
