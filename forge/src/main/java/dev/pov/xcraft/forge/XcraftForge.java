package dev.pov.xcraft.forge;

import dev.pov.xcraft.Xcraft;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Xcraft.MOD_ID)
public final class XcraftForge {
    public XcraftForge() {
        // Submit our event bus to let Architectury API register our content on the right time.
        EventBuses.registerModEventBus(Xcraft.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());

        // Run our common setup.
        Xcraft.init();
    }
}
