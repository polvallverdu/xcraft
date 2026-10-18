package dev.polv.xcraft.mixin;

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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class FurnaceSpeedMultiplierMixin {

	/**
	 * Vanilla adds 1 to the cooking progress each tick while smelting. We add the remaining
	 * {@code multiplier - 1}, stopping one short of the total so vanilla still finishes the item
	 * (output, recipe XP, hoppers) on the next tick.
	 */
	@Inject(method = "serverTick", at = @At("TAIL"))
	//? if >=1.21.2 {
	/*private static void xcraft$speedUpSmelting(ServerLevel level, BlockPos pos, BlockState state, AbstractFurnaceBlockEntity furnace, CallbackInfo ci) {
	*///?} else {
	private static void xcraft$speedUpSmelting(Level level, BlockPos pos, BlockState state, AbstractFurnaceBlockEntity furnace, CallbackInfo ci) {
	//?}
		int multiplier = XcraftConfig.furnaceSpeedMultiplier;
		if (multiplier <= 1) return;

		AbstractFurnaceBlockEntityAccessor accessor = (AbstractFurnaceBlockEntityAccessor) furnace;
		int progress = accessor.xcraft$getCookingProgress();
		int total = accessor.xcraft$getCookingTotalTime();
		if (progress <= 0 || progress >= total - 1 || accessor.xcraft$getLitTime() <= 0) return;

		accessor.xcraft$setCookingProgress(Math.min(progress + multiplier - 1, total - 1));
	}
}
