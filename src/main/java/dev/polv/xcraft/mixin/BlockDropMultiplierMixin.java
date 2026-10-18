package dev.polv.xcraft.mixin;

import dev.polv.xcraft.MultiplierUtil;
import dev.polv.xcraft.config.Multiplier;
import dev.polv.xcraft.config.XcraftConfig;
import net.minecraft.core.BlockPos;
//? if >=1.19.3 {
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
//?} else {
/*import net.minecraft.core.Registry;
*///?}
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
//? if >=1.18.2 {
import net.minecraft.tags.TagKey;
//?}
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(Block.class)
public abstract class BlockDropMultiplierMixin {

	//? if >=1.19.3 {
	@Unique
	private static final TagKey<Block> XCRAFT_C_ORES = TagKey.create(Registries.BLOCK, xcraft$id("c", "ores"));
	@Unique
	private static final TagKey<Block> XCRAFT_FORGE_ORES = TagKey.create(Registries.BLOCK, xcraft$id("forge", "ores"));
	//?} else if >=1.18.2 {
	/*@Unique
	private static final TagKey<Block> XCRAFT_C_ORES = TagKey.create(Registry.BLOCK_REGISTRY, xcraft$id("c", "ores"));
	@Unique
	private static final TagKey<Block> XCRAFT_FORGE_ORES = TagKey.create(Registry.BLOCK_REGISTRY, xcraft$id("forge", "ores"));
	*///?}

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
	/*private static void xcraft$multiplyOreDrops(BlockState state, ServerLevel level, BlockPos pos, BlockEntity blockEntity, Entity entity, net.minecraft.world.item.ItemInstance tool, CallbackInfoReturnable<List<ItemStack>> cir) {
	*///?} else {
	private static void xcraft$multiplyOreDrops(BlockState state, ServerLevel level, BlockPos pos, BlockEntity blockEntity, Entity entity, ItemStack tool, CallbackInfoReturnable<List<ItemStack>> cir) {
	//?}
		int multiplier = XcraftConfig.get(Multiplier.ORES);
		if (multiplier <= 1 || !xcraft$isOre(state)) return;

		List<ItemStack> drops = cir.getReturnValue();
		// Silk Touch (or any mod) dropping the ore block itself: don't duplicate the block.
		Item blockItem = state.getBlock().asItem();
		for (ItemStack drop : drops) {
			//? if >=1.17 {
			if (drop.is(blockItem)) return;
			//?} else {
			/*if (drop.getItem() == blockItem) return;
			*///?}
		}

		List<ItemStack> multiplied = new ArrayList<>(drops.size() * multiplier);
		for (ItemStack drop : drops) {
			MultiplierUtil.multiply(drop, multiplier, multiplied::add);
		}
		cir.setReturnValue(multiplied);
	}

	@Unique
	private static boolean xcraft$isOre(BlockState state) {
		// Before 1.18.2 there are no TagKeys; the "_ore" id check below still covers vanilla and most mods
		//? if >=1.18.2 {
		if (state.is(XCRAFT_C_ORES) || state.is(XCRAFT_FORGE_ORES)) return true;
		//?}
		//? if >=1.19.3 {
		return BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath().endsWith("_ore");
		//?} else {
		/*return Registry.BLOCK.getKey(state.getBlock()).getPath().endsWith("_ore");
		*///?}
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
