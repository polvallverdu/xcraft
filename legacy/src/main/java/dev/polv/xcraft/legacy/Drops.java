package dev.polv.xcraft.legacy;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
//? if >=1.13 {
/*import net.minecraft.util.registry.Registry;
*///?} else if >=1.8 {
import net.minecraft.util.Identifier;
//?}

import java.util.ArrayList;
import java.util.List;

/** Shared drop state for the mixins: which ore is being harvested, and whether mob loot is being dropped. */
public final class Drops {
	/** Set while a player-broken ore runs its normal (non Silk Touch) drop logic. */
	public static final ThreadLocal<Block> HARVESTED_ORE = new ThreadLocal<>();
	/** True while a mob (not a player) drops its loot; false while it drops its equipment. */
	public static final ThreadLocal<Boolean> MOB_LOOT = ThreadLocal.withInitial(() -> false);
	/** Remembers MOB_LOOT while the equipment drop temporarily disables it. */
	public static final ThreadLocal<Boolean> MOB_LOOT_PAUSED = ThreadLocal.withInitial(() -> false);
	/** While /xcraft test runs: every final (already multiplied) ore and mob drop, as "translationKey:count". */
	public static final ThreadLocal<List<String>> TEST_CAPTURE = new ThreadLocal<>();

	private Drops() {
	}

	public static boolean isOre(Block block) {
		//? if >=1.13 {
		/*String path = Registry.BLOCK.getId(block).getPath();
		*///?} else if >=1.8 {
		String path = ((Identifier) Block.REGISTRY.getIdentifier(block)).getPath();
		//?} else if >=1.7 {
		/*String path = String.valueOf(Block.REGISTRY.getId(block));
		*///?} else {
		/*// No block registry before 1.7: go by the translation key (tile.oreCoal, tile.oreRedstone, tile.netherquartz)
		String key = block.getTranslationKey();
		if (key == null) return false;
		String path = key.startsWith("tile.ore") || key.equals("tile.netherquartz") ? "x_ore" : key;
		*///?}
		return path.endsWith("_ore");
	}

	/** True if the stack is the ore block itself (e.g. modded Silk Touch), which is never multiplied. */
	public static boolean isBlockItself(Block block, ItemStack stack) {
		//? if >=1.7 {
		return stack.getItem() == Item.fromBlock(block);
		//?} else {
		/*return stack.id == block.id;
		*///?}
	}

	/** Records a final drop for /xcraft test (no-op otherwise). */
	public static void capture(ItemStack stack) {
		List<String> captured = TEST_CAPTURE.get();
		if (captured != null && !isEmpty(stack)) captured.add(stack.getTranslationKey() + ":" + count(stack));
	}

	public static int count(ItemStack stack) {
		//? if >=1.11 {
		return stack.getCount();
		//?} else {
		/*return stack.count;
		*///?}
	}

	public static boolean isEmpty(ItemStack stack) {
		//? if >=1.11 {
		return stack == null || stack.isEmpty();
		//?} else {
		/*return stack == null || stack.count <= 0;
		*///?}
	}

	private static void setCount(ItemStack stack, int count) {
		//? if >=1.11 {
		stack.setCount(count);
		//?} else {
		/*stack.count = count;
		*///?}
	}

	/** The stack multiplied, split into stacks no larger than the item's max stack size. */
	public static List<ItemStack> multiply(ItemStack stack, int multiplier) {
		List<ItemStack> out = new ArrayList<>();
		long remaining = (long) count(stack) * multiplier;
		int max = Math.max(1, stack.getMaxCount());
		while (remaining > 0) {
			int n = (int) Math.min(remaining, max);
			ItemStack copy = stack.copy();
			setCount(copy, n);
			out.add(copy);
			remaining -= n;
		}
		return out;
	}
}
