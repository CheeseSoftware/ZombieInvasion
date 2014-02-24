package io.github.gustav9797.ZombieInvasion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.LeavesDecayEvent;

import net.minecraft.server.v1_7_R1.EntityInsentient;
import net.minecraft.server.v1_7_R1.PathfinderGoal;

public class PathfinderGoalFindBreakBlock extends PathfinderGoal
{
	EntityInsentient entity;
	Arena arena;
	Location oldLocation = null;
	int findRadius = 10;
	int findHeight = 2;
	int ticksStoodStill = 0;
	boolean isBreaking = false;
	boolean isWalking = false;
	Block currentBlock = null;
	int currentBlockDamage = 0;
	int ticksPassed = 0;
	boolean blockBroken = false;
	boolean couldNotWalkToBlock = false;
	List<Block> blocksNotWalkable = new ArrayList<Block>();
	Random r = new Random();
	
	@SuppressWarnings("deprecation")
	private static List<Material> nonBreakableMaterials = new ArrayList<Material>(Arrays.asList(Material.BEDROCK, Material.getMaterial(8), Material.getMaterial(9), Material.GRASS, Material.SAND, Material.AIR));
	private static List<Material> naturalMaterials = new ArrayList<Material>(Arrays.asList(Material.GRASS, Material.DIRT, Material.LEAVES));
	private static List<Material> priorityMaterials = new ArrayList<Material>(Arrays.asList(Material.WOOD_DOOR, Material.IRON_DOOR, Material.TRAP_DOOR, Material.CHEST, Material.THIN_GLASS,
			Material.STAINED_GLASS, Material.STAINED_GLASS_PANE, Material.GLASS, Material.TORCH, Material.WOOL));

	public PathfinderGoalFindBreakBlock(EntityInsentient entity, Arena arena)
	{
		this.entity = entity;
		this.arena = arena;
	}

	@Override
	public boolean a() // canExecute
	{
		this.ticksPassed++;
		if (this.ticksPassed < 5)
			return false;
		return !this.isBreaking && this.CanFindABlock();
	}

	@Override
	public void c() // setup
	{
		super.c();
		if (CanFindABlock())
		{
			while (currentBlock == null)
			{
				currentBlock = getRandomCloseBlock();
				currentBlockDamage = 0;
				ticksStoodStill = 0;
				isBreaking = true;
				this.couldNotWalkToBlock = false;
			}
			Location temp = new Location(this.entity.getBukkitEntity().getWorld(), currentBlock.getX() + 0.5F, currentBlock.getY() + 0.5F, currentBlock.getZ() + 0.5F);

			boolean foundPath = this.entity.getNavigation().a(temp.getBlockX(), temp.getBlockY(), temp.getBlockZ(), 1);
			if (foundPath)
			{
				this.isWalking = true;
			}
		}
	}

	@Override
	public boolean b() // canContinue
	{
		return !this.couldNotWalkToBlock && (this.isBreaking || this.isWalking || this.CanFindABlock());
	}

	@Override
	public void d() // finish
	{
		super.d();
	}

