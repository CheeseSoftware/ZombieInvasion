package io.github.gustav9797.ZombieInvasion;

import org.bukkit.craftbukkit.v1_6_R3.event.CraftEventFactory;
import net.minecraft.server.v1_6_R3.EntityInsentient;
import net.minecraft.server.v1_6_R3.PathfinderGoalBreakDoor;

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

	@Override
	public boolean b()
	{
		double d0 = this.a.e(this.b, this.c, this.d);	
		return ((this.i <= 240) && (!(this.e.b_(this.a.world, this.b, this.c, this.d))) && (d0 < 4.0D));
	}

	@Override
	public void d()
	{
		super.d();
		this.a.world.f(this.a.id, this.b, this.c, this.d, -1);
	}


	@Override
	public void e()
	{
		super.e();
		if (this.a.aD().nextInt(20) == 0)
		{
			this.a.world.triggerEffect(1010, this.b, this.c, this.d, 0);
		}
		this.i += 1;
		int i = (int) (this.i / 240.0F * 10.0F);
		
		if (i != this.j)
		{
			this.a.world.f(this.a.id, this.b, this.c, this.d, i);
			this.j = i;
		}

		if ((this.i != 240) || (this.a.world.difficulty != 3))
			return;
		if (CraftEventFactory.callEntityBreakDoorEvent(this.a, this.b, this.c, this.d).isCancelled())
		{
			e();
			return;
		}

		this.a.world.setAir(this.b, this.c, this.d);
		this.a.world.triggerEffect(1012, this.b, this.c, this.d, 0);
		this.a.world.triggerEffect(2001, this.b, this.c, this.d, this.e.id);
	}

}
