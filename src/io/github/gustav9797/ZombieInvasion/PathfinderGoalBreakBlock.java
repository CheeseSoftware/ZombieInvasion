package io.github.gustav9797.ZombieInvasion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.server.v1_7_R1.EntityInsentient;
import net.minecraft.server.v1_7_R1.EntityMonster;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.util.Vector;

public class PathfinderGoalBreakBlock extends PathfinderGoalBlockInteract
{
	private int i;
	private int j = -1;
	// private Location l = null;
	protected Block block;
	protected static List<Vector> possiblePositions = new ArrayList<Vector>(Arrays.asList(new Vector(-1, 0, 0), new Vector(-1, 1, 0), new Vector(1, 0, 0), new Vector(1, 1, 0), new Vector(0, 0, -1),
			new Vector(0, 1, -1), new Vector(0, 0, 1), new Vector(0, 1, 1), new Vector(0, 2, 0), new Vector(0, -1, 0)));

	protected static List<Material> nonBreakableMaterials = new ArrayList<Material>(Arrays.asList(Material.WOOD_STAIRS, Material.COBBLESTONE_STAIRS, Material.SANDSTONE_STAIRS, Material.BRICK_STAIRS,
			Material.SMOOTH_STAIRS, Material.BEDROCK, Material.WATER, Material.GRASS));
	protected static List<Material> naturalMaterials = new ArrayList<Material>(Arrays.asList(Material.GRASS, Material.DIRT, Material.LEAVES));
	protected static List<Material> priorityMaterials = new ArrayList<Material>(Arrays.asList(Material.WOOD_DOOR, Material.IRON_DOOR, Material.TRAP_DOOR, Material.CHEST, Material.THIN_GLASS,
			Material.STAINED_GLASS, Material.STAINED_GLASS_PANE, Material.GLASS, Material.TORCH, Material.WOOL));

	private Location oldLocation = null;

	public PathfinderGoalBreakBlock(EntityInsentient entityinsentient)
	{
		super(entityinsentient);
	}

	public boolean a() // canExecute
	{
		if (this.entityInsentient instanceof EntityMonster)
		{
			EntityMonster monster = (EntityMonster) this.entityInsentient;
			if (monster.getGoalTarget() == null || !monster.getGoalTarget().isAlive())
				return false;
			return super.a();
		}
		return false;
	}

	public void c() // setup
	{
		oldLocation = entityInsentient.getBukkitEntity().getLocation();
		super.c();
		this.i = 0;
	}

	public boolean b() // canContinue
	{
		if (this.entityInsentient instanceof EntityMonster)
		{
			EntityMonster monster = (EntityMonster) this.entityInsentient;
			if (monster.getGoalTarget() == null || !monster.getGoalTarget().isAlive())
				return false;
			// double d0 = this.a.e((double) this.b, (double) this.c, (double)
			return true;// return this.i <= 240/* && !this.e.f(this.a.world,
						// this.b,
		}
		return false;
		// this.c, this.d)*/ && d0 < 4.0D;
	}

	public void d() // finish
	{
		super.d();
		// this.entityInsentient.world.d(this.entityInsentient.getId(), this.x,
		// this.y, this.z, -1);
	}

