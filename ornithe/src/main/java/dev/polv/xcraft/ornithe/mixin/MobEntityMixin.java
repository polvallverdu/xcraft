package dev.polv.xcraft.ornithe.mixin;

import dev.polv.xcraft.ornithe.Drops;
import net.minecraft.entity.living.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Mobs with a loot table drop their equipment inside the loot method; keep equipment unmultiplied. */
@Mixin(MobEntity.class)
public abstract class MobEntityMixin {

	@Inject(method = "dropItemsFromLootTable", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/living/mob/MobEntity;dropEquipment(ZI)V"))
	private void xcraft$pauseForEquipment(CallbackInfo ci) {
		if (Drops.MOB_LOOT.get()) {
			Drops.MOB_LOOT.set(false);
			Drops.MOB_LOOT_PAUSED.set(true);
		}
	}

	@Inject(method = "dropItemsFromLootTable", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/living/mob/MobEntity;dropEquipment(ZI)V", shift = At.Shift.AFTER))
	private void xcraft$resumeAfterEquipment(CallbackInfo ci) {
		if (Drops.MOB_LOOT_PAUSED.get()) {
			Drops.MOB_LOOT_PAUSED.set(false);
			Drops.MOB_LOOT.set(true);
		}
	}
}
