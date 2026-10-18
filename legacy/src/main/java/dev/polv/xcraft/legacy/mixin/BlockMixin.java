package dev.polv.xcraft.legacy.mixin;

import dev.polv.xcraft.legacy.Config;
import dev.polv.xcraft.legacy.Drops;
import dev.polv.xcraft.legacy.LegacyXcraft;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//? if >=1.8 {
import net.minecraft.util.math.BlockPos;
//?} else {
/*import org.spongepowered.asm.mixin.Shadow;
*///?}

/**
 * Ore drops. A player harvest runs either the Silk Touch branch (drops the block itself) or the normal drop call;
 * only the normal call is marked, and every stack spawned while it runs is multiplied.
 */
@Mixin(Block.class)
public abstract class BlockMixin {

	//? if >=1.13 {
	/*@Inject(method = "method_8651", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;method_16867(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;I)V"))
	*///?} else if >=1.9 {
	@Inject(method = "method_8651", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;dropAsItem(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)V"))
	//?} else if >=1.8 {
	/*@Inject(method = "harvest", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;dropAsItem(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)V"))
	*///?} else {
	/*@Inject(method = "method_424", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;canStayPlaced(Lnet/minecraft/world/World;IIIII)V"))
	*///?}
	private void xcraft$beforeNormalDrops(CallbackInfo ci) {
		Block self = (Block) (Object) this;
		if (Config.ores() > 1 && Drops.isOre(self)) Drops.HARVESTED_ORE.set(self);
	}

	//? if >=1.13 {
	/*@Inject(method = "method_8651", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;method_16867(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;I)V", shift = At.Shift.AFTER))
	*///?} else if >=1.9 {
	@Inject(method = "method_8651", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;dropAsItem(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)V", shift = At.Shift.AFTER))
	//?} else if >=1.8 {
	/*@Inject(method = "harvest", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;dropAsItem(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)V", shift = At.Shift.AFTER))
	*///?} else {
	/*@Inject(method = "method_424", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;canStayPlaced(Lnet/minecraft/world/World;IIIII)V", shift = At.Shift.AFTER))
	*///?}
	private void xcraft$afterNormalDrops(CallbackInfo ci) {
		Drops.HARVESTED_ORE.remove();
	}

	//? if >=1.8 {
	@Inject(method = "onBlockBreak", at = @At("HEAD"), cancellable = true)
	private static void xcraft$multiplyOreDrop(World world, BlockPos pos, ItemStack stack, CallbackInfo ci) {
		Block ore = Drops.HARVESTED_ORE.get();
		if (ore == null || Drops.isEmpty(stack) || Drops.isBlockItself(ore, stack)) return;
		Drops.HARVESTED_ORE.remove();
		try {
			for (ItemStack part : Drops.multiply(stack, Config.ores())) Block.onBlockBreak(world, pos, part);
			LegacyXcraft.debug("ore x" + Config.ores() + ": " + Drops.count(stack));
		} finally {
			Drops.HARVESTED_ORE.set(ore);
		}
		ci.cancel();
	}
	//?} else {
	/*@Shadow
	protected abstract void method_422(World world, int x, int y, int z, ItemStack stack);

	@Inject(method = "method_422", at = @At("HEAD"), cancellable = true)
	private void xcraft$multiplyOreDrop(World world, int x, int y, int z, ItemStack stack, CallbackInfo ci) {
		Block ore = Drops.HARVESTED_ORE.get();
		if (ore == null || Drops.isEmpty(stack) || Drops.isBlockItself(ore, stack)) return;
		Drops.HARVESTED_ORE.remove();
		try {
			for (ItemStack part : Drops.multiply(stack, Config.ores())) method_422(world, x, y, z, part);
			LegacyXcraft.debug("ore x" + Config.ores() + ": " + Drops.count(stack));
		} finally {
			Drops.HARVESTED_ORE.set(ore);
		}
		ci.cancel();
	}
	*///?}
}
