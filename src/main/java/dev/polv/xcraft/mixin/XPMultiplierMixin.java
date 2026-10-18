package dev.polv.xcraft.mixin;

import dev.polv.xcraft.config.XcraftConfig;
import net.minecraft.world.entity.ExperienceOrb;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ExperienceOrb.class)
public abstract class XPMultiplierMixin {

	/**
	 * The orb value first goes to Mending; whatever is left is given as XP, so multiplying
	 * it here boosts both without applying the multiplier twice.
	 */
	@ModifyArg(
		method = "playerTouch",
		at = @At(
			value = "INVOKE",
			//? if >=1.21 {
			target = "Lnet/minecraft/world/entity/ExperienceOrb;repairPlayerItems(Lnet/minecraft/server/level/ServerPlayer;I)I"
			//?} else {
			/*target = "Lnet/minecraft/world/entity/ExperienceOrb;repairPlayerItems(Lnet/minecraft/world/entity/player/Player;I)I"
			*///?}
		),
		index = 1
	)
	private int xcraft$multiplyXp(int value) {
		return (int) Math.min(Integer.MAX_VALUE, (long) value * XcraftConfig.xpMultiplier);
	}
}
