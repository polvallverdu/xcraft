package dev.polv.xcraft.forge.ancient;

import dev.polv.xcraft.forge.Commands;
import dev.polv.xcraft.forge.TestCapture;
import dev.polv.xcraft.forge.Xcraft;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.ForgeChunkManager;
//? if <1.4.5 {
/*import net.minecraft.src.*;
*///?} else {
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemInWorldManager;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.DamageSource;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
//?}

import java.util.List;

/**
 * {@code /xcraft test} (only with -Dxcraft.debug=true): vanilla before 1.7 has no /setblock or /summon, so the CI
 * server test can't build its scenarios over RCON. Next to spawn this kills a cow, harvests a coal ore as a
 * stand-in player and has that player pick up an XP orb, all through the game's own code, and places a furnace
 * with iron ore and coal that the world then ticks: the next run reports how much it smelted.
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
			World world = MinecraftServer.getServer().worldServers[0];
			ChunkCoordinates spawn = world.getSpawnPoint();
			int x = spawn.posX, z = spawn.posZ;
			int y = world.getTopSolidOrLiquidBlock(x, z) + 1;
			int smelted = furnace(world, x + 3, y, z);

			TestCapture.begin();
			EntityCow cow = new EntityCow(world);
			cow.setLocationAndAngles(x + 0.5, y, z + 0.5, 0, 0);
			world.spawnEntityInWorld(cow);
			cow.attackEntityFrom(DamageSource.outOfWorld, 1000);
			int[] mob = TestCapture.end();

			// The call ItemInWorldManager makes once a player has broken a block
			TestPlayer player = new TestPlayer(world);
			TestCapture.begin();
			Block.oreCoal.harvestBlock(world, player, x, y, z - 3, 0);
			int[] ore = TestCapture.end();

			int before = player.experienceTotal;
			EntityXPOrb orb = new EntityXPOrb(world, x + 0.5, y, z + 0.5, XP_ORB);
			world.spawnEntityInWorld(orb);
			orb.field_70532_c = 0;
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
	private int furnace(World world, int x, int y, int z) {
		if (furnace != null) {
			TileEntity tile = world.getBlockTileEntity(furnace[0], furnace[1], furnace[2]);
			if (ticket != null) ForgeChunkManager.releaseTicket(ticket);
			ticket = null;
			if (tile instanceof TileEntityFurnace) {
				ItemStack output = ((TileEntityFurnace) tile).getStackInSlot(2);
				return output == null ? 0 : output.stackSize;
			}
		}
		//? if >=1.5.1 {
		world.setBlock(x, y, z, Block.furnaceIdle.blockID);
		//?} else if >=1.5 {
		/*world.setBlockAndMetadataWithNotify(x, y, z, Block.furnaceIdle.blockID, 0, 3);
		*///?} else {
		/*world.setBlockWithNotify(x, y, z, Block.stoneOvenIdle.blockID);
		*///?}
		TileEntityFurnace tile = (TileEntityFurnace) world.getBlockTileEntity(x, y, z);
		tile.setInventorySlotContents(0, new ItemStack(Block.oreIron, 16));
		tile.setInventorySlotContents(1, new ItemStack(Item.coal, 4));
		furnace = new int[] {x, y, z};
		// With no player online the world stops ticking block entities (after 3 s on 1.3, 60 s since) unless a
		// chunk is force-loaded: keep the furnace's chunk loaded until the next run reads it
		ticket = ForgeChunkManager.requestTicket(mod, world, ForgeChunkManager.Type.NORMAL);
		if (ticket != null) ForgeChunkManager.forceChunk(ticket, new ChunkCoordIntPair(x >> 4, z >> 4));
		return 0;
	}

	/** A player that is never added to the world; only passed to the harvest and XP pickup code. */
	private static final class TestPlayer extends EntityPlayerMP {
		TestPlayer(World world) {
			super(MinecraftServer.getServer(), world, "xcraft-test", new ItemInWorldManager(world));
		}

		// The real one sends a packet to the (missing) client
		@Override
		public void addStat(StatBase stat, int amount) {
		}
	}
}
