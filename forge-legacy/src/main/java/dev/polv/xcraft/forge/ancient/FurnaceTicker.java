package dev.polv.xcraft.forge.ancient;

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import dev.polv.xcraft.forge.Config;
import dev.polv.xcraft.forge.ServerThread;
import dev.polv.xcraft.forge.Xcraft;
//? if <1.4.5 {
/*import net.minecraft.src.*;
*///?} else {
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.world.World;
//?}

import java.util.EnumSet;
import java.util.List;

/** Furnaces: after each server world tick, push every smelting furnace forward (an item takes 200 ticks). */
public class FurnaceTicker implements ITickHandler {

	@Override
	public void tickStart(EnumSet<TickType> type, Object... data) {
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... data) {
		if (Xcraft.DEBUG) ServerThread.tick();
		if (Config.furnace() <= 1 || data.length == 0 || !(data[0] instanceof World)) return;
		World world = (World) data[0];
		if (world.isRemote) return;
		List<?> tiles = world.loadedTileEntityList;
		for (int i = 0; i < tiles.size(); i++) {
			Object tile = tiles.get(i);
			if (!(tile instanceof TileEntityFurnace) || ((TileEntityFurnace) tile).isInvalid()) continue;
			boost((TileEntityFurnace) tile);
		}
	}

	static void boost(TileEntityFurnace furnace) {
		furnace.furnaceCookTime = Xcraft.boostCookTime(furnace.furnaceBurnTime, furnace.furnaceCookTime, 200);
	}

	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.WORLD);
	}

	@Override
	public String getLabel() {
		return "xCraft furnaces";
	}
}
