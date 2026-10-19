package dev.polv.xcraft.forge.ml;

import dev.polv.xcraft.forge.Commands;
import dev.polv.xcraft.forge.TestCapture;
import dev.polv.xcraft.forge.Xcraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
//? if >=1.14 {
/*import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.util.Hand;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
*///?} else {
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.util.EnumHand;
import net.minecraft.world.WorldServer;
//?}
//? if >=1.14.4 {
/*import net.minecraft.world.server.ServerWorld;
*///?} else if >=1.14 {
/*import net.minecraft.world.ServerWorld;
*///?}

/**
 * {@code /xcraft test} (only with -Dxcraft.debug=true), for the CI server test: kills a cow next to spawn, has a fake
 * player holding an iron pickaxe break a coal ore the way the game does (1.13: the harvest call, which fires
 * HarvestDropsEvent; 1.14+: BreakEvent, then the harvest call) and pick up an XP orb, then reports what dropped.
 * Furnaces are checked in the world by the server test itself, so the furnace field is -1.
 */
public class SelfTest implements Commands.Test {
	private static final int XP_ORB = 7;

	@Override
	public String run() {
		try {
			//? if >=1.14 {
			/*ServerWorld world = ServerLifecycleHooks.getCurrentServer().getWorlds().iterator().next();
			*///?} else {
			WorldServer world = ServerLifecycleHooks.getCurrentServer().getWorlds().iterator().next();
			//?}
			BlockPos spawn = world.getSpawnPoint();
			double x = spawn.getX() + 0.5, y = spawn.getY() + 1, z = spawn.getZ() + 0.5;

			TestCapture.begin();
			//? if >=1.14 {
			/*CowEntity cow = EntityType.COW.create(world);
			cow.setLocationAndAngles(x, y, z, 0, 0);
			world.addEntity(cow);
			*///?} else {
			EntityCow cow = new EntityCow(world);
			cow.setLocationAndAngles(x, y, z, 0, 0);
			world.spawnEntity(cow);
			//?}
			cow.attackEntityFrom(DamageSource.OUT_OF_WORLD, 1000);
			int[] mob = TestCapture.end();

			FakePlayer player = FakePlayerFactory.getMinecraft(world);
			ItemStack pickaxe = new ItemStack(Items.IRON_PICKAXE);
			//? if >=1.14 {
			/*player.setHeldItem(Hand.MAIN_HAND, pickaxe);
			*///?} else {
			player.setHeldItem(EnumHand.MAIN_HAND, pickaxe);
			//?}
			BlockPos pos = spawn.up(3);
			TestCapture.begin();
			//? if >=1.14 {
			/*BlockState coalOre = Blocks.COAL_ORE.getDefaultState();
			// Forge 1.14+ has no HarvestDropsEvent: xCraft hooks BreakEvent, which the game posts before the harvest
			if (!MinecraftForge.EVENT_BUS.post(new BlockEvent.BreakEvent(world, pos, coalOre, player))) {
				Blocks.COAL_ORE.harvestBlock(world, player, pos, coalOre, null, pickaxe);
			}
			*///?} else {
			Blocks.COAL_ORE.harvestBlock(world, player, pos, Blocks.COAL_ORE.getDefaultState(), null, pickaxe);
			//?}
			int[] ore = TestCapture.end();
			//? if >=1.14 {
			/*player.setHeldItem(Hand.MAIN_HAND, ItemStack.EMPTY);
			*///?} else {
			player.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
			//?}

			int before = player.experienceTotal;
			//? if >=1.14 {
			/*ExperienceOrbEntity orb = new ExperienceOrbEntity(world, x, y, z, XP_ORB);
			*///?} else {
			EntityXPOrb orb = new EntityXPOrb(world, x, y, z, XP_ORB);
			//?}
			orb.delayBeforeCanPickup = 0;
			player.xpCooldown = 0;
			orb.onCollideWithPlayer(player);
			int xp = player.experienceTotal - before;
			return TestCapture.format(-1, mob, ore, xp, XP_ORB);
		} catch (RuntimeException e) {
			TestCapture.end();
			Xcraft.error("xcraft test failed", e);
			return "xcraft-test error " + e;
		}
	}
}
