package dev.polv.xcraft.forge.ancient;

import dev.polv.xcraft.forge.Config;
import dev.polv.xcraft.forge.TestCapture;
import dev.polv.xcraft.forge.Xcraft;
import net.minecraftforge.event.EventPriority;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
//? if <1.4.5 {
/*import net.minecraft.src.*;
*///?} else {
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
//?}

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/** Mob drops, XP and (with the coremod) ore drops, as Forge event handlers. */
public class Events {
	private static final String MULTIPLIED = "xcraftMultiplied";
	private static Field xpValue;
	/** Set while extra item entities are spawned, so they are not multiplied again. */
	private static boolean spawningExtra;

	/** Mob drops: the event carries loot and equipment; equipment (the slot's own stack) is left alone. */
	@ForgeSubscribe(priority = EventPriority.LOW)
	public void onLivingDrops(LivingDropsEvent event) {
		int multiplier = Config.mobs();
		if (multiplier <= 1 || event.entityLiving instanceof EntityPlayer) return;
		List<ItemStack> equipment = new ArrayList<ItemStack>();
		//? if >=1.4.2 {
		for (int slot = 0; slot < 5; slot++) equipment.add(event.entityLiving.getCurrentItemOrArmor(slot));
		//?}
		List<EntityItem> extra = new ArrayList<EntityItem>();
		for (EntityItem drop : event.drops) {
			ItemStack stack = stackOf(drop);
			if (stack == null || stack.stackSize <= 0 || containsSame(equipment, stack)) continue;
			Xcraft.debug("mob loot x" + multiplier + ": " + stack.stackSize + " -> " + stack.stackSize * multiplier);
			int[] parts = Xcraft.split(stack.stackSize, multiplier, stack.getMaxStackSize());
			stack.stackSize = parts[0];
			for (int i = 1; i < parts.length; i++) {
				ItemStack copy = stack.copy();
				copy.stackSize = parts[i];
				EntityItem item = new EntityItem(drop.worldObj, drop.posX, drop.posY, drop.posZ, copy);
				item.delayBeforeCanPickup = 10;
				extra.add(item);
			}
		}
		event.drops.addAll(extra);
	}

	/**
	 * XP (there is no pickup event before 1.7): orbs are multiplied once when they enter the world, and marked so
	 * a reloaded orb isn't multiplied again. Ores (coremod): items a player's ore harvest spawns are multiplied.
	 */
	@ForgeSubscribe(priority = EventPriority.LOW)
	public void onJoinWorld(EntityJoinWorldEvent event) {
		if (event.world.isRemote) return;
		if (event.entity instanceof EntityXPOrb) {
			multiplyOrb((EntityXPOrb) event.entity);
		} else if (event.entity instanceof EntityItem && !spawningExtra) {
			EntityItem item = (EntityItem) event.entity;
			Block ore = OreHarvest.harvestingOre(item.posX, item.posY, item.posZ);
			if (ore != null && OreHarvest.multiplies(ore, stackOf(item))) multiplyOreDrop(item);
			if (TestCapture.active()) record(stackOf(item));
		}
	}

	private static void multiplyOreDrop(EntityItem item) {
		ItemStack stack = stackOf(item);
		int multiplier = Config.ores();
		Xcraft.debug("ore x" + multiplier + ": " + stack.stackSize);
		int[] parts = Xcraft.split(stack.stackSize, multiplier, stack.getMaxStackSize());
		stack.stackSize = parts[0];
		spawningExtra = true;
		try {
			for (int i = 1; i < parts.length; i++) {
				ItemStack copy = stack.copy();
				copy.stackSize = parts[i];
				EntityItem extra = new EntityItem(item.worldObj, item.posX, item.posY, item.posZ, copy);
				extra.delayBeforeCanPickup = item.delayBeforeCanPickup;
				item.worldObj.spawnEntityInWorld(extra);
				if (TestCapture.active()) record(copy);
			}
		} finally {
			spawningExtra = false;
		}
	}

	private static void multiplyOrb(EntityXPOrb orb) {
		NBTTagCompound data = orb.getEntityData();
		if (Config.xp() <= 1 || data.getBoolean(MULTIPLIED)) return;
		data.setBoolean(MULTIPLIED, true);
		try {
			Field field = xpValue(orb);
			field.setInt(orb, Xcraft.multiplyXp(orb.getXpValue()));
		} catch (Exception e) {
			Xcraft.error("Could not multiply an XP orb", e);
		}
	}

	/**
	 * The orb's private xpValue field. Its name depends on the runtime (obfuscated before 1.6, SRG names on 1.6),
	 * so it is found as the int field that getXpValue() reads.
	 */
	private static Field xpValue(EntityXPOrb orb) throws IllegalAccessException {
		if (xpValue != null) return xpValue;
		int value = orb.getXpValue();
		for (Field field : EntityXPOrb.class.getDeclaredFields()) {
			if (field.getType() != int.class || java.lang.reflect.Modifier.isStatic(field.getModifiers())) continue;
			field.setAccessible(true);
			if (field.getInt(orb) != value) continue;
			field.setInt(orb, value + 12345);
			boolean match = orb.getXpValue() == value + 12345;
			field.setInt(orb, value);
			if (match) return xpValue = field;
		}
		throw new IllegalStateException("no xpValue field in " + EntityXPOrb.class.getName());
	}

	private static void record(ItemStack stack) {
		if (stack == null) return;
		Item item = stack.getItem();
		TestCapture.record(item == Item.beefRaw ? TestCapture.BEEF : item == Item.leather ? TestCapture.LEATHER : TestCapture.OTHER,
			stack.stackSize);
	}

	static ItemStack stackOf(EntityItem item) {
		//? if >=1.4.7 {
		return item.getEntityItem();
		//?} else if >=1.4.6 {
		/*return item.func_92014_d(); // unnamed in 1.4.6's MCP names
		*///?} else {
		/*return item.item;
		*///?}
	}

	private static boolean containsSame(List<ItemStack> stacks, ItemStack stack) {
		for (ItemStack s : stacks) if (s == stack) return true;
		return false;
	}
}
