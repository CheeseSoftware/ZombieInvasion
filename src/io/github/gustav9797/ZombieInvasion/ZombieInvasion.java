package io.github.gustav9797.ZombieInvasion;

<<<<<<< HEAD
=======
import io.github.gustav9797.ZombieInvasion.CustomEntityType;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
>>>>>>> 739569ab4247e5a56238ec5b34ffcf7ea04d837d
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

<<<<<<< HEAD
=======
import net.minecraft.server.v1_7_R1.BiomeBase;
import net.minecraft.server.v1_7_R1.BiomeForest;
import net.minecraft.server.v1_7_R1.BiomeMeta;
import net.minecraft.server.v1_7_R1.EntityTypes;
>>>>>>> 739569ab4247e5a56238ec5b34ffcf7ea04d837d
import net.minecraft.server.v1_7_R1.EntityZombie;
import net.minecraft.server.v1_7_R1.Vec3D;
import net.minecraft.server.v1_7_R1.World;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_7_R1.CraftWorld;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class ZombieInvasion extends JavaPlugin implements Listener
{
	LinkedList<CustomEntityType> entityTypes = new LinkedList<CustomEntityType>();
	Random r = new Random();
	Location middle = new Location(null, 0, 80, 0);
	int widthheight = 128;

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
				World mcWorld = ((CraftWorld) player.getWorld()).getHandle();
				EntityFastZombie zombie = new EntityFastZombie(mcWorld);
				zombie.setPosition(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ());
				mcWorld.addEntity(zombie);
				return true;
			}
			else if (cmd.getName().equals("startwave"))
			{
				if (args.length > 0)
				{
					int wave = Integer.parseInt(args[0]);
					if (wave > 0 && wave < 100)
					{
						for (int i = 0; i < 10; i++)
						{
							SpawnZombies(new Location(player.getWorld(), 
									r.nextInt(widthheight) + middle.getBlockX() - 64, 
									r.nextInt(widthheight) + middle.getBlockY(), 
									r.nextInt(widthheight) + middle.getBlockZ() - 64), wave);
						}
					}
				}
			}
		}
		return false;
	}

	private static Object getPrivateStatic(Class clazz, String f) throws Exception
	{
		Field field = clazz.getDeclaredField(f);
		field.setAccessible(true);
		return field.get(null);
	}

	public void SpawnZombies(Location l, int amount)
	{
		for (int i = 0; i < amount; i++)
		{
			Location pos = new Location(l.getWorld(), r.nextInt(11) - 5 + l.getBlockX(), l.getBlockY(), r.nextInt(11) - 5 + l.getBlockZ());
			World mcWorld = ((CraftWorld) l.getWorld()).getHandle();
			while(pos.getWorld().getBlockAt(pos).getType() == Material.AIR)
				pos.setY(pos.getBlockY() - 1);
			pos.setY(pos.getBlockY() + 2);
			EntityFastZombie zombie = new EntityFastZombie(mcWorld);
			zombie.setPosition(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ());
			mcWorld.addEntity(zombie);
		}
	}

	public void registerEntities()
	{
		BiomeBase[] biomes;
		try
		{
			biomes = (BiomeBase[]) getPrivateStatic(BiomeBase.class, "biomes");
		}
		catch (Exception exc)
		{
			// Unable to fetch.
			return;
		}
		for (BiomeBase biomeBase : biomes)
		{
			if (biomeBase == null)
				break;

			// This changed names from J, K, L and M.
			for (String field : new String[]
			{ "as", "at", "au", "av" })
				try
				{
					Field list = BiomeBase.class.getDeclaredField(field);
					list.setAccessible(true);
					@SuppressWarnings("unchecked")
					List<BiomeMeta> mobList = (List<BiomeMeta>) list.get(biomeBase);

					// Write in our custom class.
					for (BiomeMeta meta : mobList)
						for (CustomEntityType entity : entityTypes)
							if (entity.getNMSClass().equals(meta.b))
								meta.b = entity.getCustomClass();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
		}
	}
	/*
	 * public void registerEntities() { for (CustomEntityType entity :
	 * entityTypes) { try { Method reg =
	 * EntityTypes.class.getDeclaredMethod("a", Class.class, String.class,
	 * int.class); reg.setAccessible(true); reg.invoke(null,
	 * entity.getCustomClass(), entity.getName(), entity.getId()); } catch
	 * (Exception e) { e.printStackTrace(); getLogger().info("ID:" +
	 * entity.getId() + " name:" + entity.getName()); } } }
	 */
}
