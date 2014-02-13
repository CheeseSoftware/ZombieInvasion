package io.github.gustav9797.ZombieInvasion;

import java.lang.reflect.Field;

import net.minecraft.server.v1_7_R1.EntityHuman;
import net.minecraft.server.v1_7_R1.EntityVillager;
import net.minecraft.server.v1_7_R1.EntityZombie;
import net.minecraft.server.v1_7_R1.PathfinderGoalBreakDoor;
import net.minecraft.server.v1_7_R1.PathfinderGoalFloat;
import net.minecraft.server.v1_7_R1.PathfinderGoalHurtByTarget;
import net.minecraft.server.v1_7_R1.PathfinderGoalLookAtPlayer;
import net.minecraft.server.v1_7_R1.PathfinderGoalMeleeAttack;
import net.minecraft.server.v1_7_R1.PathfinderGoalMoveThroughVillage;
import net.minecraft.server.v1_7_R1.PathfinderGoalMoveTowardsRestriction;
import net.minecraft.server.v1_7_R1.PathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_7_R1.PathfinderGoalRandomLookaround;
import net.minecraft.server.v1_7_R1.PathfinderGoalRandomStroll;
import net.minecraft.server.v1_7_R1.PathfinderGoalSelector;
import net.minecraft.server.v1_7_R1.World;

import org.bukkit.craftbukkit.v1_7_R1.util.UnsafeList;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;


public class EntityFastZombie extends EntityZombie
{

	public EntityFastZombie(World world)
	{
		super(world);

		try
		{

			Field gsa = PathfinderGoalSelector.class.getDeclaredField("a");
			gsa.setAccessible(true);
			
			gsa.set(this.goalSelector, new UnsafeList());
			gsa.set(this.targetSelector, new UnsafeList());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		this.goalSelector.a(0, new PathfinderGoalFloat(this));
		this.goalSelector.a(1, new PathfinderGoalBreakDoor(this));
		this.goalSelector.a(2, new PathfinderGoalMeleeAttack(this, EntityHuman.class, this.bl, false));
		this.goalSelector.a(3, new PathfinderGoalMeleeAttack(this, EntityVillager.class, this.bl, true));
		this.goalSelector.a(4, new PathfinderGoalMoveTowardsRestriction(this, this.bl));
		this.goalSelector.a(5, new PathfinderGoalMoveThroughVillage(this, this.bl, false));
		this.goalSelector.a(6, new PathfinderGoalRandomStroll(this, this.bl));
		this.goalSelector.a(7, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
		this.goalSelector.a(8, new PathfinderGoalRandomLookaround(this));
		
		this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this, true));
		this.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, 0, true));
		this.targetSelector.a(3, new PathfinderGoalNearestAttackableTarget(this, EntityVillager.class, 0, false));
		
	}

}
