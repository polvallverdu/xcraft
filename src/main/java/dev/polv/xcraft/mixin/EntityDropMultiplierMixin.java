package dev.polv.xcraft.mixin;

import dev.polv.xcraft.MultiplierUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(LivingEntity.class)
public abstract class EntityDropMultiplierMixin {

	@ModifyArg(
		//? if >=1.21.10 {
		/*method = "dropFromLootTable(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;ZLnet/minecraft/resources/ResourceKey;Ljava/util/function/Consumer;)V",
		*///?} else if >=1.21.2 {
		/*method = "dropFromLootTable(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;Z)V",
		*///?} else {
		method = "dropFromLootTable(Lnet/minecraft/world/damagesource/DamageSource;Z)V",
		//?}
		//? if >=1.20 {
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/storage/loot/LootTable;getRandomItems(Lnet/minecraft/world/level/storage/loot/LootParams;JLjava/util/function/Consumer;)V"
		),
		index = 2
		//?} else if forge && >=1.19 {
		/*// Forge <1.20 patches this to getRandomItems(context).forEach(this::spawnAtLocation)
		at = @At(
			value = "INVOKE",
			target = "Lit/unimi/dsi/fastutil/objects/ObjectArrayList;forEach(Ljava/util/function/Consumer;)V"
		),
		index = 0
		*///?} else if forge {
		/*at = @At(
			value = "INVOKE",
			target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V"
		),
		index = 0
		*///?} else {
		/*at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/storage/loot/LootTable;getRandomItems(Lnet/minecraft/world/level/storage/loot/LootContext;Ljava/util/function/Consumer;)V"
		),
		index = 1
		*///?}
	)
	private Consumer<ItemStack> xcraft$multiplyMobDrops(Consumer<ItemStack> output) {
		LivingEntity self = (LivingEntity) (Object) this;
		if (self instanceof Player) return output;
		// Its own mobDropMultipliers entry, else hostile (Enemy: zombies, slimes, ghasts...) or passive, else mobs
		int multiplier = MultiplierUtil.mobMultiplier(self);
		if (multiplier <= 1) return output;
		return stack -> {
			// Loot is multiplied here: never again as a charged-creeper head (DropSpawnMixin)
			MultiplierUtil.MOB_HEAD.set(false);
			MultiplierUtil.multiply(stack, multiplier, output);
		};
	}

	/** A head marked by a charged creeper but never dropped must not apply to a later drop. */
	@Inject(method = "dropAllDeathLoot", at = @At("TAIL"))
	private void xcraft$clearMobHead(CallbackInfo ci) {
		MultiplierUtil.MOB_HEAD.set(false);
	}
}
