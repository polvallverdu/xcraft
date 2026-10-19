package dev.polv.xcraft.forge.ancient.core;

import dev.polv.xcraft.forge.ancient.OreHarvest;
//? if <1.4.5 {
/*import net.minecraft.src.*;
*///?} else {
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
//?}

/**
 * Never loaded or run. OreTransformer reads this class's bytecode to learn how Block.harvestBlock and the
 * OreHarvest hooks are named at runtime: the build remaps these calls to the production names of each version.
 */
final class HarvestTemplate {
	private HarvestTemplate() {
	}

	static void template(Block block, World world, EntityPlayer player) {
		block.harvestBlock(world, player, 0, 0, 0, 0);
		OreHarvest.begin(block, player, 0, 0, 0);
		OreHarvest.end();
	}
}
