package dev.polv.xcraft.ornithe.mixin;

import dev.polv.xcraft.ornithe.Config;
import net.minecraft.entity.ExperienceOrbEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/** XP given to the player on pickup (after Mending has repaired items). */
@Mixin(ExperienceOrbEntity.class)
public abstract class ExperienceOrbEntityMixin {

	@ModifyArg(method = "onPlayerCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/living/player/PlayerEntity;increaseXp(I)V"))
	private int xcraft$multiplyXp(int amount) {
		return (int) Math.min(Integer.MAX_VALUE, (long) amount * Config.xp());
	}
}
