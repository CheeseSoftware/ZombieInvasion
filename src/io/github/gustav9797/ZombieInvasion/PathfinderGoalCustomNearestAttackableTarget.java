package io.github.gustav9797.ZombieInvasion;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_7_R1.EntityCreature;
import net.minecraft.server.v1_7_R1.EntityLiving;

public class PathfinderGoalCustomNearestAttackableTarget extends PathfinderGoalCustomTarget
{

	private final int attackCheckFrequency;
	private EntityLiving target;
	private Arena arena;

	public PathfinderGoalCustomNearestAttackableTarget(EntityCreature entitycreature, int attackCheckFrequency, Arena arena)
	{
		super(entitycreature);
		this.attackCheckFrequency = attackCheckFrequency;
		this.arena = arena;
		this.a(1);
	}

	public boolean a() // canExecute
	{
		if (this.attackCheckFrequency > 0 && this.entity.aI().nextInt(this.attackCheckFrequency) != 0)
		{
			return false;
		}
		else if (arena != null)
		//else if(this.entity.getGoalTarget() == null)
		{
			// this.target = (EntityLiving) list.get(0);
			List<Player> players = new ArrayList<Player>(arena.players);
			//Player[] players = Bukkit.getServer().getOnlinePlayers();
			Player closestPlayer = null;
			double closestPlayerDistance = Double.MAX_VALUE;
			for(Player player : players)
			{
				double distance = player.getLocation().distance(this.entity.getBukkitEntity().getLocation());
				if(distance < closestPlayerDistance && player.getGameMode() == GameMode.SURVIVAL)
				{
					closestPlayerDistance = distance;
					closestPlayer = player;
				}
			}
			
			if(closestPlayer != null)
			{
				this.target= ((CraftPlayer)closestPlayer).getHandle();
				this.entity.setGoalTarget(target);
				//this.entity.target = this.target;
				return true;
			}
		}
		return false;
	}

	public void c() // setup
	{
		this.entity.setGoalTarget(this.target);
		super.c();
	}
}