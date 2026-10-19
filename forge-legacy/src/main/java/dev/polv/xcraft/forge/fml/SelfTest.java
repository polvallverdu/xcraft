package dev.polv.xcraft.forge.fml;

import dev.polv.xcraft.forge.Commands;
import dev.polv.xcraft.forge.TestCapture;
import dev.polv.xcraft.forge.Xcraft;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
//? if >=1.9.4 {
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
//?} else if >=1.9 {
/*import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCoordIntPair;
*///?} else if >=1.8 {
/*import net.minecraft.util.BlockPos;
import net.minecraft.world.ChunkCoordIntPair;
*///?} else {
/*import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.ChunkCoordIntPair;
*///?}

import java.util.List;

/**
 * {@code /xcraft test} (only with -Dxcraft.debug=true), for the CI server test. Next to spawn it kills a cow, harvests
 * a coal ore as a fake player (the call the game makes once a player broke a block, which fires HarvestDropsEvent),
 * has that player pick up an XP orb, and places a furnace with iron ore and coal that the world then ticks: the next
 * run reports how much it smelted. Everything happens on the server thread: before 1.12 RCON runs commands on its
 * own thread, and changing the world from there (RCON /summon, /setblock) can crash these servers.
 */
public class SelfTest implements Commands.Test, ForgeChunkManager.LoadingCallback {
	private static final int XP_ORB = 7;
	private final Object mod;
	private int[] furnace;
	private ForgeChunkManager.Ticket ticket;

	SelfTest(Object mod) {
		this.mod = mod;
		ForgeChunkManager.setForcedChunkLoadingCallback(mod, this);
	}

	// Tickets of an earlier test are not kept
	@Override
	public void ticketsLoaded(List<ForgeChunkManager.Ticket> tickets, World world) {
		for (ForgeChunkManager.Ticket old : tickets) ForgeChunkManager.releaseTicket(old);
	}

	@Override
	public String run() {
		try {
			WorldServer world = DimensionManager.getWorld(0);
			//? if >=1.8 {
			BlockPos spawn = world.getSpawnPoint();
			int bx = spawn.getX(), by = spawn.getY() + 1, bz = spawn.getZ();
			//?} else {
			/*ChunkCoordinates spawn = world.getSpawnPoint();
			int bx = spawn.posX, by = spawn.posY + 1, bz = spawn.posZ;
			*///?}
			double x = bx + 0.5, y = by, z = bz + 0.5;
			int smelted = furnace(world, bx + 3, by, bz);

			TestCapture.begin();
			EntityCow cow = new EntityCow(world);
			cow.setLocationAndAngles(x, y, z, 0, 0);
			//? if >=1.10 {
			world.spawnEntity(cow);
			//?} else {
			/*world.spawnEntityInWorld(cow);
			*///?}
			//? if >=1.11 {
			cow.attackEntityFrom(DamageSource.OUT_OF_WORLD, 1000);
			//?} else {
			/*cow.attackEntityFrom(DamageSource.outOfWorld, 1000);
			*///?}
			int[] mob = TestCapture.end();

			FakePlayer player = FakePlayerFactory.getMinecraft(world);
			TestCapture.begin();
			//? if >=1.11 {
			BlockPos pos = new BlockPos(bx, by, bz - 3);
			Blocks.COAL_ORE.harvestBlock(world, player, pos, Blocks.COAL_ORE.getDefaultState(), null, ItemStack.EMPTY);
			//?} else if >=1.9 {
			/*BlockPos pos = new BlockPos(bx, by, bz - 3);
			Blocks.COAL_ORE.harvestBlock(world, player, pos, Blocks.COAL_ORE.getDefaultState(), null, null);
			*///?} else if >=1.8 {
			/*BlockPos pos = new BlockPos(bx, by, bz - 3);
			Blocks.coal_ore.harvestBlock(world, player, pos, Blocks.coal_ore.getDefaultState(), null);
			*///?} else {
			/*Blocks.coal_ore.harvestBlock(world, player, bx, by, bz - 3, 0);
			*///?}
			int[] ore = TestCapture.end();

			int before = player.experienceTotal;
			EntityXPOrb orb = new EntityXPOrb(world, x, y, z, XP_ORB);
			//? if >=1.8 {
			orb.delayBeforeCanPickup = 0;
			//?} else {
			/*orb.field_70532_c = 0;
			*///?}
			player.xpCooldown = 0;
			orb.onCollideWithPlayer(player);
			int xp = player.experienceTotal - before;
			return TestCapture.format(smelted, mob, ore, xp, XP_ORB);
		} catch (RuntimeException e) {
			TestCapture.end();
			Xcraft.error("xcraft test failed", e);
			return "xcraft-test error " + e;
		}
	}

	/** Places a furnace with 16 iron ore and 4 coal on the first run; reports how many ingots it made since. */
	private int furnace(WorldServer world, int x, int y, int z) {
		if (furnace != null) {
			//? if >=1.8 {
			TileEntity tile = world.getTileEntity(new BlockPos(furnace[0], furnace[1], furnace[2]));
			//?} else {
			/*TileEntity tile = world.getTileEntity(furnace[0], furnace[1], furnace[2]);
			*///?}
			if (ticket != null) ForgeChunkManager.releaseTicket(ticket);
			ticket = null;
			if (!(tile instanceof TileEntityFurnace)) return 0;
			ItemStack output = ((TileEntityFurnace) tile).getStackInSlot(2);
			//? if >=1.11 {
			return output.getCount();
			//?} else {
			/*return output == null ? 0 : output.stackSize;
			*///?}
		}
		//? if >=1.9 {
		BlockPos pos = new BlockPos(x, y, z);
		world.setBlockState(pos, Blocks.FURNACE.getDefaultState());
		TileEntityFurnace tile = (TileEntityFurnace) world.getTileEntity(pos);
		tile.setInventorySlotContents(0, new ItemStack(Blocks.IRON_ORE, 16));
		tile.setInventorySlotContents(1, new ItemStack(Items.COAL, 4));
		//?} else if >=1.8 {
		/*BlockPos pos = new BlockPos(x, y, z);
		world.setBlockState(pos, Blocks.furnace.getDefaultState());
		TileEntityFurnace tile = (TileEntityFurnace) world.getTileEntity(pos);
		tile.setInventorySlotContents(0, new ItemStack(Blocks.iron_ore, 16));
		tile.setInventorySlotContents(1, new ItemStack(Items.coal, 4));
		*///?} else {
		/*world.setBlock(x, y, z, Blocks.furnace);
		TileEntityFurnace tile = (TileEntityFurnace) world.getTileEntity(x, y, z);
		tile.setInventorySlotContents(0, new ItemStack(Blocks.iron_ore, 16));
		tile.setInventorySlotContents(1, new ItemStack(Items.coal, 4));
		*///?}
		furnace = new int[] {x, y, z};
		// With no player online the world stops ticking block entities after a while unless a chunk is force-loaded:
		// keep the furnace's chunk loaded until the next run reads it
		ticket = ForgeChunkManager.requestTicket(mod, world, ForgeChunkManager.Type.NORMAL);
		//? if >=1.9.4 {
		if (ticket != null) ForgeChunkManager.forceChunk(ticket, new ChunkPos(x >> 4, z >> 4));
		//?} else {
		/*if (ticket != null) ForgeChunkManager.forceChunk(ticket, new ChunkCoordIntPair(x >> 4, z >> 4));
		*///?}
		return 0;
	}
}
