package dev.polv.xcraft.legacy.mixin;

import dev.polv.xcraft.legacy.Drops;
import dev.polv.xcraft.legacy.LegacyXcraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Every entity item drop goes through dropItem(ItemStack, float); multiply it while mob loot is marked. */
@Mixin(Entity.class)
public abstract class EntityMixin {

	@Shadow
	public abstract ItemEntity dropItem(ItemStack stack, float yOffset);

	@Inject(method = "dropItem(Lnet/minecraft/item/ItemStack;F)Lnet/minecraft/entity/ItemEntity;", at = @At("HEAD"), cancellable = true)
	private void xcraft$multiplyMobLoot(ItemStack stack, float yOffset, CallbackInfoReturnable<ItemEntity> cir) {
		if (!Drops.MOB_LOOT.get() || Drops.isEmpty(stack)) {
			if (xcraft$multiplyLateDrop(stack, yOffset, cir)) return;
			Drops.capture(stack);
			return;
		}
		int multiplier = Drops.mobMultiplier((Entity) (Object) this);
		Drops.MOB_LOOT.set(false);
		ItemEntity last = null;
		try {
			for (ItemStack part : Drops.multiply(stack, multiplier)) last = dropItem(part, yOffset);
			LegacyXcraft.debug("mob loot x" + multiplier + ": " + Drops.count(stack) + " -> " + Drops.count(stack) * multiplier);
		} finally {
			Drops.MOB_LOOT.set(true);
		}
		cir.setReturnValue(last);
	}

	/** Drops outside the loot window: a mob's head or music disc right after its death, a falling dragon egg. */
	@Unique
	private boolean xcraft$multiplyLateDrop(ItemStack stack, float yOffset, CallbackInfoReturnable<ItemEntity> cir) {
		if (Drops.LATE_EXTRA.get() || Drops.isEmpty(stack)) return false;
		int multiplier = Drops.lateDropMultiplier((Entity) (Object) this, stack);
		if (multiplier <= 1) return false;
		Drops.LATE_EXTRA.set(true);
		ItemEntity last = null;
		try {
			for (ItemStack part : Drops.multiply(stack, multiplier)) {
				Drops.capture(part);
				last = dropItem(part, yOffset);
			}
			LegacyXcraft.debug("late drop x" + multiplier + ": " + stack.getTranslationKey() + " " + Drops.count(stack) + " -> " + Drops.count(stack) * multiplier);
		} finally {
			Drops.LATE_EXTRA.set(false);
		}
		cir.setReturnValue(last);
		return true;
	}
}
