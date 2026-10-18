package dev.polv.xcraft.legacy.mixin;

import dev.polv.xcraft.legacy.Config;
import net.minecraft.entity.ExperienceOrbEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/** XP given to the player on pickup (from 1.9 after Mending has repaired items). */
@Mixin(ExperienceOrbEntity.class)
public abstract class ExperienceOrbEntityMixin {

	//? if >=1.13 {
	/*@ModifyArg(method = "onPlayerCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;method_15934(I)V"))
	*///?} else {
	@ModifyArg(method = "onPlayerCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;addExperience(I)V"))
	//?}
	private int xcraft$multiplyXp(int amount) {
		return (int) Math.min(Integer.MAX_VALUE, (long) amount * Config.xp());
	}
}
