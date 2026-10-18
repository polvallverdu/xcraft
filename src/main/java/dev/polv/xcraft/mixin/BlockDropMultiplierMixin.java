package dev.polv.xcraft.mixin;

import dev.polv.xcraft.MultiplierUtil;
import dev.polv.xcraft.config.XcraftConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(Block.class)
public abstract class BlockDropMultiplierMixin {

	@Unique
	private static final TagKey<Block> XCRAFT_C_ORES = TagKey.create(Registries.BLOCK, xcraft$id("c", "ores"));
	@Unique
	private static final TagKey<Block> XCRAFT_FORGE_ORES = TagKey.create(Registries.BLOCK, xcraft$id("forge", "ores"));

	@Inject(
		//? if >=26.1 {
		/*method = "getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemInstance;)Ljava/util/List;",
		*///?} else {
		method = "getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;",
		//?}
		at = @At("RETURN"),
		cancellable = true
	)
	//? if >=26.1 {
	/*private static void xcraft$multiplyOreDrops(BlockState state, ServerLevel level, BlockPos pos, @Nullable BlockEntity blockEntity, @Nullable Entity entity, net.minecraft.world.item.ItemInstance tool, CallbackInfoReturnable<List<ItemStack>> cir) {
	*///?} else {
	private static void xcraft$multiplyOreDrops(BlockState state, ServerLevel level, BlockPos pos, @Nullable BlockEntity blockEntity, @Nullable Entity entity, ItemStack tool, CallbackInfoReturnable<List<ItemStack>> cir) {
	//?}
		int multiplier = XcraftConfig.oreDropMultiplier;
		if (multiplier <= 1 || !xcraft$isOre(state)) return;

		List<ItemStack> drops = cir.getReturnValue();
		// Silk Touch (or any mod) dropping the ore block itself: don't duplicate the block.
		Item blockItem = state.getBlock().asItem();
		for (ItemStack drop : drops) {
			if (drop.is(blockItem)) return;
		}

		List<ItemStack> multiplied = new ArrayList<>(drops.size() * multiplier);
		for (ItemStack drop : drops) {
			MultiplierUtil.multiply(drop, multiplier, multiplied::add);
		}
		cir.setReturnValue(multiplied);
	}

	@Unique
	private static boolean xcraft$isOre(BlockState state) {
		if (state.is(XCRAFT_C_ORES) || state.is(XCRAFT_FORGE_ORES)) return true;
		return BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath().endsWith("_ore");
	}

	@Unique
	private static ResourceLocation xcraft$id(String namespace, String path) {
		//? if >=1.21 {
		return ResourceLocation.fromNamespaceAndPath(namespace, path);
		//?} else {
		/*return new ResourceLocation(namespace, path);
		*///?}
	}
}
