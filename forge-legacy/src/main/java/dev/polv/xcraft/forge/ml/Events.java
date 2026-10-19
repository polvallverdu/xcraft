package dev.polv.xcraft.forge.ml;

import dev.polv.xcraft.forge.Config;
import dev.polv.xcraft.forge.Xcraft;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
//? if >=1.14 {
/*import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraft.util.IIntArray;
import net.minecraft.util.math.BlockPos;
import java.lang.reflect.Field;
*///?} else {
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.tileentity.TileEntityFurnace;
//?}
//? if >=1.14.4 {
/*import net.minecraft.world.server.ServerWorld;
*///?} else if >=1.14 {
/*import net.minecraft.world.ServerWorld;
*///?}
//? if >=1.14.4 {
/*import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
*///?} else {
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.event.entity.player.PlayerPickupXpEvent;
//?}
//? if >=1.16 {
/*import net.minecraftforge.event.RegisterCommandsEvent;
*///?} else {
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
//?}

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** The four multipliers and the command, as Forge event handlers. */
public class Events {
	//? if >=1.14 {
	/*private static final Field FURNACE_DATA = furnaceData();

	/^* The furnace's protected IIntArray (burn time, fuel total, cook time, cook total), by its SRG name. ^/
	private static Field furnaceData() {
		try {
			Field field = AbstractFurnaceTileEntity.class.getDeclaredField("field_214013_b");
			field.setAccessible(true);
			return field;
		} catch (NoSuchFieldException e) {
			throw new IllegalStateException(e);
		}
	}
	*///?}

	//? if >=1.16 {
	/*@SubscribeEvent
	public void onRegisterCommands(RegisterCommandsEvent event) {
		BrigadierCommand.register(event.getDispatcher());
	}
	*///?} else {
	@SubscribeEvent
	public void onServerStarting(FMLServerStartingEvent event) {
		BrigadierCommand.register(event.getCommandDispatcher());
	}
	//?}

	//? if >=1.14 {
	/*/^*
	 * Ore drops. Forge stopped firing HarvestDropsEvent in 1.14, so when a player breaks an ore the extra
	 * (multiplier - 1) copies are rolled from the block's drops and spawned next to vanilla's own drop.
	 ^/
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onBreak(BlockEvent.BreakEvent event) {
		int multiplier = Config.ores();
		PlayerEntity player = event.getPlayer();
		BlockState state = event.getState();
		BlockPos pos = event.getPos();
		if (multiplier <= 1 || event.isCanceled() || player == null || player.isCreative()
			|| !(event.getWorld() instanceof ServerWorld) || !isOre(state.getBlock())) return;
		ServerWorld world = (ServerWorld) event.getWorld();
		ItemStack tool = player.getHeldItemMainhand();
		if (!state.canHarvestBlock(world, pos, player)
			|| EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, tool) > 0) return;
		Item blockItem = state.getBlock().asItem();
		TileEntity tile = world.getTileEntity(pos);
		for (ItemStack stack : drops(state, world, pos, tile, player, tool)) {
			// The ore block itself (e.g. iron ore before 1.17) is never multiplied
			if (stack.isEmpty() || stack.getItem() == blockItem) continue;
			Xcraft.debug("ore x" + multiplier + ": " + stack.getCount());
			for (int part : Xcraft.split(stack.getCount(), multiplier - 1, stack.getMaxStackSize())) {
				ItemStack copy = stack.copy();
				copy.setCount(part);
				Block.spawnAsEntity(world, pos, copy);
			}
		}
	}
	*///?} else {
	/** Ore drops: Forge fires this for the normal drop call and (with isSilkTouching) for Silk Touch. */
	@SubscribeEvent(priority = EventPriority.LOW)
	public void onHarvestDrops(BlockEvent.HarvestDropsEvent event) {
		int multiplier = Config.ores();
		Block block = event.getState().getBlock();
		if (multiplier <= 1 || event.getHarvester() == null || event.isSilkTouching() || !isOre(block)) return;
		Item blockItem = block.asItem();
		List<ItemStack> drops = event.getDrops();
		List<ItemStack> out = new ArrayList<>();
		for (ItemStack stack : drops) {
			if (stack.isEmpty() || stack.getItem() == blockItem) {
				out.add(stack);
				continue;
			}
			Xcraft.debug("ore x" + multiplier + ": " + stack.getCount());
			for (int part : Xcraft.split(stack.getCount(), multiplier, stack.getMaxStackSize())) {
				ItemStack copy = stack.copy();
				copy.setCount(part);
				out.add(copy);
			}
		}
		drops.clear();
		drops.addAll(out);
	}
	//?}

