package dev.polv.xcraft.mixin;

import dev.polv.xcraft.MultiplierUtil;
import dev.polv.xcraft.config.XcraftConfig;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.Consumer;

@Mixin(LivingEntity.class)
public abstract class EntityDropMultiplierMixin {

	@ModifyArg(
		//? if >=1.21.2 {
		/*method = "dropFromLootTable(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;ZLnet/minecraft/resources/ResourceKey;Ljava/util/function/Consumer;)V",
		*///?} else {
		method = "dropFromLootTable(Lnet/minecraft/world/damagesource/DamageSource;Z)V",
		//?}
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/storage/loot/LootTable;getRandomItems(Lnet/minecraft/world/level/storage/loot/LootParams;JLjava/util/function/Consumer;)V"
		),
		index = 2
	)
	private Consumer<ItemStack> xcraft$multiplyMobDrops(Consumer<ItemStack> output) {
		if ((Object) this instanceof Player) return output;
		int multiplier = XcraftConfig.entityDropMultiplier;
		if (multiplier <= 1) return output;
		return stack -> MultiplierUtil.multiply(stack, multiplier, output);
	}
}
