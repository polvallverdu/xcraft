package dev.polv.xcraft.ornithe.mixin;

import dev.polv.xcraft.ornithe.Config;
import dev.polv.xcraft.ornithe.Drops;
import dev.polv.xcraft.ornithe.OrnitheXcraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
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
		if (!Drops.MOB_LOOT.get() || Drops.isEmpty(stack)) return;
		Drops.MOB_LOOT.set(false);
		ItemEntity last = null;
		try {
			for (ItemStack part : Drops.multiply(stack, Config.mobs())) last = dropItem(part, yOffset);
			OrnitheXcraft.debug("mob loot x" + Config.mobs() + ": " + Drops.count(stack) + " -> " + Drops.count(stack) * Config.mobs());
		} finally {
			Drops.MOB_LOOT.set(true);
		}
		cir.setReturnValue(last);
	}
}
