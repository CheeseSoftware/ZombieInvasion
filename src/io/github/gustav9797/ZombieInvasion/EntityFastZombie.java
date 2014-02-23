package io.github.gustav9797.ZombieInvasion;

import java.lang.reflect.Field;
import java.util.Random;

import net.minecraft.server.v1_7_R1.AttributeInstance;
import net.minecraft.server.v1_7_R1.Entity;
import net.minecraft.server.v1_7_R1.EntityHuman;
import net.minecraft.server.v1_7_R1.EntityVillager;
import net.minecraft.server.v1_7_R1.EntityZombie;
import net.minecraft.server.v1_7_R1.Navigation;
import net.minecraft.server.v1_7_R1.PathfinderGoal;
import net.minecraft.server.v1_7_R1.PathfinderGoalFloat;
import net.minecraft.server.v1_7_R1.PathfinderGoalHurtByTarget;
import net.minecraft.server.v1_7_R1.PathfinderGoalLookAtPlayer;
import net.minecraft.server.v1_7_R1.PathfinderGoalMeleeAttack;
import net.minecraft.server.v1_7_R1.PathfinderGoalMoveTowardsRestriction;
import net.minecraft.server.v1_7_R1.PathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_7_R1.PathfinderGoalRandomLookaround;
import net.minecraft.server.v1_7_R1.PathfinderGoalSelector;
import net.minecraft.server.v1_7_R1.World;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_7_R1.util.UnsafeList;

public class EntityFastZombie extends EntityZombie
{
	private boolean bu = false;
	private final PathfinderGoalBreakBlock bs = new PathfinderGoalBreakBlock(this);
	private Random r = new Random();

	public EntityFastZombie(World world, Location middle)
	{
		super(world);

		try
		{
			Field field = Navigation.class.getDeclaredField("e");
			field.setAccessible(true);
			AttributeInstance e = (AttributeInstance) field.get(this.getNavigation());
			e.setValue(128); // Navigation distance in block lengths goes here
		}
		catch (Exception ex)
		{
		}

		try
		{
			Field gsa = PathfinderGoalSelector.class.getDeclaredField("b");
			gsa.setAccessible(true);
			gsa.set(this.goalSelector, new UnsafeList<Object>());
			gsa.set(this.targetSelector, new UnsafeList<Object>());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		this.getNavigation().b(true);
		this.goalSelector.a(0, new PathfinderGoalFloat(this));
		this.goalSelector.a(1, new PathfinderGoalBreakBlock(this));
		this.goalSelector.a(2, new PathfinderGoalMeleeAttack(this, EntityHuman.class, 1.0D, false));
		this.goalSelector.a(4, new PathfinderGoalMeleeAttack(this, EntityVillager.class, 1.0D, true));
		this.goalSelector.a(5, new PathfinderGoalMoveTowardsRestriction(this, 1.0D));
		this.goalSelector.a(8, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
		this.goalSelector.a(8, new PathfinderGoalRandomLookaround(this));
		this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this, true));
		this.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, 0, true));
		if(middle != null)
			this.targetSelector.a(0, new PathfinderGoalWalkToTile(this, 1.0F, middle));
		this.a(0.6F, 1.8F);
		
	}
	
	public EntityFastZombie(World world)
	{
		this(world, null);
		/*//super(world);

		try
		{
			Field field = Navigation.class.getDeclaredField("e");
			field.setAccessible(true);
			AttributeInstance e = (AttributeInstance) field.get(this.getNavigation());
			e.setValue(128); // Navigation distance in block lengths goes here
		}
		catch (Exception ex)
		{
		}

		try
		{
			Field gsa = PathfinderGoalSelector.class.getDeclaredField("b");
			gsa.setAccessible(true);
			gsa.set(this.goalSelector, new UnsafeList<Object>());
			gsa.set(this.targetSelector, new UnsafeList<Object>());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		this.getNavigation().b(true);
		this.goalSelector.a(0, new PathfinderGoalFloat(this));
		this.goalSelector.a(1, new PathfinderGoalBreakBlock(this));
		this.goalSelector.a(2, new PathfinderGoalMeleeAttack(this, EntityHuman.class, 1.0D, false));
		this.goalSelector.a(4, new PathfinderGoalMeleeAttack(this, EntityVillager.class, 1.0D, true));
		this.goalSelector.a(5, new PathfinderGoalMoveTowardsRestriction(this, 1.0D));
		this.goalSelector.a(8, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
		this.goalSelector.a(8, new PathfinderGoalRandomLookaround(this));
		this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this, true));
		//this.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, 0, true));
		this.a(0.6F, 1.8F);*/
	}

	public EntityHuman findNearbyVulnerablePlayer(double d0, double d1, double d2)
	{
		if (world.players.size() > 0)
		{
			int i = r.nextInt(world.players.size());
			EntityHuman entityhuman1 = (EntityHuman) this.world.players.get(i);

			if (!entityhuman1.abilities.isInvulnerable && entityhuman1.isAlive())
			{
				return entityhuman1;
			}
		}
		return null;
	}

	@Override
	public Entity findTarget()
	{
		return super.findTarget();
		//EntityHuman entityhuman = findNearbyVulnerablePlayer(this.locX, this.locY, this.locZ);
		//return entityhuman;
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
