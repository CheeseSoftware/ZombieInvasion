package io.github.gustav9797.ZombieInvasion;

import org.bukkit.Location;

import net.minecraft.server.v1_7_R1.EntityCreature;
import net.minecraft.server.v1_7_R1.PathfinderGoal;

public class PathfinderGoalWalkToTile extends PathfinderGoal
{
	float speed;
	private EntityCreature entityCreature;
	private Location goal;
	private int times = 10;

	public PathfinderGoalWalkToTile(EntityCreature entitycreature, float speed, Location location)
	{
		this.speed = speed;
		this.entityCreature = entitycreature;
		this.goal = location;
	}

	@Override
	public boolean a()
	{
		if (times > 0 && this.entityCreature.isAlive())
		{
			//this.entityCreature.getNavigation().a(goal.getBlockX(), goal.getBlockY(), goal.getBlockZ(), speed);
			times--;
			return true;
		}
		return false;
	}

	@Override
	public boolean b()
	{
		return !this.entityCreature.getNavigation().g();
	}

	@Override
	public void c()
	{
		this.entityCreature.getNavigation().a(goal.getBlockX(), goal.getBlockY(), goal.getBlockZ(), speed);
	}

}