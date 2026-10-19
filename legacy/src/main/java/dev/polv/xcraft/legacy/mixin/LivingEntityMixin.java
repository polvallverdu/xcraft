package dev.polv.xcraft.legacy.mixin;

import dev.polv.xcraft.legacy.Config;
import dev.polv.xcraft.legacy.Drops;
//? if >=1.6 {
import net.minecraft.entity.LivingEntity;
//?} else {
/*import net.minecraft.entity.mob.MobEntity;
*///?}
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mob drops: marks the loot part of a mob's death drops (not its equipment, never a player's inventory);
 * EntityMixin multiplies every stack dropped while marked. Before 1.6 there is no LivingEntity: the death
 * drops live in MobEntity.
 */
//? if >=1.6 {
@Mixin(LivingEntity.class)
//?} else {
/*@Mixin(MobEntity.class)
*///?}
public abstract class LivingEntityMixin {

	//? if >=1.9 {
	@Inject(method = "onKilled", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;method_13044(ZILnet/minecraft/entity/damage/DamageSource;)V"))
	//?} else if >=1.6 {
	/*@Inject(method = "onKilled", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;dropLoot(ZI)V"))
	*///?} else {
	/*@Inject(method = "dropInventory", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/MobEntity;method_2587(ZI)V"))
	*///?}
	private void xcraft$beforeLoot(CallbackInfo ci) {
		if (Config.mobs() > 1 && !((Object) this instanceof PlayerEntity)) Drops.MOB_LOOT.set(true);
	}

	//? if >=1.9 {
	@Inject(method = "onKilled", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;method_13044(ZILnet/minecraft/entity/damage/DamageSource;)V", shift = At.Shift.AFTER))
	//?} else if >=1.6 {
	/*@Inject(method = "onKilled", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;dropLoot(ZI)V", shift = At.Shift.AFTER))
	*///?} else {
	/*@Inject(method = "dropInventory", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/MobEntity;method_2587(ZI)V", shift = At.Shift.AFTER))
	*///?}
	private void xcraft$afterLoot(CallbackInfo ci) {
		Drops.MOB_LOOT.set(false);
	}

	//? if >=1.9 {
	// The equipment drop runs inside the loot method from 1.9; keep it unmultiplied
	@Inject(method = "method_13044", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;method_4472(ZI)V"))
	private void xcraft$pauseForEquipment(CallbackInfo ci) {
		if (Drops.MOB_LOOT.get()) {
			Drops.MOB_LOOT.set(false);
			Drops.MOB_LOOT_PAUSED.set(true);
		}
	}

	@Inject(method = "method_13044", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;method_4472(ZI)V", shift = At.Shift.AFTER))
	private void xcraft$resumeAfterEquipment(CallbackInfo ci) {
		if (Drops.MOB_LOOT_PAUSED.get()) {
			Drops.MOB_LOOT_PAUSED.set(false);
			Drops.MOB_LOOT.set(true);
		}
	}
	//?} else if >=1.8 {
	/*// Rare drops (e.g. a zombie's iron ingot) are loot too
	@Inject(method = "onKilled", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;method_4473()V"))
	private void xcraft$beforeRareDrop(CallbackInfo ci) {
		xcraft$beforeLoot(ci);
	}

	@Inject(method = "onKilled", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;method_4473()V", shift = At.Shift.AFTER))
	private void xcraft$afterRareDrop(CallbackInfo ci) {
		Drops.MOB_LOOT.set(false);
	}
	*///?} else if >=1.6 {
	/*// Rare drops (e.g. a zombie's iron ingot) are loot too
	@Inject(method = "onKilled", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;method_2672(I)V"))
	private void xcraft$beforeRareDrop(CallbackInfo ci) {
		xcraft$beforeLoot(ci);
	}

	@Inject(method = "onKilled", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;method_2672(I)V", shift = At.Shift.AFTER))
	private void xcraft$afterRareDrop(CallbackInfo ci) {
		Drops.MOB_LOOT.set(false);
	}
	*///?} else {
	/*// Rare drops (e.g. a zombie's iron ingot) are loot too
	@Inject(method = "dropInventory", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/MobEntity;dropRandomItem(I)V"))
	private void xcraft$beforeRareDrop(CallbackInfo ci) {
		xcraft$beforeLoot(ci);
	}

	@Inject(method = "dropInventory", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/MobEntity;dropRandomItem(I)V", shift = At.Shift.AFTER))
	private void xcraft$afterRareDrop(CallbackInfo ci) {
		Drops.MOB_LOOT.set(false);
	}
	*///?}
}
