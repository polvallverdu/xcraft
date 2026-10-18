package dev.polv.xcraft.ornithe.mixin;

import dev.polv.xcraft.ornithe.Config;
import dev.polv.xcraft.ornithe.Drops;
import net.minecraft.entity.living.LivingEntity;
import net.minecraft.entity.living.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mob drops: marks the loot part of a mob's death drops (not its equipment, never a player's inventory);
 * EntityMixin multiplies every stack dropped while marked.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

	@Inject(method = "die", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/living/LivingEntity;dropItemsFromLootTable(ZILnet/minecraft/entity/damage/DamageSource;)V"))
	private void xcraft$beforeLoot(CallbackInfo ci) {
		if (Config.mobs() > 1 && !((Object) this instanceof PlayerEntity)) Drops.MOB_LOOT.set(true);
	}

	@Inject(method = "die", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/living/LivingEntity;dropItemsFromLootTable(ZILnet/minecraft/entity/damage/DamageSource;)V", shift = At.Shift.AFTER))
	private void xcraft$afterLoot(CallbackInfo ci) {
		Drops.MOB_LOOT.set(false);
	}

	// The equipment drop runs inside the loot method; keep it unmultiplied
	@Inject(method = "dropItemsFromLootTable", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/living/LivingEntity;dropEquipment(ZI)V"))
	private void xcraft$pauseForEquipment(CallbackInfo ci) {
		if (Drops.MOB_LOOT.get()) {
			Drops.MOB_LOOT.set(false);
			Drops.MOB_LOOT_PAUSED.set(true);
		}
	}

	@Inject(method = "dropItemsFromLootTable", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/living/LivingEntity;dropEquipment(ZI)V", shift = At.Shift.AFTER))
	private void xcraft$resumeAfterEquipment(CallbackInfo ci) {
		if (Drops.MOB_LOOT_PAUSED.get()) {
			Drops.MOB_LOOT_PAUSED.set(false);
			Drops.MOB_LOOT.set(true);
		}
	}
}
