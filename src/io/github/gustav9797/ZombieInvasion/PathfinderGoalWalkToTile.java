package io.github.gustav9797.ZombieInvasion;

import org.bukkit.Location;

import net.minecraft.server.v1_7_R1.EntityCreature;
import net.minecraft.server.v1_7_R1.PathfinderGoal;

public class PathfinderGoalWalkToTile extends PathfinderGoal
{
	float speed;
	private EntityCreature entityCreature;
	private Location goal;

	public PathfinderGoalWalkToTile(EntityCreature entitycreature, float speed, Location location)
	{
		this.speed = speed;
		this.entityCreature = entitycreature;
		this.goal = location;
	}

	@Override
	public boolean a()
	{
		if(entityCreature.target == null || !entityCreature.target.isAlive())
			this.entityCreature.getNavigation().a(goal.getBlockX(), goal.getBlockY(), goal.getBlockZ(), speed);
		return false;
	}

	@Override
	public void c()
	{
		this.entityCreature.getNavigation().a(goal.getBlockX(), goal.getBlockY(), goal.getBlockZ(), speed);
	}

}