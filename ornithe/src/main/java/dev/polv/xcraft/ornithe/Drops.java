package dev.polv.xcraft.ornithe;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
//? if >=1.13 {
/*import net.minecraft.entity.EntityType;
*///?} else {
import net.minecraft.entity.Entities;
//?}
import net.minecraft.entity.living.mob.monster.Monster;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
//? if >=1.13.1 {
/*import net.minecraft.util.registry.Registry;
*///?} else {
import net.minecraft.resource.Identifier;
//?}

import java.util.ArrayList;
import java.util.List;

/** Shared drop state for the mixins: which block is being harvested, and whether mob loot is being dropped. */
public final class Drops {
	/** Set while a player-broken block whose drops are multiplied runs its normal (non Silk Touch) drop logic. */
	public static final ThreadLocal<Block> HARVESTED_BLOCK = new ThreadLocal<>();
	/** True while a mob (not a player) drops its loot; false while it drops its equipment. */
	public static final ThreadLocal<Boolean> MOB_LOOT = ThreadLocal.withInitial(() -> false);
	/** Remembers MOB_LOOT while the equipment drop temporarily disables it. */
	public static final ThreadLocal<Boolean> MOB_LOOT_PAUSED = ThreadLocal.withInitial(() -> false);
	/**
	 * The mob whose death code just finished. Mobs drop a head (killed by a charged creeper) or a music disc
	 * (creeper killed by a skeleton) after it, in their own death code: those drops are loot too.
	 */
	public static final ThreadLocal<Object> DIED = new ThreadLocal<>();
	/** True while the extra items of a late drop are dropped. */
	public static final ThreadLocal<Boolean> LATE_EXTRA = ThreadLocal.withInitial(() -> false);

	private Drops() {
	}

	/** A harvested block's drop multiplier: its blockDropMultipliers entry, else the ores multiplier for ores. */
	public static int blockMultiplier(Block block) {
		//? if >=1.13.1 {
		/*return Config.blockMultiplier(isOre(block), String.valueOf(Registry.BLOCK.getKey(block)));
		*///?} else {
		return Config.blockMultiplier(isOre(block), String.valueOf(Block.REGISTRY.getKey(block)));
		//?}
	}

	/** A mob's drop multiplier: its mobDropMultipliers entry, else the hostile/passive multiplier, else mobs. */
	public static int mobMultiplier(Entity entity) {
		return Config.mobMultiplier(entityId(entity), entity instanceof Monster);
	}

	/** An id ("minecraft:zombie") from 1.11, a name ("Zombie", "FallingSand") before. */
	public static String entityId(Entity entity) {
		//? if >=1.13 {
		/*return String.valueOf(EntityType.getKey(entity.getType()));
		*///?} else {
		return String.valueOf(Entities.getKey(entity));
		//?}
	}

	/** The item's id, e.g. "minecraft:skull" (before 1.13) or "minecraft:zombie_head". */
	public static String itemId(ItemStack stack) {
		//? if >=1.13.1 {
		/*return String.valueOf(Registry.ITEM.getKey(stack.getItem()));
		*///?} else {
		return String.valueOf(Item.REGISTRY.getKey(stack.getItem()));
		//?}
	}

	/**
	 * The multiplier of a drop made outside the loot window, 1 if it is not one: a head or music disc dropped by
	 * a mob right after its death code, or a falling dragon egg that broke (only with an explicit
	 * blockDropMultipliers entry: one egg per world, so x1 unless asked for).
	 */
	public static int lateDropMultiplier(Entity entity, ItemStack stack) {
		String id = itemId(stack);
		if (DIED.get() == entity && (id.contains("skull") || id.endsWith("_head") || id.contains("record") || id.contains("music_disc"))) {
			return mobMultiplier(entity);
		}
		if (id.endsWith("dragon_egg") && entityId(entity).toLowerCase(java.util.Locale.ROOT).contains("falling")) {
			return Config.blockMultiplier(false, "minecraft:dragon_egg");
		}
		return 1;
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
