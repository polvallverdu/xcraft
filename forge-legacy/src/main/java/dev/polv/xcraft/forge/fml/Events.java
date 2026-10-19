package dev.polv.xcraft.forge.fml;

import dev.polv.xcraft.forge.Config;
import dev.polv.xcraft.forge.Xcraft;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerPickupXpEvent;
import net.minecraftforge.event.world.BlockEvent;
//? if >=1.9 {
import net.minecraft.inventory.EntityEquipmentSlot;
//?}
//? if >=1.8 {
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
//?} else {
/*import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
*///?}

import java.util.ArrayList;
import java.util.List;

/** The four multipliers, as Forge event handlers (no mixins or coremods needed on these versions). */
public class Events {

	/** Ore drops: Forge fires this for the normal drop call and (with isSilkTouching) for Silk Touch. */
	@SubscribeEvent(priority = EventPriority.LOW)
	public void onHarvestDrops(BlockEvent.HarvestDropsEvent event) {
		int multiplier = Config.ores();
		//? if >=1.9 {
		EntityPlayer harvester = event.getHarvester();
		boolean silkTouch = event.isSilkTouching();
		Block block = event.getState().getBlock();
		List<ItemStack> drops = event.getDrops();
		//?} else if >=1.8 {
		/*EntityPlayer harvester = event.harvester;
		boolean silkTouch = event.isSilkTouching;
		Block block = event.state.getBlock();
		List<ItemStack> drops = event.drops;
		*///?} else {
		/*EntityPlayer harvester = event.harvester;
		boolean silkTouch = event.isSilkTouching;
		Block block = event.block;
		List<ItemStack> drops = event.drops;
		*///?}
		if (multiplier <= 1 || harvester == null || silkTouch || !isOre(block)) return;
		Item blockItem = Item.getItemFromBlock(block);
		List<ItemStack> out = new ArrayList<ItemStack>();
		for (ItemStack stack : drops) {
			// The ore block itself (e.g. iron ore before 1.17) is never multiplied
			if (empty(stack) || stack.getItem() == blockItem) {
				out.add(stack);
				continue;
			}
			Xcraft.debug("ore x" + multiplier + ": " + count(stack));
			for (int part : Xcraft.split(count(stack), multiplier, stack.getMaxStackSize())) {
				ItemStack copy = stack.copy();
				setCount(copy, part);
				out.add(copy);
			}
		}
		drops.clear();
		drops.addAll(out);
	}

	/** Mob drops: the event carries loot and equipment; equipment (the slot's own stack) is left alone. */
	@SubscribeEvent(priority = EventPriority.LOW)
	public void onLivingDrops(LivingDropsEvent event) {
		int multiplier = Config.mobs();
		//? if >=1.9 {
		EntityLivingBase entity = event.getEntityLiving();
		List<EntityItem> drops = event.getDrops();
		//?} else {
		/*EntityLivingBase entity = event.entityLiving;
		List<EntityItem> drops = event.drops;
		*///?}
		if (multiplier <= 1 || entity instanceof EntityPlayer) return;
		List<ItemStack> equipment = equipment(entity);
		List<EntityItem> extra = new ArrayList<EntityItem>();
		for (EntityItem drop : drops) {
			ItemStack stack = itemOf(drop);
			if (empty(stack) || containsSame(equipment, stack)) continue;
			int[] parts = Xcraft.split(count(stack), multiplier, stack.getMaxStackSize());
			Xcraft.debug("mob loot x" + multiplier + ": " + count(stack) + " -> " + count(stack) * multiplier);
			ItemStack first = stack.copy();
			setCount(first, parts[0]);
			setItem(drop, first);
			for (int i = 1; i < parts.length; i++) {
				ItemStack copy = stack.copy();
				setCount(copy, parts[i]);
				EntityItem item = new EntityItem(worldOf(drop), drop.posX, drop.posY, drop.posZ, copy);
				//? if >=1.8 {
				item.setDefaultPickupDelay();
				//?} else {
				/*item.delayBeforeCanPickup = 10;
				*///?}
				extra.add(item);
			}
		}
		drops.addAll(extra);
	}

