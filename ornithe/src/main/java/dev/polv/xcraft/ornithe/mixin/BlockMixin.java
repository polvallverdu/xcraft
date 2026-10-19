package dev.polv.xcraft.ornithe.mixin;

import dev.polv.xcraft.ornithe.Config;
import dev.polv.xcraft.ornithe.Drops;
import dev.polv.xcraft.ornithe.OrnitheXcraft;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Ore drops. A player harvest runs either the Silk Touch branch (drops the block itself) or the normal drop call;
 * only the normal call is marked, and every stack spawned while it runs is multiplied.
 */
@Mixin(Block.class)
public abstract class BlockMixin {

	//? if >=1.13 {
	/*@Inject(method = "afterMinedByPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/state/BlockState;dropItems(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;I)V"))
	*///?} else {
	@Inject(method = "afterMinedByPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;dropItems(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/BlockState;I)V"))
	//?}
	private void xcraft$beforeNormalDrops(CallbackInfo ci) {
		Block self = (Block) (Object) this;
		if (Config.ores() > 1 && Drops.isOre(self)) Drops.HARVESTED_ORE.set(self);
	}

	//? if >=1.13 {
	/*@Inject(method = "afterMinedByPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/state/BlockState;dropItems(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;I)V", shift = At.Shift.AFTER))
	*///?} else {
	@Inject(method = "afterMinedByPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;dropItems(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/BlockState;I)V", shift = At.Shift.AFTER))
	//?}
	private void xcraft$afterNormalDrops(CallbackInfo ci) {
		Drops.HARVESTED_ORE.remove();
	}

	@Inject(method = "dropItem(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/item/ItemStack;)V", at = @At("HEAD"), cancellable = true)
	private static void xcraft$multiplyOreDrop(World world, BlockPos pos, ItemStack stack, CallbackInfo ci) {
		Block ore = Drops.HARVESTED_ORE.get();
		if (ore == null || Drops.isEmpty(stack) || Drops.isBlockItself(ore, stack)) return;
		Drops.HARVESTED_ORE.remove();
		try {
			for (ItemStack part : Drops.multiply(stack, Config.ores())) Block.dropItem(world, pos, part);
			OrnitheXcraft.debug("ore x" + Config.ores() + ": " + Drops.count(stack));
		} finally {
			Drops.HARVESTED_ORE.set(ore);
		}
		ci.cancel();
	}
}
