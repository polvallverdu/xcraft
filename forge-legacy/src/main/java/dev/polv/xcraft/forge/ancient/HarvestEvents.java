package dev.polv.xcraft.forge.ancient;

//? if >=1.6.2 {
import dev.polv.xcraft.forge.Config;
import dev.polv.xcraft.forge.Xcraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.EventPriority;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.world.BlockEvent;

import java.util.ArrayList;
import java.util.List;

/** Ore drops on Forge 1.6.2+, which fires HarvestDropsEvent (a separate class: Forge 1.6.1 lacks the event). */
public class HarvestEvents {

	@ForgeSubscribe(priority = EventPriority.LOW)
	public void onHarvestDrops(BlockEvent.HarvestDropsEvent event) {
		int multiplier = Config.ores();
		if (multiplier <= 1 || event.harvester == null || event.isSilkTouching || !OreHarvest.isOre(event.block)) return;
		List<ItemStack> out = new ArrayList<ItemStack>();
		for (ItemStack stack : event.drops) {
			if (!OreHarvest.multiplies(event.block, stack)) {
				out.add(stack);
				continue;
			}
			Xcraft.debug("ore x" + multiplier + ": " + stack.stackSize);
			for (int part : Xcraft.split(stack.stackSize, multiplier, stack.getMaxStackSize())) {
				ItemStack copy = stack.copy();
				copy.stackSize = part;
				out.add(copy);
			}
		}
		event.drops.clear();
		event.drops.addAll(out);
	}
}
//?}
