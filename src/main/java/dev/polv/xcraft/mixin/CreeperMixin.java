package dev.polv.xcraft.mixin;

import dev.polv.xcraft.MultiplierUtil;
import net.minecraft.world.entity.monster.Creeper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A mob killed by a charged creeper drops its head outside its loot table: it counts the head on the creeper,
 * then drops it. Mark that drop so DropSpawnMixin multiplies it like the rest of the mob's loot.
 */
@Mixin(Creeper.class)
public abstract class CreeperMixin {

	// Optional: from 26.1 the creeper no longer counts heads this way (the smoke test checks heads are still x3)
	@Inject(method = "increaseDroppedSkulls", at = @At("HEAD"), require = 0)
	private void xcraft$markMobHead(CallbackInfo ci) {
		MultiplierUtil.MOB_HEAD.set(true);
	}
}
