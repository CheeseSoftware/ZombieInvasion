package io.github.gustav9797.ZombieInvasion;

import java.lang.reflect.Field;

import net.minecraft.server.v1_7_R1.EntityHuman;
import net.minecraft.server.v1_7_R1.EntityVillager;
import net.minecraft.server.v1_7_R1.EntityZombie;
import net.minecraft.server.v1_7_R1.PathfinderGoal;
import net.minecraft.server.v1_7_R1.PathfinderGoalFloat;
import net.minecraft.server.v1_7_R1.PathfinderGoalHurtByTarget;
import net.minecraft.server.v1_7_R1.PathfinderGoalLookAtPlayer;
import net.minecraft.server.v1_7_R1.PathfinderGoalMeleeAttack;
import net.minecraft.server.v1_7_R1.PathfinderGoalMoveTowardsRestriction;
import net.minecraft.server.v1_7_R1.PathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_7_R1.PathfinderGoalRandomLookaround;
import net.minecraft.server.v1_7_R1.PathfinderGoalRandomStroll;
import net.minecraft.server.v1_7_R1.PathfinderGoalSelector;
import net.minecraft.server.v1_7_R1.World;

import org.bukkit.craftbukkit.v1_7_R1.util.UnsafeList;

public class EntityFastZombie extends EntityZombie
{
	private boolean bu = false;
	private final PathfinderGoalBreakBlock bs = new PathfinderGoalBreakBlock(this);

	@SuppressWarnings("rawtypes")
	public EntityFastZombie(World world)
	{
		super(world);

		try
		{

			Field gsa = PathfinderGoalSelector.class.getDeclaredField("b");
			gsa.setAccessible(true);

			gsa.set(this.goalSelector, new UnsafeList());
			gsa.set(this.targetSelector, new UnsafeList());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		this.getNavigation().b(true);
		this.goalSelector.a(0, new PathfinderGoalFloat(this));
		this.goalSelector.a(2, new PathfinderGoalMeleeAttack(this, EntityHuman.class, 1.0D, false));
		this.goalSelector.a(4, new PathfinderGoalMeleeAttack(this, EntityVillager.class, 1.0D, true));
		this.goalSelector.a(5, new PathfinderGoalMoveTowardsRestriction(this, 1.0D));
		// this.goalSelector.a(6, new PathfinderGoalMoveThroughVillage(this,
		// 1.0D, false));
		this.goalSelector.a(7, new PathfinderGoalRandomStroll(this, 1.0D));
		this.goalSelector.a(8, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
		this.goalSelector.a(8, new PathfinderGoalRandomLookaround(this));
		this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this, true));
		this.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, 0, true));
		this.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget(this, EntityVillager.class, 0, false));
		// this.targetSelector.a(3, new PathfinderGoalBreakBlock(this));
		this.goalSelector.a(1, new PathfinderGoalBreakBlock(this));
		//this.goalSelector.a((PathfinderGoal) this.bs);
		this.a(0.6F, 1.8F);
	}

	@Override
	public void a(boolean flag)
	{
		if (this.bu != flag)
		{
			this.bu = flag;
			if (flag)
			{
				this.goalSelector.a(1, this.bs);
			}
			else
			{
				this.goalSelector.a((PathfinderGoal) this.bs);
			}
		}
	}

}