	/** Mob drops: the event carries loot and equipment; equipment (the slot's own stack) is left alone. */
	@SubscribeEvent(priority = EventPriority.LOW)
	public void onLivingDrops(LivingDropsEvent event) {
		int multiplier = Config.mobs();
		//? if >=1.14 {
		/*LivingEntity entity = event.getEntityLiving();
		if (multiplier <= 1 || entity instanceof PlayerEntity) return;
		Collection<ItemEntity> drops = event.getDrops();
		List<ItemStack> equipment = new ArrayList<>();
		for (EquipmentSlotType slot : EquipmentSlotType.values()) equipment.add(entity.getItemStackFromSlot(slot));
		List<ItemEntity> extra = new ArrayList<>();
		for (ItemEntity drop : drops) {
		*///?} else {
		EntityLivingBase entity = event.getEntityLiving();
		if (multiplier <= 1 || entity instanceof EntityPlayer) return;
		Collection<EntityItem> drops = event.getDrops();
		List<ItemStack> equipment = new ArrayList<>();
		for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) equipment.add(entity.getItemStackFromSlot(slot));
		List<EntityItem> extra = new ArrayList<>();
		for (EntityItem drop : drops) {
		//?}
			ItemStack stack = drop.getItem();
			if (stack.isEmpty() || containsSame(equipment, stack)) continue;
			int[] parts = Xcraft.split(stack.getCount(), multiplier, stack.getMaxStackSize());
			Xcraft.debug("mob loot x" + multiplier + ": " + stack.getCount() + " -> " + stack.getCount() * multiplier);
			ItemStack first = stack.copy();
			first.setCount(parts[0]);
			drop.setItem(first);
			for (int i = 1; i < parts.length; i++) {
				ItemStack copy = stack.copy();
				copy.setCount(parts[i]);
				//? if >=1.14 {
				/*// The position fields went private in 1.15, and their getters are unnamed in 1.15's MCP mappings
				ItemEntity item = new ItemEntity(drop.world, 0, 0, 0, copy);
				item.copyLocationAndAnglesFrom(drop);
				*///?} else {
				EntityItem item = new EntityItem(drop.world, drop.posX, drop.posY, drop.posZ, copy);
				//?}
				item.setDefaultPickupDelay();
				extra.add(item);
			}
		}
		drops.addAll(extra);
	}

	/** XP: Forge's access transformer makes the orb's value public. */
	//? if >=1.14.4 {
	/*@SubscribeEvent(priority = EventPriority.LOW)
	public void onPickupXp(PlayerXpEvent.PickupXp event) {
	*///?} else {
	@SubscribeEvent(priority = EventPriority.LOW)
	public void onPickupXp(PlayerPickupXpEvent event) {
	//?}
		if (Config.xp() <= 1) return;
		event.getOrb().xpValue = Xcraft.multiplyXp(event.getOrb().xpValue);
	}

	/** Furnaces: after the world ticked its tile entities, push every smelting furnace forward. */
	@SubscribeEvent
	public void onWorldTick(TickEvent.WorldTickEvent event) {
		World world = event.world;
		if (event.phase != TickEvent.Phase.END || world.isRemote || Config.furnace() <= 1) return;
		List<TileEntity> tiles = world.loadedTileEntityList;
		for (int i = 0; i < tiles.size(); i++) {
			TileEntity tile = tiles.get(i);
			//? if >=1.14 {
			/*if (!(tile instanceof AbstractFurnaceTileEntity) || tile.isRemoved() || !world.isBlockLoaded(tile.getPos())) continue;
			IIntArray data;
			try {
				data = (IIntArray) FURNACE_DATA.get(tile);
			} catch (IllegalAccessException e) {
				throw new IllegalStateException(e);
			}
			// 0 = burn time, 1 = fuel's total burn time, 2 = cook time, 3 = total cook time
			data.set(2, Xcraft.boostCookTime(data.get(0), data.get(2), data.get(3)));
			*///?} else {
			if (!(tile instanceof TileEntityFurnace) || tile.isRemoved() || !world.isBlockLoaded(tile.getPos())) continue;
			TileEntityFurnace furnace = (TileEntityFurnace) tile;
			furnace.setField(2, Xcraft.boostCookTime(furnace.getField(0), furnace.getField(2), furnace.getField(3)));
			//?}
		}
	}

	// The static Block.getDrops is unnamed (func_220077_a) in the 1.14.4 and 1.15 MCP mappings
	//? if >=1.14.4 <1.15.1 {
	/*private static List<ItemStack> drops(BlockState state, ServerWorld world, BlockPos pos, TileEntity tile, PlayerEntity player, ItemStack tool) {
		return Block.func_220077_a(state, world, pos, tile, player, tool);
	}
	*///?} else if >=1.14 {
	/*private static List<ItemStack> drops(BlockState state, ServerWorld world, BlockPos pos, TileEntity tile, PlayerEntity player, ItemStack tool) {
		return Block.getDrops(state, world, pos, tile, player, tool);
	}
	*///?}

	private static boolean isOre(Block block) {
		return String.valueOf(block.getRegistryName()).endsWith("_ore");
	}

	private static boolean containsSame(List<ItemStack> stacks, ItemStack stack) {
		for (ItemStack s : stacks) if (s == stack) return true;
		return false;
	}
}
