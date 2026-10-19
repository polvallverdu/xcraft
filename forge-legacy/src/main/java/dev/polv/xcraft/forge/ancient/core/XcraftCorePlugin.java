package dev.polv.xcraft.forge.ancient.core;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

import java.util.Map;

/**
 * Coremod entry (FMLCorePlugin in the jar manifest) for ore drops on Forge before 1.6.2, which has no
 * HarvestDropsEvent. FML loads coremods from the coremods folder up to 1.5.2 and from mods on 1.6.
 */
@IFMLLoadingPlugin.TransformerExclusions({"dev.polv.xcraft.forge.ancient.core"})
public class XcraftCorePlugin implements IFMLLoadingPlugin {

	@Override
	public String[] getLibraryRequestClass() {
		return null;
	}

	@Override
	public String[] getASMTransformerClass() {
		boolean hasEvent = XcraftCorePlugin.class.getClassLoader()
			.getResource("net/minecraftforge/event/world/BlockEvent$HarvestDropsEvent.class") != null;
		return hasEvent ? new String[0] : new String[] {OreTransformer.class.getName()};
	}

	@Override
	public String getModContainerClass() {
		return null;
	}

	@Override
	public String getSetupClass() {
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data) {
	}
}
