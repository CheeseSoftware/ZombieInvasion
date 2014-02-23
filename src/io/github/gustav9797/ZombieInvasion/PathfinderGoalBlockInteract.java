package io.github.gustav9797.ZombieInvasion;

import java.util.Random;

import net.minecraft.server.v1_7_R1.EntityInsentient;
import net.minecraft.server.v1_7_R1.PathfinderGoal;

public abstract class PathfinderGoalBlockInteract extends PathfinderGoal
{

	protected EntityInsentient entity;
	protected int x;
	protected int y;
	protected int z;
	boolean f;
	float g;
	float h;
	Random r = new Random();

	public PathfinderGoalBlockInteract(EntityInsentient entity)
	{
		this.entity = entity;
	}

	public boolean a() //canExecute
	{
		return true;
	}

	public boolean b() //canContinue
	{
		return !this.f;
	}

	public void c() //setup
	{
		this.f = false;
		this.g = (float) ((double) ((float) this.x + 0.5F) - this.entity.locX);
		this.h = (float) ((double) ((float) this.z + 0.5F) - this.entity.locZ);
	}

	public void e() //move
	{
		float f = (float) ((double) ((float) this.x + 0.5F) - this.entity.locX);
		float f1 = (float) ((double) ((float) this.z + 0.5F) - this.entity.locZ);
		float f2 = this.g * f + this.h * f1;
		if (f2 < 0.0F)
		{
			this.f = true;
		}
	}
}