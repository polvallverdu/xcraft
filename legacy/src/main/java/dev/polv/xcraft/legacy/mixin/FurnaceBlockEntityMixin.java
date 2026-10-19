package dev.polv.xcraft.legacy.mixin;

import dev.polv.xcraft.legacy.Config;
import dev.polv.xcraft.legacy.LegacyXcraft;
import net.minecraft.block.entity.FurnaceBlockEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Vanilla adds 1 to the cook time each tick while smelting; add the remaining multiplier - 1, stopping one
 * short of the total so vanilla still finishes the item on its next tick.
 */
@Mixin(FurnaceBlockEntity.class)
public abstract class FurnaceBlockEntityMixin {

	//? if >=1.8 {
	@Shadow
	private int fuelTime;
	@Shadow
	private int cookTime;
	@Shadow
	private int totalCookTime;

	@Inject(method = "tick", at = @At("TAIL"))
	private void xcraft$speedUpSmelting(CallbackInfo ci) {
		World world = ((BlockEntityAccessor) this).xcraft$getWorld();
		if (world == null || world.isClient) return;
		int multiplier = Config.furnace();
		if (multiplier <= 1 || fuelTime <= 0 || cookTime <= 0 || cookTime >= totalCookTime - 1) return;
		cookTime = Math.min(cookTime + multiplier - 1, totalCookTime - 1);
		LegacyXcraft.debug("furnace x" + multiplier + ": cook " + cookTime + "/" + totalCookTime);
	}
	//?} else {
	/*// 1.7.10 has no total-cook-time field: an item is done when the cook time reaches 200
	@Shadow
	public int fuelTime;
	@Shadow
	public int field_547;

	@Inject(method = "method_545", at = @At("TAIL"))
	private void xcraft$speedUpSmelting(CallbackInfo ci) {
		World world = ((BlockEntityAccessor) this).xcraft$getWorld();
		if (world == null || world.isClient) return;
		int multiplier = Config.furnace();
		if (multiplier <= 1 || fuelTime <= 0 || field_547 <= 0 || field_547 >= 199) return;
		field_547 = Math.min(field_547 + multiplier - 1, 199);
		LegacyXcraft.debug("furnace x" + multiplier + ": cook " + field_547 + "/200");
	}
	*///?}
}
