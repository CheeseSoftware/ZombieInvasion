package io.github.gustav9797.ZombieInvasion.Entity;

import io.github.gustav9797.ZombieInvasion.Arena;
import io.github.gustav9797.ZombieInvasion.PathfinderGoal.PathfinderGoalBreakBlock;
import io.github.gustav9797.ZombieInvasion.PathfinderGoal.PathfinderGoalCustomMeleeAttack;
import io.github.gustav9797.ZombieInvasion.PathfinderGoal.PathfinderGoalFindBreakBlock;
import io.github.gustav9797.ZombieInvasion.PathfinderGoal.PathfinderGoalWalkToTile;

import java.lang.reflect.Field;
import java.util.Random;

import net.minecraft.server.v1_7_R1.AttributeInstance;
import net.minecraft.server.v1_7_R1.EntityHuman;
import net.minecraft.server.v1_7_R1.EntityVillager;
import net.minecraft.server.v1_7_R1.GenericAttributes;
import net.minecraft.server.v1_7_R1.Navigation;
import net.minecraft.server.v1_7_R1.PathfinderGoalFloat;
import net.minecraft.server.v1_7_R1.PathfinderGoalHurtByTarget;
import net.minecraft.server.v1_7_R1.PathfinderGoalLookAtPlayer;
import net.minecraft.server.v1_7_R1.PathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_7_R1.PathfinderGoalRandomLookaround;
import net.minecraft.server.v1_7_R1.PathfinderGoalRandomStroll;
import net.minecraft.server.v1_7_R1.PathfinderGoalSelector;
import net.minecraft.server.v1_7_R1.World;

import org.bukkit.craftbukkit.v1_7_R1.util.UnsafeList;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class EntityBlockBreakingVillager extends EntityVillager implements ICustomMonster
{
	private Random r = new Random();

	public EntityBlockBreakingVillager(World world)
	{
		super(world);
		try
		{
			Field field = Navigation.class.getDeclaredField("e");
			field.setAccessible(true);
			AttributeInstance e = (AttributeInstance) field.get(this.getNavigation());
			e.setValue(128); // Navigation distance in block lengths goes here
			this.getAttributeInstance(GenericAttributes.d).setValue(0.3D); // walking
																			// speed
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

		this.setProfession(r.nextInt(6));
		int profession = this.getProfession();

		switch (profession)
		{
			case 0: // farmer
				((LivingEntity) this.getBukkitEntity()).addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, Integer.MAX_VALUE, 8));
				break;
			case 1: // Librarian
				((LivingEntity) this.getBukkitEntity()).addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 8));
				((LivingEntity) this.getBukkitEntity()).addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, Integer.MAX_VALUE, 2));
				((LivingEntity) this.getBukkitEntity()).addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 1));
				((LivingEntity) this.getBukkitEntity()).addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, Integer.MAX_VALUE, 1));
				((LivingEntity) this.getBukkitEntity()).addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, Integer.MAX_VALUE, 1));
				break;
			case 2: // Priest
				((LivingEntity) this.getBukkitEntity()).addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, Integer.MAX_VALUE, 1));
				((LivingEntity) this.getBukkitEntity()).addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 1)); // heal
				// others!
				break;
			case 3: // Blacksmith
				((LivingEntity) this.getBukkitEntity()).addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 2));
				((LivingEntity) this.getBukkitEntity()).addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 1));
				((LivingEntity) this.getBukkitEntity()).addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, Integer.MAX_VALUE, 1));
				break;
			case 4: // Butcher
				((LivingEntity) this.getBukkitEntity()).addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, 1));
				break;
			case 5:
				// Generic
				((LivingEntity) this.getBukkitEntity()).addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2));
				((LivingEntity) this.getBukkitEntity()).addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 2));
				break;
		}

		this.getNavigation().b(true);
		this.goalSelector.a(6, new PathfinderGoalFloat(this));
		this.goalSelector.a(7, new PathfinderGoalCustomMeleeAttack(this, EntityHuman.class, 1.0D, false));
		this.goalSelector.a(8, new PathfinderGoalRandomStroll(this, 1.0D));
		this.goalSelector.a(8, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
		this.goalSelector.a(8, new PathfinderGoalRandomLookaround(this));
		this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this, true));
		this.targetSelector.a(0, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, 0, true));
		this.a(0.6F, 1.8F);
	}

	public void setArena(Arena arena)
	{
		if (arena != null)
			this.targetSelector.a(0, new PathfinderGoalWalkToTile(this, 1.0F, arena.getSpawnLocation()));
		if (this.getProfession() == 0)
			this.goalSelector.a(3, new PathfinderGoalFindBreakBlock(this, arena, 50));
		else if(this.getProfession() == 5)
			this.goalSelector.a(3, new PathfinderGoalBreakBlock(this, arena));
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
	public int aV() {
		int i = super.aV() + 2;

		if (i > 80) {
	       i = 80;
	    }
	
		return i;
	}

}