package io.github.gustav9797.ZombieInvasion;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.logging.Level;

import net.minecraft.server.v1_6_R3.EntityInsentient;
import net.minecraft.server.v1_6_R3.EntityZombie;
import net.minecraft.server.v1_6_R3.EntityTypes;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_6_R3.CraftWorld;
import org.bukkit.entity.Entity;
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
		registerEntities();
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
				 net.minecraft.server.v1_6_R3.World mcWorld = ((CraftWorld)player.getWorld()).getHandle(); 
				 EntityFastZombie zombie = new EntityFastZombie(mcWorld);
				 zombie.setPosition(player.getLocation().getX(),
				 player.getLocation().getY(), player.getLocation().getZ());
				 mcWorld.addEntity(zombie);
				return true;
			}
		}
		return false;
	}

	public void registerEntities()
	{
		for (CustomEntityType entity : entityTypes)
		{
			try
			{
				Method reg = EntityTypes.class.getDeclaredMethod("a", Class.class, String.class, int.class);
				reg.setAccessible(true);
				reg.invoke(null, entity.getCustomClass(), entity.getName(), entity.getId());
			}
			catch (ReflectiveOperationException e)
			{
				e.printStackTrace();
			}
		}
	}
}
