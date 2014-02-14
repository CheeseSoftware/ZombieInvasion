package io.github.gustav9797.ZombieInvasion;

import java.util.Random;

import org.bukkit.block.Block;

import net.minecraft.server.v1_7_R1.EntityInsentient;
import net.minecraft.server.v1_7_R1.MathHelper;
import net.minecraft.server.v1_7_R1.Navigation;
import net.minecraft.server.v1_7_R1.PathEntity;
import net.minecraft.server.v1_7_R1.PathPoint;
import net.minecraft.server.v1_7_R1.PathfinderGoal;

public abstract class PathfinderGoalBlockInteract extends PathfinderGoal
{

	protected EntityInsentient entityInsentient;
	protected int x;
	protected int y;
	protected int z;
	protected Block block;
	boolean f;
	float g;
	float h;
	Random r = new Random();

	public PathfinderGoalBlockInteract(EntityInsentient entityinsentient)
	{
		this.entityInsentient = entityinsentient;
	}

	public boolean a()
	{
		if (!this.entityInsentient.positionChanged)
		{
			return false;
		}
		else
		{
			// this.block = null;
			Navigation navigation = this.entityInsentient.getNavigation();
			PathEntity pathentity = navigation.e();

			if (pathentity != null && !pathentity.b() && navigation.c())
			{
				for (int i = 0; i < Math.min(pathentity.e() + 2, pathentity.d()); ++i)
				{
					PathPoint pathpoint = pathentity.a(i);

					this.x = pathpoint.a;
					this.y = pathpoint.b + 1;
					this.z = pathpoint.c;

					if (this.entityInsentient.e((double) this.x, this.entityInsentient.locY, (double) this.z) <= 2.25D)
					{
						this.block = this.getBlockAt(this.x, this.y, this.z);
						if (this.block != null)
						{
							return true;
						}
					}

				}

				this.x = MathHelper.floor(this.entityInsentient.locX);
				this.y = MathHelper.floor(this.entityInsentient.locY + 1.0D);
				this.z = MathHelper.floor(this.entityInsentient.locZ);
				// this.block = this.getBlockAt(this.x, this.y, this.z);
				// return this.block!= null;
				return true;
			}
			else
			{
				return false;
			}
		}
	}

	public boolean b()
	{
		return !this.f;
	}

	public void c()
	{
		this.f = false;
		this.g = (float) ((double) ((float) this.x + 0.5F) - this.entityInsentient.locX);
		this.h = (float) ((double) ((float) this.z + 0.5F) - this.entityInsentient.locZ);
	}

	public void e()
	{
		float f = (float) ((double) ((float) this.x + 0.5F) - this.entityInsentient.locX);
		float f1 = (float) ((double) ((float) this.z + 0.5F) - this.entityInsentient.locZ);
		float f2 = this.g * f + this.h * f1;

		if (f2 < 0.0F)
		{
			this.f = true;
		}
	}

	private Block getBlockAt(int i, int j, int k)
	{
		return this.entityInsentient.getBukkitEntity().getWorld().getBlockAt(i, j, k);
	}
}