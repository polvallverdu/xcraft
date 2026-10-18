package dev.polv.xcraft;

import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

public final class MultiplierUtil {
	private MultiplierUtil() {
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
