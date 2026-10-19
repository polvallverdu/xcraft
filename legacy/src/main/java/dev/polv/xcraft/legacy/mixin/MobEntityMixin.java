package dev.polv.xcraft.legacy.mixin;

//? if >=1.9 {
import dev.polv.xcraft.legacy.Drops;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** From 1.9 mobs with a loot table drop their equipment inside the loot method; keep equipment unmultiplied. */
@Mixin(MobEntity.class)
public abstract class MobEntityMixin {

	@Inject(method = "method_13044", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/MobEntity;method_4472(ZI)V"))
	private void xcraft$pauseForEquipment(CallbackInfo ci) {
		if (Drops.MOB_LOOT.get()) {
			Drops.MOB_LOOT.set(false);
			Drops.MOB_LOOT_PAUSED.set(true);
		}
	}

	@Inject(method = "method_13044", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/MobEntity;method_4472(ZI)V", shift = At.Shift.AFTER))
	private void xcraft$resumeAfterEquipment(CallbackInfo ci) {
		if (Drops.MOB_LOOT_PAUSED.get()) {
			Drops.MOB_LOOT_PAUSED.set(false);
			Drops.MOB_LOOT.set(true);
		}
	}
}
//?} else {
/*import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;

// Before 1.9 the equipment drop is a separate call that is never marked as loot: nothing to do
@Mixin(MobEntity.class)
public abstract class MobEntityMixin {
}
*///?}
