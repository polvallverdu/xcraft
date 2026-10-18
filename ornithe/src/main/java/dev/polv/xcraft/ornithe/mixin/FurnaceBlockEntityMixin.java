package dev.polv.xcraft.ornithe.mixin;

import dev.polv.xcraft.ornithe.Config;
import dev.polv.xcraft.ornithe.OrnitheXcraft;
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
		OrnitheXcraft.debug("furnace x" + multiplier + ": cook " + cookTime + "/" + totalCookTime);
	}
}
