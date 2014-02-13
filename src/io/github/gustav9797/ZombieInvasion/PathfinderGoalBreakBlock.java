package io.github.gustav9797.ZombieInvasion;

import org.bukkit.craftbukkit.v1_7_R1.event.CraftEventFactory;

import net.minecraft.server.v1_7_R1.EntityInsentient;
import net.minecraft.server.v1_7_R1.PathfinderGoalBreakDoor;


public class PathfinderGoalBreakBlock extends PathfinderGoalBreakDoor
{

	private int i;
	private int j = -1;

	public PathfinderGoalBreakBlock(EntityInsentient entityinsentient)
	{
		super(entityinsentient);
	}

	@Override
	public boolean a()
	{
		return (super.a());
	}

	@Override
	public void c()
	{
		super.c();
		this.i = 0;
	}
}