	public void e() // move
	{
		super.e();
		Location currentLocation = this.entityInsentient.getBukkitEntity().getLocation();
		if (currentLocation.getBlockX() == oldLocation.getBlockX() && currentLocation.getBlockY() == oldLocation.getBlockY() && currentLocation.getBlockZ() == oldLocation.getBlockZ())
		{
			Entity entity = this.entityInsentient.getBukkitEntity();
			if (this.entityInsentient instanceof EntityMonster)
			{
				EntityMonster monster = (EntityMonster) this.entityInsentient;
				if (monster.getGoalTarget() == null || !monster.getGoalTarget().isAlive())
					return;
			}

			if (block == null || block.getType() == Material.AIR || getDistanceBetween(block.getLocation(), entity.getLocation()) > 3.5)
			{
				if (block != null)
					this.entityInsentient.world.d(this.entityInsentient.getId(), block.getX(), block.getY(), block.getZ(), 0);
				block = getRandomCloseBlock();
				if (block == null)
					return;
				this.i = 0;
			}

			if (block != null && block.getType() != Material.AIR)
			{
				if (this.entityInsentient.aI().nextInt(300) == 0)
				{
					this.entityInsentient.world.triggerEffect(1010, block.getX(), block.getY(), block.getZ(), 0);
				}

				this.i += 2;
				int i = (int) ((float) this.i / 240.0F * 10.0F);

				if (i != this.j)
				{
					this.entityInsentient.world.d(this.entityInsentient.getId(), block.getX(), block.getY(), block.getZ(), i);
					this.j = i;
				}

				if (this.i >= 240)
				{
					this.i = 0;
					Bukkit.getPluginManager().callEvent(new LeavesDecayEvent(block));
					block.breakNaturally();
					this.entityInsentient.world.triggerEffect(1012, block.getX(), block.getY(), block.getZ(), 0);
					this.entityInsentient.world.triggerEffect(2001, block.getX(), block.getY(), block.getZ(),
							net.minecraft.server.v1_7_R1.Block.b(this.entityInsentient.world.getType(block.getX(), block.getY(), block.getZ())));
					block = null;
				}
			}
		}
		oldLocation = currentLocation;
	}

	private Block getRandomCloseBlock()
	{
		if (this.entityInsentient instanceof EntityMonster)
		{
			EntityMonster zombie = (EntityMonster) this.entityInsentient;
			if (zombie.target != null && zombie.target.isAlive())
			{
				Set<Block> blocks = this.getCloseBlocks();
				Location monsterLocation = zombie.getBukkitEntity().getLocation();
				Location targetLocation = zombie.target.getBukkitEntity().getLocation();
				/*
				 * if (targetLocation.getBlockY() < monsterLocation.getBlockY()
				 * - 1) { Location below =
				 * this.entityInsentient.getBukkitEntity().getLocation();
				 * below.setY(below.getY() - 1); blocks.add(below.getBlock()); }
				 * else
				 */
				if (targetLocation.getBlockY() > monsterLocation.getBlockY())
				{
					Location above = this.entityInsentient.getBukkitEntity().getLocation();
					above.setY(above.getY() + 1);
					blocks.add(above.getBlock());
				}

				Set<Block> innaturalBlocks = new HashSet<Block>();
				Set<Block> priorityBlocks = new HashSet<Block>();
				Set<Block> hatedBlocks = new HashSet<Block>();
				for (Block block : blocks)
				{
					if (!nonBreakableMaterials.contains(block.getType()))
					{
						Location l = block.getLocation();
						l.setY(l.getY() + 1);
						Block above = this.entityInsentient.getBukkitEntity().getWorld().getBlockAt(l);
						if (above == null || (!priorityMaterials.contains(block.getType()) && above.getType() == Material.AIR))
							hatedBlocks.add(block);
						else if (priorityBlocks.contains(block.getType()))
							priorityBlocks.add(block);
						else if (!naturalMaterials.contains(block.getType()))
							innaturalBlocks.add(block);
					}
				}

				if (priorityBlocks.size() > 0)
					return (Block) priorityBlocks.toArray()[r.nextInt(priorityBlocks.size())];
				else if (innaturalBlocks.size() > 0)
					return (Block) innaturalBlocks.toArray()[r.nextInt(innaturalBlocks.size())];
				else if (hatedBlocks.size() > 0 && blocks.size() <= hatedBlocks.size())
				{
					int i = r.nextInt(400);
					if (i == 0)
					{
						return (Block) hatedBlocks.toArray()[r.nextInt(hatedBlocks.size())];
					}
				}
				else if (blocks.size() > 0)
					return (Block) blocks.toArray()[r.nextInt(blocks.size())];
			}
		}
		return null;
	}

	private Set<Block> getCloseBlocks()
	{
		Set<Block> blocks = new HashSet<Block>();
		Location a = this.entityInsentient.getBukkitEntity().getLocation();
		for (Vector vector : possiblePositions)
		{
			Location finalLocation = new Location(a.getWorld(), a.getBlockX() + vector.getBlockX(), a.getBlockY() + vector.getBlockY(), a.getBlockZ() + vector.getBlockZ());
			blocks.add(finalLocation.getBlock());
		}
		return (Set<Block>) blocks;
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