	@Override
	public void e() // move
	{
		Location currentLocation = this.entity.getBukkitEntity().getLocation();
		if (isWalking)
		{
			if (this.oldLocation == null)
			{
				this.oldLocation = currentLocation;
				return;
			}
			if (currentLocation.getBlockX() == oldLocation.getBlockX() && currentLocation.getBlockY() == oldLocation.getBlockY() && currentLocation.getBlockZ() == oldLocation.getBlockZ())
			{
				this.ticksStoodStill++;
				if (this.ticksStoodStill > 30)
				{
					if (currentBlock == null || this.getDistanceBetween(this.currentBlock.getLocation(), currentLocation) < 2)
						this.isWalking = false;
					else
					{
						this.couldNotWalkToBlock = true;
						this.isBreaking = false;
						currentBlockDamage = 0;
						ticksStoodStill = 0;
						this.blocksNotWalkable.add(currentBlock);
						this.currentBlock = null;
						return;
					}
					this.ticksStoodStill = 0;
				}
			}
		}
		if (!isWalking)
		{
			if (currentBlock == null && !isBreaking && CanFindABlock())
			{
				while (currentBlock == null)
				{
					currentBlock = getRandomCloseBlock();
					currentBlockDamage = 0;
					ticksStoodStill = 0;
					isBreaking = true;
					blockBroken = false;
				}
				Location temp = new Location(this.entity.getBukkitEntity().getWorld(), currentBlock.getX() + 0.5F, currentBlock.getY() + 0.5F, currentBlock.getZ() + 0.5F);

				boolean foundPath = this.entity.getNavigation().a(temp.getBlockX(), temp.getBlockY(), temp.getBlockZ(), 1);
				if (foundPath)
				{
					this.isWalking = true;
				}
			}

			if (currentBlock != null && currentBlock.getType() != Material.AIR)
			{
				currentBlockDamage += 5;
				if (this.entity.aI().nextInt(300) == 0)
					this.entity.world.triggerEffect(1010, currentBlock.getX(), currentBlock.getY(), currentBlock.getZ(), 0);
				int i = (int) ((float) this.currentBlockDamage / 240.0F * 10.0F);
				this.entity.world.d(entity.getId(), currentBlock.getX(), currentBlock.getY(), currentBlock.getZ(), i);
				if (currentBlockDamage >= 240)
				{
					currentBlock.setType(Material.AIR);
					entity.world.getWorld().playEffect(currentBlock.getLocation(), Effect.ZOMBIE_DESTROY_DOOR, 1);
					Bukkit.getPluginManager().callEvent(new LeavesDecayEvent(this.currentBlock));

					this.blocksNotWalkable.clear();
					currentBlock = null;
					isBreaking = false;
					this.blockBroken = true;
					currentBlockDamage = 0;
				}
			}
			else
			{
				currentBlock = null;
				isBreaking = false;
				this.blockBroken = true;
				currentBlockDamage = 0;
			}
		}
		this.oldLocation = currentLocation;
	}

	public boolean CanFindABlock()
	{
		List<Block> blocks = this.getCloseBlocks();
		for (Block block : blocks)
		{
			if (!nonBreakableMaterials.contains(block.getType()))
				return true;
		}
		return false;
	}

	public List<Block> getCloseBlocks()
	{
		Location loc = this.entity.getBukkitEntity().getLocation();
		List<Block> blocks = new ArrayList<Block>();
		for (int y = loc.getBlockY() + findHeight + 1; y >= loc.getBlockY(); y--)
		{
			for (int x = loc.getBlockX() - findRadius; x < loc.getBlockX() + findRadius; x++)
			{
				for (int z = loc.getBlockZ() - findRadius; z < loc.getBlockZ() + findRadius; z++)
				{
					Block block = this.entity.getBukkitEntity().getWorld().getBlockAt(x, y, z);
					if (!nonBreakableMaterials.contains(block.getType()) && !this.blocksNotWalkable.contains(block))
					{
						if (arena == null || (arena != null && !arena.isBorder(block.getLocation().toVector())))
							blocks.add(block);
					}
				}
			}
		}
		return blocks;
	}

	public Block getRandomCloseBlock()
	{
		List<Block> blocks = this.getCloseBlocks();
		List<Block> highPriorityBlocks = new ArrayList<Block>();
		List<Block> normalPriorityBlocks = new ArrayList<Block>();
		List<Block> lowPriorityBlocks = new ArrayList<Block>();
		for (Block block : blocks)
		{
			if (!nonBreakableMaterials.contains(block.getType()) && (arena == null || arena.ContainsLocation(block.getLocation())))
			{
				if (priorityMaterials.contains(block.getType()))
					highPriorityBlocks.add(block);
				else if (naturalMaterials.contains(block.getType()))
					lowPriorityBlocks.add(block);
				else
					normalPriorityBlocks.add(block);
			}
		}

		List<Block> listToUse = null;
		if (highPriorityBlocks.size() > 0)
			listToUse = highPriorityBlocks;
		else if (normalPriorityBlocks.size() > 0)
			listToUse = normalPriorityBlocks;
		else if (lowPriorityBlocks.size() > 0)
			listToUse = lowPriorityBlocks;
		if (listToUse != null)
		{
			Block closest = null;
			double closestDistance = Integer.MAX_VALUE;
			for (Block block : listToUse)
			{
				double distance = getDistanceBetween(block.getLocation(), this.entity.getBukkitEntity().getLocation());
				if (closest == null || distance < closestDistance)
				{
					closestDistance = distance;
					closest = block;
				}
			}
			return closest;
		}
		return null;
	}

	private double getDistanceBetween(Location a, Location b)
	{
		int xd = b.getBlockX() - a.getBlockX();
		int yd = b.getBlockY() - a.getBlockY();
		int zd = b.getBlockZ() - a.getBlockZ();
		double distance = Math.sqrt(xd * xd + yd * yd + zd * zd);
		return distance;
	}
}
