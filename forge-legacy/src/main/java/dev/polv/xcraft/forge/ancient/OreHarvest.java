package dev.polv.xcraft.forge.ancient;

import dev.polv.xcraft.forge.Config;
//? if <1.4.5 {
/*import net.minecraft.src.*;
*///?} else {
import net.minecraft.block.Block;
import net.minecraft.block.BlockOre;
import net.minecraft.block.BlockRedstoneOre;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
//?}

/**
 * Ore drops before Forge 1.6.2, which has no HarvestDropsEvent. The coremod (core/OreTransformer) makes
 * Block.harvestBlock - the call the game makes when a player breaks a block - call {@link #begin} and
 * {@link #end} around itself; the item entities the block spawns in between are multiplied (see Events).
 */
public final class OreHarvest {
	/** Forge 1.6.2+ fires HarvestDropsEvent, which HarvestEvents uses instead. */
	public static final boolean HAS_HARVEST_EVENT = OreHarvest.class.getClassLoader()
		.getResource("net/minecraftforge/event/world/BlockEvent$HarvestDropsEvent.class") != null;
	/** Set by the coremod once it patched Block.harvestBlock. */
	public static final boolean HOOKED = Boolean.getBoolean("xcraft.oreHook");

	private static Block block;
	private static EntityPlayer player;
	private static int x, y, z;

	private OreHarvest() {
	}

	public static void begin(Block harvested, EntityPlayer harvester, int blockX, int blockY, int blockZ) {
		block = harvested;
		player = harvester;
		x = blockX;
		y = blockY;
		z = blockZ;
	}

	public static void end() {
		block = null;
		player = null;
	}

	/**
	 * The block a player is harvesting right now, if its drops are multiplied and the item at (itemX, itemY, itemZ)
	 * was dropped by it (the game spawns drops within the block's space); else null.
	 */
	static Block harvesting(double itemX, double itemY, double itemZ) {
		if (block == null || player == null || multiplier(block) <= 1) return null;
		boolean near = Math.abs(itemX - (x + 0.5)) < 1 && Math.abs(itemY - (y + 0.5)) < 1 && Math.abs(itemZ - (z + 0.5)) < 1;
		return near ? block : null;
	}

	/**
	 * The drop multiplier of a harvested block: its own entry in blockDropMultipliers (by numeric id, e.g. "16", or
	 * name, e.g. "tile.oreCoal"), else the ores multiplier for ores.
	 */
	static int multiplier(Block block) {
		return Config.blockMultiplier(isOre(block), String.valueOf(block.blockID), name(block));
	}

	/** Whether {@code stack}, dropped by {@code block}, is multiplied: not an ore block itself (Silk Touch, iron ore). */
	static boolean multiplies(Block block, ItemStack stack) {
		return stack != null && stack.stackSize > 0 && (stack.itemID != block.blockID || !isOre(block));
	}

	private static String name(Block block) {
		//? if >=1.5 {
		return block.getUnlocalizedName();
		//?} else {
		/*return block.getBlockName();
		*///?}
	}

	static boolean isOre(Block block) {
		String name = name(block);
		// Vanilla ores, ores of mods that extend them, and blocks named like ores (tile.oreCopper, tile.netherquartz)
		return block instanceof BlockOre || block instanceof BlockRedstoneOre
			|| (name != null && (name.startsWith("tile.ore") || name.endsWith("Ore") || name.equals("tile.netherquartz")));
	}
}
