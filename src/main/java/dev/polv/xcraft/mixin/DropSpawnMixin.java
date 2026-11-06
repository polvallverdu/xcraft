package dev.polv.xcraft.mixin;

import dev.polv.xcraft.MultiplierUtil;
import dev.polv.xcraft.config.XcraftConfig;
//? if >=1.21.2 {
/*import net.minecraft.server.level.ServerLevel;
*///?}
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Drops that skip the loot table, through the item drop every entity uses:
 * <ul>
 *   <li>a mob's head when a charged creeper kills it (marked by CreeperMixin), with the mob's multiplier;</li>
 *   <li>a falling dragon egg that breaks (e.g. on a torch), only with an explicit blockDropMultipliers entry
 *       for minecraft:dragon_egg: there is one egg per world, so it stays x1 unless asked for.</li>
 * </ul>
 * The vanilla drop goes ahead; the missing (multiplier - 1) items are dropped next to it.
 */
@Mixin(Entity.class)
public abstract class DropSpawnMixin {
	@Unique
	private static final ThreadLocal<Boolean> XCRAFT_SPAWNING_EXTRA = ThreadLocal.withInitial(() -> false);

	//? if >=1.21.2 {
	/*@Inject(method = "spawnAtLocation(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;F)Lnet/minecraft/world/entity/item/ItemEntity;", at = @At("HEAD"))
	private void xcraft$multiplyDrop(ServerLevel level, ItemStack stack, float yOffset, CallbackInfoReturnable<ItemEntity> cir) {
	*///?} else {
	@Inject(method = "spawnAtLocation(Lnet/minecraft/world/item/ItemStack;F)Lnet/minecraft/world/entity/item/ItemEntity;", at = @At("HEAD"))
	private void xcraft$multiplyDrop(ItemStack stack, float yOffset, CallbackInfoReturnable<ItemEntity> cir) {
	//?}
		if (XCRAFT_SPAWNING_EXTRA.get() || stack.isEmpty()) return;
		Entity self = (Entity) (Object) this;
		int multiplier = 1;
		if (self instanceof FallingBlockEntity) {
			if (stack.getItem() == Items.DRAGON_EGG) multiplier = XcraftConfig.blockMultiplier("minecraft:dragon_egg", false);
		} else if (MultiplierUtil.MOB_HEAD.get() && self instanceof LivingEntity && !(self instanceof Player)) {
			MultiplierUtil.MOB_HEAD.set(false);
			multiplier = MultiplierUtil.mobMultiplier((LivingEntity) self);
		}
		if (multiplier <= 1) return;
		XCRAFT_SPAWNING_EXTRA.set(true);
		try {
			MultiplierUtil.multiply(stack.copy(), multiplier - 1, extra -> {
				//? if >=1.21.2 {
				/*self.spawnAtLocation(level, extra, yOffset);
				*///?} else {
				self.spawnAtLocation(extra, yOffset);
				//?}
			});
		} finally {
			XCRAFT_SPAWNING_EXTRA.set(false);
		}
	}
}
