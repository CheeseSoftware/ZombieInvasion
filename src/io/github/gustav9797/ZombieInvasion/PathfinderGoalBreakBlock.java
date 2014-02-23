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
	private Arena arena;
	private int j = -1;
	protected Block block;
	protected static List<Vector> possiblePositions = new ArrayList<Vector>(Arrays.asList(new Vector(-1, 0, 0), new Vector(-1, 1, 0), new Vector(1, 0, 0), new Vector(1, 1, 0), new Vector(0, 0, -1),
			new Vector(0, 1, -1), new Vector(0, 0, 1), new Vector(0, 1, 1), new Vector(0, 2, 0), new Vector(0, -1, 0)));
	protected static List<Material> nonBreakableMaterials = new ArrayList<Material>(Arrays.asList(Material.WOOD_STAIRS, Material.COBBLESTONE_STAIRS, Material.SANDSTONE_STAIRS, Material.BRICK_STAIRS,
			Material.SMOOTH_STAIRS, Material.BEDROCK, Material.WATER, Material.GRASS));
	protected static List<Material> naturalMaterials = new ArrayList<Material>(Arrays.asList(Material.GRASS, Material.DIRT, Material.LEAVES));
	protected static List<Material> priorityMaterials = new ArrayList<Material>(Arrays.asList(Material.WOOD_DOOR, Material.IRON_DOOR, Material.TRAP_DOOR, Material.CHEST, Material.THIN_GLASS,
			Material.STAINED_GLASS, Material.STAINED_GLASS_PANE, Material.GLASS, Material.TORCH, Material.WOOL));

	private Location oldLocation = null;

	public PathfinderGoalBreakBlock(EntityInsentient entity, Arena arena)
	{
		super(entity);
		this.arena = arena;
	}

	public boolean a() // canExecute
	{
		if (this.entity instanceof EntityMonster)
		{
			EntityMonster monster = (EntityMonster) this.entity;
			if (monster.getGoalTarget() == null || !monster.getGoalTarget().isAlive())
				return false;
			return super.a();
		}
		return false;
	}

	public void c() // setup
	{
		oldLocation = entity.getBukkitEntity().getLocation();
		super.c();
		this.i = 0;
	}

	public boolean b() // canContinue
	{
		if (this.entity instanceof EntityMonster)
		{
			EntityMonster monster = (EntityMonster) this.entity;
			if (monster.getGoalTarget() == null || !monster.getGoalTarget().isAlive())
				return false;
			return true;
		}
		return false;
	}

	public void d() // finish
	{
		super.d();
	}

	public void e() // move
	{
		super.e();
		Location currentLocation = this.entity.getBukkitEntity().getLocation();
		if (currentLocation.getBlockX() == oldLocation.getBlockX() && currentLocation.getBlockY() == oldLocation.getBlockY() && currentLocation.getBlockZ() == oldLocation.getBlockZ())
		{
			Entity entity = this.entity.getBukkitEntity();
			if (this.entity instanceof EntityMonster)
			{
				EntityMonster monster = (EntityMonster) this.entity;
				if (monster.getGoalTarget() == null || !monster.getGoalTarget().isAlive())
					return;
			}

			if (block == null || block.getType() == Material.AIR || getDistanceBetween(block.getLocation(), entity.getLocation()) > 3.5)
			{
				if (block != null)
					this.entity.world.d(this.entity.getId(), block.getX(), block.getY(), block.getZ(), 0);
				block = getRandomCloseBlock();
				if (block == null)
					return;
				this.i = 0;
			}

			if (block != null && block.getType() != Material.AIR)
			{
				if (this.entity.aI().nextInt(300) == 0)
				{
					this.entity.world.triggerEffect(1010, block.getX(), block.getY(), block.getZ(), 0);
				}

				this.i += 2;
				int i = (int) ((float) this.i / 240.0F * 10.0F);

				if (i != this.j)
				{
					this.entity.world.d(this.entity.getId(), block.getX(), block.getY(), block.getZ(), i);
					this.j = i;
				}

				if (this.i >= 240)
				{
					this.i = 0;
					Bukkit.getPluginManager().callEvent(new LeavesDecayEvent(block));
					block.setType(Material.AIR);
					this.entity.world.triggerEffect(1012, block.getX(), block.getY(), block.getZ(), 0);
					this.entity.world.triggerEffect(2001, block.getX(), block.getY(), block.getZ(),
							net.minecraft.server.v1_7_R1.Block.b(this.entity.world.getType(block.getX(), block.getY(), block.getZ())));
					block = null;
				}
			}
		}
		oldLocation = currentLocation;
	}

	private Block getRandomCloseBlock()
	{
		if (this.entity instanceof EntityMonster)
		{
			EntityMonster zombie = (EntityMonster) this.entity;
			if (zombie.target != null && zombie.target.isAlive())
			{
				Set<Block> blocks = this.getCloseBlocks();
				Location monsterLocation = zombie.getBukkitEntity().getLocation();
				Location targetLocation = zombie.target.getBukkitEntity().getLocation();
				if (targetLocation.getBlockY() > monsterLocation.getBlockY())
				{
					Location above = this.entity.getBukkitEntity().getLocation();
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
						Block above = this.entity.getBukkitEntity().getWorld().getBlockAt(l);
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
		Location a = this.entity.getBukkitEntity().getLocation();
		for (Vector vector : possiblePositions)
		{
			Location finalLocation = new Location(a.getWorld(), a.getBlockX() + vector.getBlockX(), a.getBlockY() + vector.getBlockY(), a.getBlockZ() + vector.getBlockZ());
			if (arena == null || (arena != null && !arena.isBorder(finalLocation.toVector())))
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
