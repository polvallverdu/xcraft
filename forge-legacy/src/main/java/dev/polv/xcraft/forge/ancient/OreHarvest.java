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
	 * The ore a player is harvesting right now, if ores are multiplied and the item at (itemX, itemY, itemZ) was
	 * dropped by it (the game spawns drops within the block's space); else null.
	 */
	static Block harvestingOre(double itemX, double itemY, double itemZ) {
		if (block == null || player == null || Config.ores() <= 1 || !isOre(block)) return null;
		boolean near = Math.abs(itemX - (x + 0.5)) < 1 && Math.abs(itemY - (y + 0.5)) < 1 && Math.abs(itemZ - (z + 0.5)) < 1;
		return near ? block : null;
	}

	/** Whether {@code stack}, dropped by {@code ore}, is multiplied: not the ore block itself (Silk Touch, iron ore). */
	static boolean multiplies(Block ore, ItemStack stack) {
		return stack != null && stack.stackSize > 0 && stack.itemID != ore.blockID;
	}

	static boolean isOre(Block block) {
		//? if >=1.5 {
		String name = block.getUnlocalizedName();
		//?} else {
		/*String name = block.getBlockName();
		*///?}
		// Vanilla ores, ores of mods that extend them, and blocks named like ores (tile.oreCopper, tile.netherquartz)
		return block instanceof BlockOre || block instanceof BlockRedstoneOre
			|| (name != null && (name.startsWith("tile.ore") || name.endsWith("Ore") || name.equals("tile.netherquartz")));
	}
}
