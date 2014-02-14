package io.github.gustav9797.ZombieInvasion;

import java.util.Arrays;
import java.util.LinkedList;

import net.minecraft.server.v1_7_R1.Block;
import net.minecraft.server.v1_7_R1.EntityInsentient;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.event.block.LeavesDecayEvent;

public class PathfinderGoalBreakBlock extends PathfinderGoalBlockInteract
{
	private int i;
	private int j = -1;
	private Location l = null;
	protected static LinkedList<Location> possibleLocations = new LinkedList<Location>(Arrays.asList(new Location(null, -1, 0, 0), new Location(null, -1, 1, 0), new Location(null, 1, 0, 0),
			new Location(null, 1, 1, 0), new Location(null, 0, 0, -1), new Location(null, 0, 1, -1), new Location(null, 0, 0, 1), new Location(null, 0, 1, 1), new Location(null, 0, 2, 0),
			new Location(null, 0, -1, 0)));

	public PathfinderGoalBreakBlock(EntityInsentient entityinsentient)
	{
		super(entityinsentient);
	}

	public boolean a()
	{
		// Bukkit.getServer().broadcastMessage("a was called");
		return super.a();// (!this.a.world.getGameRules().getBoolean("mobGriefing")
							// ? false : !this.e.f(this.a.world,
		// this.b, this.c, this.d));
	}

	public void c()
	{
		// Bukkit.getServer().broadcastMessage("c was called");
		super.c();
		this.i = 0;
	}

	public boolean b()
	{
		// Bukkit.getServer().broadcastMessage("b was called");
		// double d0 = this.a.e((double) this.b, (double) this.c, (double)
		// this.d);
		return true;// return this.i <= 240/* && !this.e.f(this.a.world, this.b,
					// this.c, this.d)*/ && d0 < 4.0D;
	}

	public void d()
	{
		// Bukkit.getServer().broadcastMessage("d was called");
		super.d();
		this.entityInsentient.world.d(this.entityInsentient.getId(), this.x, this.y, this.z, -1);
	}

	private Location getRandomCloseBlock()
	{
		//Entity entity = this.entityInsentient.getBukkitEntity();
		if (this.entityInsentient instanceof EntityFastZombie)
		{
			EntityFastZombie zombie = (EntityFastZombie) this.entityInsentient;
			if (zombie.target != null && zombie.target.isAlive())
			{
				int i = 0;
				if (zombie.target.getBukkitEntity().getLocation().getBlockY() < this.entityInsentient.getBukkitEntity().getLocation().getBlockY())
					i = r.nextInt(10);
				else
					i = r.nextInt(9);
				int x_ = possibleLocations.get(i).getBlockX();
				int y_ = possibleLocations.get(i).getBlockY();
				int z_ = possibleLocations.get(i).getBlockZ();

				int x = this.entityInsentient.getBukkitEntity().getLocation().getBlockX() + x_;
				int y = this.entityInsentient.getBukkitEntity().getLocation().getBlockY() + y_;
				int z = this.entityInsentient.getBukkitEntity().getLocation().getBlockZ() + z_;

				return new Location(null, x, y, z);
			}
		}
		return new Location(null, 0, 0, 0);
	}

	private double getDistanceBetween(int x1, int y1, int z1, int x2, int y2, int z2)
	{
		int xd = x2 - x1;
		int yd = y2 - y1;
		int zd = z2 - z1;
		double distance = Math.sqrt(xd * xd + yd * yd + zd * zd);
		return distance;
	}

	public void e()
	{
		// Bukkit.getServer().broadcastMessage("e was called");
		super.e();
		Entity entity = this.entityInsentient.getBukkitEntity();
		if (this.entityInsentient instanceof EntityFastZombie)
		{
			EntityFastZombie z = (EntityFastZombie) this.entityInsentient;
			if (z.getGoalTarget() == null || !z.getGoalTarget().isAlive())
				return;
		}
		for (int ii = 0; ii < 18; ii++)
			if (super.block == null
					|| super.block.getType() == Material.AIR
					|| (l != null && getDistanceBetween(l.getBlockX(), l.getBlockY(), l.getBlockZ(), entity.getLocation().getBlockX(), entity.getLocation().getBlockY(), entity.getLocation()
							.getBlockZ()) > 3.5))
			{
				if (l != null)
					this.entityInsentient.world.d(this.entityInsentient.getId(), l.getBlockX(), l.getBlockY(), l.getBlockZ(), 0);
				l = getRandomCloseBlock();
				super.block = entity.getWorld().getBlockAt(l.getBlockX(), l.getBlockY(), l.getBlockZ());
				this.i = 0;
			}
			else
				break;

		if (super.block != null && l != null && super.block.getType() != Material.AIR)
		{
			if (this.entityInsentient.aI().nextInt(300) == 0)
			{
				this.entityInsentient.world.triggerEffect(1010, l.getBlockX(), l.getBlockY(), l.getBlockZ(), 0);
			}

			++this.i;
			int i = (int) ((float) this.i / 240.0F * 10.0F);

			if (i != this.j)
			{
				this.entityInsentient.world.d(this.entityInsentient.getId(), l.getBlockX(), l.getBlockY(), l.getBlockZ(), i);
				this.j = i;
			}

			if (this.i >= 240)
			{
				this.i = 0;
				Bukkit.getPluginManager().callEvent(new LeavesDecayEvent(entity.getWorld().getBlockAt(l))); 
				entity.getWorld().getBlockAt(l).breakNaturally();
				this.entityInsentient.world.triggerEffect(1012, l.getBlockX(), l.getBlockY(), l.getBlockZ(), 0);
				this.entityInsentient.world.triggerEffect(2001, l.getBlockX(), l.getBlockY(), l.getBlockZ(), Block.b(this.entityInsentient.world.getType(l.getBlockX(), l.getBlockY(), l.getBlockZ())));
				super.block = null;
				l = null;
			}
		}
	}
}