	/** XP: Forge's access transformer makes the orb's value public on every version. */
	@SubscribeEvent(priority = EventPriority.LOW)
	public void onPickupXp(PlayerPickupXpEvent event) {
		if (Config.xp() <= 1) return;
		//? if >=1.9 {
		event.getOrb().xpValue = Xcraft.multiplyXp(event.getOrb().xpValue);
		//?} else {
		/*event.orb.xpValue = Xcraft.multiplyXp(event.orb.xpValue);
		*///?}
	}

	/** Furnaces: after the world ticked its tile entities, push every smelting furnace forward. */
	@SubscribeEvent
	public void onWorldTick(TickEvent.WorldTickEvent event) {
		World world = event.world;
		if (event.phase != TickEvent.Phase.END || world.isRemote || Config.furnace() <= 1) return;
		List<?> tiles = world.loadedTileEntityList;
		for (int i = 0; i < tiles.size(); i++) {
			Object tile = tiles.get(i);
			if (!(tile instanceof TileEntityFurnace) || ((TileEntity) tile).isInvalid()) continue;
			TileEntityFurnace furnace = (TileEntityFurnace) tile;
			//? if >=1.8 {
			if (!world.isBlockLoaded(furnace.getPos())) continue;
			furnace.setField(2, Xcraft.boostCookTime(furnace.getField(0), furnace.getField(2), furnace.getField(3)));
			//?} else {
			/*if (!world.blockExists(furnace.xCoord, furnace.yCoord, furnace.zCoord)) continue;
			// 1.7 has no total cook time: an item is done when the cook time reaches 200
			furnace.furnaceCookTime = Xcraft.boostCookTime(furnace.furnaceBurnTime, furnace.furnaceCookTime, 200);
			*///?}
		}
	}

	private static boolean isOre(Block block) {
		//? if >=1.9 {
		return String.valueOf(Block.REGISTRY.getNameForObject(block)).endsWith("_ore");
		//?} else {
		/*return String.valueOf(Block.blockRegistry.getNameForObject(block)).endsWith("_ore");
		*///?}
	}

	private static List<ItemStack> equipment(EntityLivingBase entity) {
		List<ItemStack> out = new ArrayList<ItemStack>();
		//? if >=1.9 {
		for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) out.add(entity.getItemStackFromSlot(slot));
		//?} else {
		/*for (int slot = 0; slot < 5; slot++) out.add(entity.getEquipmentInSlot(slot));
		*///?}
		return out;
	}

	private static boolean containsSame(List<ItemStack> stacks, ItemStack stack) {
		for (ItemStack s : stacks) if (s == stack) return true;
		return false;
	}

	private static World worldOf(EntityItem item) {
		//? if >=1.10 {
		return item.world;
		//?} else {
		/*return item.worldObj;
		*///?}
	}

	private static ItemStack itemOf(EntityItem item) {
		//? if >=1.11 {
		return item.getItem();
		//?} else {
		/*return item.getEntityItem();
		*///?}
	}

	private static void setItem(EntityItem item, ItemStack stack) {
		//? if >=1.11 {
		item.setItem(stack);
		//?} else {
		/*item.setEntityItemStack(stack);
		*///?}
	}

	private static boolean empty(ItemStack stack) {
		//? if >=1.11 {
		return stack == null || stack.isEmpty();
		//?} else {
		/*return stack == null || stack.stackSize <= 0;
		*///?}
	}

	private static int count(ItemStack stack) {
		//? if >=1.11 {
		return stack.getCount();
		//?} else {
		/*return stack.stackSize;
		*///?}
	}

	private static void setCount(ItemStack stack, int count) {
		//? if >=1.11 {
		stack.setCount(count);
		//?} else {
		/*stack.stackSize = count;
		*///?}
	}
}
