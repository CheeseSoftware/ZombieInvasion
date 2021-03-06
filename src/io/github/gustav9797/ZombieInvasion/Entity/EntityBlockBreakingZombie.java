package io.github.gustav9797.ZombieInvasion.Entity;

import io.github.gustav9797.ZombieInvasion.Arena;
import io.github.gustav9797.ZombieInvasion.PathfinderGoal.PathfinderGoalBreakBlock;
import io.github.gustav9797.ZombieInvasion.PathfinderGoal.PathfinderGoalCustomMeleeAttack;
import io.github.gustav9797.ZombieInvasion.PathfinderGoal.PathfinderGoalCustomNearestAttackableTarget;
import io.github.gustav9797.ZombieInvasion.PathfinderGoal.PathfinderGoalWalkToTile;

import java.lang.reflect.Field;
import java.util.Random;

import net.minecraft.server.v1_7_R2.AttributeInstance;
import net.minecraft.server.v1_7_R2.Entity;
import net.minecraft.server.v1_7_R2.EntityHuman;
import net.minecraft.server.v1_7_R2.EntityZombie;
import net.minecraft.server.v1_7_R2.Navigation;
import net.minecraft.server.v1_7_R2.PathfinderGoalFloat;
import net.minecraft.server.v1_7_R2.PathfinderGoalHurtByTarget;
import net.minecraft.server.v1_7_R2.PathfinderGoalLookAtPlayer;
import net.minecraft.server.v1_7_R2.PathfinderGoalRandomLookaround;
import net.minecraft.server.v1_7_R2.PathfinderGoalSelector;
import net.minecraft.server.v1_7_R2.World;

import org.bukkit.craftbukkit.v1_7_R2.util.UnsafeList;

public class EntityBlockBreakingZombie extends EntityZombie implements ICustomMonster
{
	private Random r = new Random();

	public EntityBlockBreakingZombie(World world)
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
		this.goalSelector.a(6, new PathfinderGoalFloat(this));
		this.goalSelector.a(7, new PathfinderGoalCustomMeleeAttack(this, EntityHuman.class, 1.0D, false));
		this.goalSelector.a(8, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
		this.goalSelector.a(8, new PathfinderGoalRandomLookaround(this));
		this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this, true));
		this.a(0.6F, 1.8F);

	}

	public void setArena(Arena arena)
	{
		if (arena != null)
			this.targetSelector.a(0, new PathfinderGoalWalkToTile(this, 1.0F, arena.getSpawnLocation()));
		
		if (random.nextInt(8) == 0)
		{
			//ItemStack[] equipment = this.getEquipment();
			//v�gar inte g�ra n�got mer:/ skeletonhuvuden!
			
			this.goalSelector.a(1, new PathfinderGoalBreakBlock(this, arena, true));
		}
		else
		{
			this.goalSelector.a(1, new PathfinderGoalBreakBlock(this, arena));
		}
		
		this.targetSelector.a(0, new PathfinderGoalCustomNearestAttackableTarget(this, 0, arena));
	}

	@Override
	protected Entity findTarget()
	{
		EntityHuman entityhuman = this.findNearbyVulnerablePlayer(128, 128, 128);

		return entityhuman;
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
}
