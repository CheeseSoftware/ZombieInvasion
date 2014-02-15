package io.github.gustav9797.ZombieInvasion;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.server.v1_7_R1.BiomeBase;
import net.minecraft.server.v1_7_R1.BiomeMeta;
import net.minecraft.server.v1_7_R1.EntityZombie;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_7_R1.CraftWorld;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.LazyMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

public final class ZombieInvasion extends JavaPlugin implements Listener
{
	LinkedList<CustomEntityType> entityTypes;
	Random r = new Random();
	Map<String, Arena> arenas;

	@Override
	public void onEnable()
	{
		entityTypes = new LinkedList<CustomEntityType>();
		entityTypes.add(new CustomEntityType("Zombie", 54, EntityType.ZOMBIE, EntityZombie.class, EntityFastZombie.class));
		registerEntities();

		arenas = new HashMap<String, Arena>();
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
			if (cmd.getName().equals("createarena"))
			{
				if (args.length > 0)
				{
					String name = args[0];
					if (!arenas.containsKey(name))
					{
						arenas.put(name, new ZombieArena(96, player.getLocation()));
						sender.sendMessage("Arena " + name + " created!");
					}
					else
						sender.sendMessage("Arena already exists.");
				}
				else
					sender.sendMessage("Usage: /createarena <name>");
				return true;
			}
			else if (cmd.getName().equals("removearena"))
			{
				if (args.length > 0)
				{
					String name = args[0];
					if (arenas.containsKey(name))
					{
						arenas.remove(name);
						sender.sendMessage("Arena " + name + " removed!");
					}
					else
						sender.sendMessage("Arena doesn't exists.");
				}
				else
					sender.sendMessage("Usage: /removearena <name>");
				return true;
			}
			else if (cmd.getName().equals("joinarena"))
			{
				if (args.length > 0)
				{
					String name = args[0];
					if (arenas.containsKey(name))
					{
						Arena arena = arenas.get(name);
						player.teleport(arena.getMiddle());
						player.setMetadata("arena", new FixedMetadataValue(this, name));
						sender.sendMessage("Arena joined!");
					}
					else
						sender.sendMessage("Arena doesn't exist.");
				}
				else
					sender.sendMessage("Usage: /joinarena <name>");
				return true;
			}
			else if (cmd.getName().equals("startwave"))
			{
				if (player.hasMetadata("arena") && arenas.containsKey(player.getMetadata("arena").get(0).asString()))
				{
					Arena arena = arenas.get(player.getMetadata("arena").get(0).asString());
					if (args.length > 0)
					{
						int wave = Integer.parseInt(args[0]);
						if (wave > 0 && wave < 100)
						{
							arena.StartWave(wave, this);
							sender.sendMessage("Wave " + wave + " has begun!");
						}
						else
							sender.sendMessage("Wave has to be between 0 and 100.");
					}
					else
						sender.sendMessage("Usage: /startwave <wave>");
				}
				else
					sender.sendMessage("You have to join an arena! (/joinarena)");
				return true;
			}
			else if (cmd.getName().equals("setmiddle"))
			{
				if (player.hasMetadata("arena") && arenas.containsKey(player.getMetadata("arena").get(0).asString()))
				{
					Arena arena = arenas.get(player.getMetadata("arena").get(0).asString());
					arena.setMiddle(player.getLocation());
					sender.sendMessage("Arena middle was set!");
				}
				else
					sender.sendMessage("You have to join an arena! (/joinarena)");
				return true;
			}
			else if (cmd.getName().equals("setsize"))
			{
				if (player.hasMetadata("arena") && arenas.containsKey(player.getMetadata("arena").get(0).asString()))
				{
					Arena arena = arenas.get(player.getMetadata("arena").get(0).asString());
					if (args.length > 0)
					{
						int size = Integer.parseInt(args[0]);
						if (size > 0 && size <= 128)
						{
							arena.setSize(size);
							sender.sendMessage("Size was set to " + size);
						}
						else
							sender.sendMessage("Size has to be between 0 and 128.");
					}
					else
						sender.sendMessage("Usage: /setsize <size>");
				}
				else
					sender.sendMessage("You have to join an arena! (/joinarena)");
				return true;
			}
			else if (cmd.getName().equals("createborder"))
			{
				if (player.hasMetadata("arena") && arenas.containsKey(player.getMetadata("arena").get(0).asString()))
				{
					Arena arena = arenas.get(player.getMetadata("arena").get(0).asString());
					arena.CreateBorder(150, Material.GLASS);
					sender.sendMessage("Border created.");
				}
				else
					sender.sendMessage("You have to join an arena! (/joinarena)");
				return true;
			}
			else if (cmd.getName().equals("removeborder"))
			{
				if (player.hasMetadata("arena") && arenas.containsKey(player.getMetadata("arena").get(0).asString()))
				{
					Arena arena = arenas.get(player.getMetadata("arena").get(0).asString());
					arena.RestoreBorder();
					sender.sendMessage("Border removed.");
				}
				else
					sender.sendMessage("You have to join an arena! (/joinarena)");
				return true;
			}
		}
		return false;
	}

	private static Object getPrivateStatic(@SuppressWarnings("rawtypes") Class clazz, String f) throws Exception
	{
		Field field = clazz.getDeclaredField(f);
		field.setAccessible(true);
		return field.get(null);
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
			return;
		}
		for (BiomeBase biomeBase : biomes)
		{
			if (biomeBase == null)
				break;
			for (String field : new String[]
			{ "as", "at", "au", "av" })
				try
				{
					Field list = BiomeBase.class.getDeclaredField(field);
					list.setAccessible(true);
					@SuppressWarnings("unchecked")
					List<BiomeMeta> mobList = (List<BiomeMeta>) list.get(biomeBase);

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

}
