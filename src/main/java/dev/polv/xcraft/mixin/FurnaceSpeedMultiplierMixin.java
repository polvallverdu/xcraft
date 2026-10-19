package dev.polv.xcraft.mixin;

import dev.polv.xcraft.config.Multiplier;
import dev.polv.xcraft.config.XcraftConfig;
import net.minecraft.core.BlockPos;
//? if >=1.21.2 {
/*import net.minecraft.server.level.ServerLevel;
*///?} else {
import net.minecraft.world.level.Level;
//?}
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class FurnaceSpeedMultiplierMixin {

	//? if >=1.21.2 {
	/*@Inject(method = "serverTick", at = @At("TAIL"))
	private static void xcraft$speedUpSmelting(ServerLevel level, BlockPos pos, BlockState state, AbstractFurnaceBlockEntity furnace, CallbackInfo ci) {
		xcraft$boost((AbstractFurnaceBlockEntityAccessor) furnace);
	}
	*///?} else if >=1.17 {
	@Inject(method = "serverTick", at = @At("TAIL"))
	private static void xcraft$speedUpSmelting(Level level, BlockPos pos, BlockState state, AbstractFurnaceBlockEntity furnace, CallbackInfo ci) {
		xcraft$boost((AbstractFurnaceBlockEntityAccessor) furnace);
	}
	//?} else {
	/*// Before 1.17 block entities tick through an instance tick() on both sides
	@Inject(method = "tick", at = @At("TAIL"))
	private void xcraft$speedUpSmelting(CallbackInfo ci) {
		Level level = ((AbstractFurnaceBlockEntity) (Object) this).getLevel();
		if (level == null || level.isClientSide) return;
		xcraft$boost((AbstractFurnaceBlockEntityAccessor) (Object) this);
	}
	*///?}

	/**
	 * Vanilla adds 1 to the cooking progress each tick while smelting. We add the remaining
	 * {@code multiplier - 1}, stopping one short of the total so vanilla still finishes the item
	 * (output, recipe XP, hoppers) on the next tick.
	 */
	@Unique
	private static void xcraft$boost(AbstractFurnaceBlockEntityAccessor accessor) {
		int multiplier = XcraftConfig.get(Multiplier.FURNACE);
		if (multiplier <= 1) return;

		int progress = accessor.xcraft$getCookingProgress();
		int total = accessor.xcraft$getCookingTotalTime();
		if (progress <= 0 || progress >= total - 1 || accessor.xcraft$getLitTime() <= 0) return;

		accessor.xcraft$setCookingProgress(Math.min(progress + multiplier - 1, total - 1));
	}
}
