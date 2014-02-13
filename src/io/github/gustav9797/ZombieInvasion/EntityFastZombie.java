package io.github.gustav9797.ZombieInvasion;

import java.lang.reflect.Field;

import org.bukkit.craftbukkit.v1_6_R3.util.UnsafeList;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.minecraft.server.v1_6_R3.EntityZombie;
import net.minecraft.server.v1_6_R3.World;

public class EntityFastZombie extends EntityZombie
{

	public EntityFastZombie(World world)
	{
		super(world);

		try
		{

			Field gsa = net.minecraft.server.v1_6_R3.PathfinderGoalSelector.class.getDeclaredField("a");
			gsa.setAccessible(true);

			gsa.set(this.goalSelector, new UnsafeList());
			gsa.set(this.targetSelector, new UnsafeList());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		this.goalSelector.a(0, new PathfinderGoalBreakBlock(this));
	}

}
