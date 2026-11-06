package dev.polv.xcraft;

import dev.polv.xcraft.config.XcraftConfig;
//? if >=1.19.3 {
import net.minecraft.core.registries.BuiltInRegistries;
//?} else {
/*import net.minecraft.core.Registry;
*///?}
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

public final class MultiplierUtil {
	/**
	 * Set by a charged creeper when its victim is about to drop its head (outside the victim's loot table); the
	 * victim's next item drop is that head. Cleared once used and at the end of every death drop.
	 */
	public static final ThreadLocal<Boolean> MOB_HEAD = ThreadLocal.withInitial(() -> false);

	private MultiplierUtil() {
	}

	/** A mob's drop multiplier: its mobDropMultipliers entry, else hostile (Enemy) or passive, else mobs. */
	public static int mobMultiplier(LivingEntity entity) {
		//? if >=1.19.3 {
		String id = String.valueOf(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()));
		//?} else {
		/*String id = String.valueOf(Registry.ENTITY_TYPE.getKey(entity.getType()));
		*///?}
		return XcraftConfig.mobMultiplier(id, entity instanceof Enemy);
	}

	/**
	 * Emits {@code stack} multiplied by {@code multiplier}, split into stacks that respect the max stack size.
	 */
	public static void multiply(ItemStack stack, int multiplier, Consumer<ItemStack> output) {
		if (multiplier <= 1 || stack.isEmpty()) {
			output.accept(stack);
			return;
		}
		long remaining = (long) stack.getCount() * multiplier;
		int max = Math.max(1, stack.getMaxStackSize());
		while (remaining > 0) {
			int count = (int) Math.min(remaining, max);
			ItemStack copy = stack.copy();
			copy.setCount(count);
			output.accept(copy);
			remaining -= count;
		}
	}
}
