package dev.polv.xcraft.legacy;

//? if <1.7 {
/*import net.minecraft.block.Block;
import net.minecraft.block.entity.FurnaceBlockEntity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
*///?}

/**
 * {@code /xcraft test} (only with -Dxcraft.debug=true): vanilla before 1.7 has no /setblock or /summon, so the CI
 * server test can't build its scenarios over RCON. This builds them next to spawn through the real game paths
 * (furnace ticks, a cow killed, a coal ore harvested by a stand-in player, an XP orb picked up) and reports the
 * results on one line: {@code xcraft-test furnace=<smelted> beef=<stacks> leather=<stacks> ore=<stacks> xp=<points>}.
 */
public final class SelfTest {
	private SelfTest() {
	}

	//? if <1.7 {
	/*private static final int XP_ORB = 7;

	public static String run() {
		World world = MinecraftServer.getServer().worlds[0];
		int x = world.getLevelProperties().getSpawnX();
		int z = world.getLevelProperties().getSpawnZ();
		int y = world.getHighestBlockY(x, z) + 2;
		List<String> captured = new ArrayList<>();
		Drops.TEST_CAPTURE.set(captured);
		try {
			// Furnace: 200 ticks smelt one item in vanilla
			setBlock(world, x, y, z, Block.FURNACE.id);
			FurnaceBlockEntity furnace = (FurnaceBlockEntity) world.getBlockEntity(x, y, z);
			furnace.setInvStack(0, new ItemStack(Block.IRON_ORE, 16));
			furnace.setInvStack(1, new ItemStack(Item.COAL, 4));
			for (int i = 0; i < 200; i++) furnace.method_545();
			ItemStack output = furnace.getInvStack(2);
			int smelted = output == null ? 0 : output.count;
			furnace.setInvStack(0, null);
			furnace.setInvStack(1, null);
			furnace.setInvStack(2, null);
			setBlock(world, x, y, z, 0);

			// Mob loot
			CowEntity cow = new CowEntity(world);
			cow.refreshPositionAndAngles(x + 0.5, y, z + 0.5, 0, 0);
			world.spawnEntity(cow);
			int start = captured.size();
			cow.damage(DamageSource.OUT_OF_WORLD, 1000);
			List<String> mob = new ArrayList<>(captured.subList(start, captured.size()));

			// Ore: the same harvest call the game makes when a player breaks the block
			PlayerEntity player = new TestPlayer(world);
			start = captured.size();
			Block.COAL_ORE.method_424(world, player, x, y, z, 0);
			List<String> ore = new ArrayList<>(captured.subList(start, captured.size()));

			// XP
			int xpBefore = player.totalExperience;
			ExperienceOrbEntity orb = new ExperienceOrbEntity(world, x + 0.5, y, z + 0.5, XP_ORB);
			orb.pickupDelay = 0;
			player.experiencePickUpDelay = 0;
			orb.onPlayerCollision(player);
			int xp = player.totalExperience - xpBefore;

			return "xcraft-test furnace=" + smelted + " beef=" + counts(mob, "beef") + " leather=" + counts(mob, "leather")
				+ " ore=" + counts(ore, "") + " xp=" + xp + " (orb " + XP_ORB + ")";
		} catch (RuntimeException e) {
			LegacyXcraft.error("xcraft test failed", e);
			return "xcraft-test error " + e;
		} finally {
			Drops.TEST_CAPTURE.remove();
		}
	}

	private static void setBlock(World world, int x, int y, int z, int id) {
		//? if >=1.5 {
		world.method_4719(x, y, z, id);
		//?} else {
		/^world.method_3690(x, y, z, id);
		^///?}
	}

	private static String counts(List<String> drops, String filter) {
		StringBuilder out = new StringBuilder();
		for (String drop : drops) {
			int colon = drop.lastIndexOf(':');
			if (!drop.substring(0, colon).toLowerCase().contains(filter)) continue;
			if (out.length() > 0) out.append(',');
			out.append(drop.substring(colon + 1));
		}
		return out.length() == 0 ? "none" : out.toString();
	}

	/^* A player that is never added to the world, only passed to the harvest and XP pickup code. ^/
	private static final class TestPlayer extends PlayerEntity {
		TestPlayer(World world) {
			//? if >=1.6 {
			super(world, "xcraft-test");
			//?} else {
			/^super(world);
			^///?}
		}

		// CommandSource, which PlayerEntity leaves to its subclasses
		//? if >=1.6 {
		public void method_5505(net.minecraft.text.ChatMessage message) {
		}
		//?} else {
		/^public void method_3331(String message) {
		}
		^///?}

		//? if >=1.4 {
		public boolean canUseCommand(int level, String command) {
			return false;
		}

		public net.minecraft.util.math.BlockPos getPosition() {
			return null;
		}
		//?} else {
		/^public boolean method_3333(String command) {
			return false;
		}
		^///?}
	}
	*///?} else {
	public static String run() {
		return "xcraft-test is only needed before 1.7 (newer servers have /setblock and /summon)";
	}
	//?}
}
