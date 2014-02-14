package io.github.gustav9797.ZombieInvasion;

import io.github.gustav9797.ZombieInvasion.CustomEntityType;

import java.lang.reflect.Method;
import java.util.LinkedList;

import net.minecraft.server.v1_7_R1.EntityTypes;
import net.minecraft.server.v1_7_R1.EntityZombie;
import net.minecraft.server.v1_7_R1.World;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_7_R1.CraftWorld;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class ZombieInvasion extends JavaPlugin
{
	LinkedList<CustomEntityType> entityTypes = new LinkedList<CustomEntityType>();

	@Override
	public void onEnable()
	{
		entityTypes.add(new CustomEntityType("Zombie", 54, EntityType.ZOMBIE, EntityZombie.class, EntityFastZombie.class));
<<<<<<< HEAD
		//registerEntities();
=======
		getServer().getPluginManager().registerEvents(new EventListener(this), this);
		registerEntities();
		
>>>>>>> 0e7146d5b8b6865fa3a164094dbf867e1b423c4c
	}

	@Override
	public void onDisable()
	{

	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if (sender instanceof Player)
		{
			Player player = (Player) sender;
			if (cmd.getName().equals("zombie"))
			{
				 World mcWorld = ((CraftWorld)player.getWorld()).getHandle(); 
				 EntityFastZombie zombie = new EntityFastZombie(mcWorld);
				 zombie.setPosition(player.getLocation().getX(),
				 player.getLocation().getY(), player.getLocation().getZ());
				 mcWorld.addEntity(zombie);
				return true;
			}
		}
		return false;
	}

	/*public void registerEntities()
	{
		for (CustomEntityType entity : entityTypes)
		{
			try
			{
				Method reg = EntityTypes.class.getDeclaredMethod("a", Class.class, String.class, int.class);
				reg.setAccessible(true);
				reg.invoke(null, entity.getCustomClass(), entity.getName(), entity.getId());
			}
			catch (Exception e)
			{
				e.printStackTrace();
				getLogger().info("ID:" + entity.getId() + " name:" + entity.getName());
			}
		}
	}*/
}
