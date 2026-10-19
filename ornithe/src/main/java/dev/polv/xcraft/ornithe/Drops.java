package dev.polv.xcraft.ornithe;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
//? if >=1.13.1 {
/*import net.minecraft.util.registry.Registry;
*///?} else {
import net.minecraft.resource.Identifier;
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

	private Drops() {
	}

	public static boolean isOre(Block block) {
		//? if >=1.13.1 {
		/*String path = Registry.BLOCK.getKey(block).getPath();
		*///?} else {
		String path = ((Identifier) Block.REGISTRY.getKey(block)).getPath();
		//?}
		return path.endsWith("_ore");
	}

	/** True if the stack is the ore block itself (e.g. modded Silk Touch), which is never multiplied. */
	public static boolean isBlockItself(Block block, ItemStack stack) {
		return stack.getItem() == Item.byBlock(block);
	}

	public static int count(ItemStack stack) {
		//? if >=1.11 {
		return stack.getSize();
		//?} else {
		/*return stack.size;
		*///?}
	}

	public static boolean isEmpty(ItemStack stack) {
		//? if >=1.11 {
		return stack == null || stack.isEmpty();
		//?} else {
		/*return stack == null || stack.size <= 0;
		*///?}
	}

	private static void setCount(ItemStack stack, int count) {
		//? if >=1.11 {
		stack.setSize(count);
		//?} else {
		/*stack.size = count;
		*///?}
	}

	/** The stack multiplied, split into stacks no larger than the item's max stack size. */
	public static List<ItemStack> multiply(ItemStack stack, int multiplier) {
		List<ItemStack> out = new ArrayList<>();
		long remaining = (long) count(stack) * multiplier;
		int max = Math.max(1, stack.getMaxSize());
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
