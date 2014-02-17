package io.github.gustav9797.ZombieInvasion;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.server.v1_7_R1.BiomeBase;
import net.minecraft.server.v1_7_R1.BiomeMeta;
import net.minecraft.server.v1_7_R1.EntityZombie;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import ostkaka34.OstEconomyPlugin.IOstEconomy;

public final class ZombieInvasion extends JavaPlugin implements Listener
{
	LinkedList<CustomEntityType> entityTypes;
	Random r = new Random();
	Map<String, Arena> arenas;
	Lobby lobby;
	File configFile;
	public static IOstEconomy economyPlugin;

	public void Save()
	{
		YamlConfiguration config = new YamlConfiguration();
		List<String> temp = new LinkedList<String>();
		for (Arena a : arenas.values())
			temp.add(a.name);
		config.set("zombiearenas", temp);
		try
		{
			config.save(configFile);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void Load()
	{
		YamlConfiguration config = new YamlConfiguration();
		try
		{
			config.load(configFile);
		}
		catch (IOException | InvalidConfigurationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		@SuppressWarnings("unchecked")
		List<String> temp = (List<String>) config.getList("zombiearenas");
		for (String arena : temp)
		{
			ZombieArena a = new ZombieArena(arena, this, lobby);
			a.Load(this);
			arenas.put(arena, a);
		}
	}

	@Override
	public void onEnable()
	{
		configFile = new File(this.getDataFolder() + File.separator + "config.yml");
		entityTypes = new LinkedList<CustomEntityType>();
		entityTypes.add(new CustomEntityType("Zombie", 54, EntityType.ZOMBIE, EntityZombie.class, EntityFastZombie.class));
		registerEntities();
		arenas = new HashMap<String, Arena>();
		lobby = new Lobby(arenas, this);
		
		if(!configFile.exists())
		{
			try
			{
				configFile.createNewFile();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			this.Save();
		}
		this.Load();

		Plugin[] plugins = getServer().getPluginManager().getPlugins();
		for (int i = 0; i < plugins.length; i++)
		{
			if (plugins[i] instanceof IOstEconomy)
			{
				economyPlugin = (IOstEconomy) plugins[i];
				break;
			}
		}
		
		getServer().getPluginManager().registerEvents(this, this);
	}

	@Override
	public void onDisable()
	{
		this.saveDefaultConfig();
		this.saveConfig();
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
						Arena a = new ZombieArena(name, this, this.lobby);
						a.setMiddle(player.getLocation(), this);
						a.setSpawnLocation(player.getLocation(), this);
						a.setSize(96, this);
						arenas.put(name, a);
						this.Save();
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
						File file = new File(this.getDataFolder() + File.separator + name + File.separator + "config.yml");
						file.delete();
						File file2 = new File(this.getDataFolder() + File.separator + name);
						file2.delete();
						this.Save();
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
						arena.onPlayerJoinArena(player, this);
					}
					else
						sender.sendMessage("Arena doesn't exist.");
				}
				else
					sender.sendMessage("Usage: /joinarena <name>");
				return true;
			}
			else if (cmd.getName().equals("leavearena"))
			{
				if (player.hasMetadata("arena") && arenas.containsKey(player.getMetadata("arena").get(0).asString()))
				{
					Arena arena = arenas.get(player.getMetadata("arena").get(0).asString());
					arena.onPlayerLeaveArena(player, "left the arena", this);
				}
				else
					sender.sendMessage("You haven't joined any arena!");
				return true;
			}
			else if (cmd.getName().equals("startwave"))
			{
				if (player.hasMetadata("arena") && arenas.containsKey(player.getMetadata("arena").get(0).asString()))
				{
					Arena arena = arenas.get(player.getMetadata("arena").get(0).asString());
					arena.SendWaves(this);
					this.getServer().broadcastMessage("Waves are coming! Hide!");
				}
				else
					sender.sendMessage("You have to join an arena! (/joinarena)");
				return true;
			}
			else if (cmd.getName().equals("setlocation"))
			{
				if (player.hasMetadata("arena") && arenas.containsKey(player.getMetadata("arena").get(0).asString()))
				{
					Arena arena = arenas.get(player.getMetadata("arena").get(0).asString());
					arena.setMiddle(player.getLocation(), this);
					sender.sendMessage("Arena middle was set!");
				}
				else
					sender.sendMessage("You have to join an arena! (/joinarena)");
				return true;
			}
			else if (cmd.getName().equals("setarenaspawn"))
			{
				if (player.hasMetadata("arena") && arenas.containsKey(player.getMetadata("arena").get(0).asString()))
				{
					Arena arena = arenas.get(player.getMetadata("arena").get(0).asString());
					arena.setSpawnLocation(player.getLocation(), this);
					sender.sendMessage("Arena spawn was set!");
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
							arena.setSize(size, this);
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
			else if (cmd.getName().equals("reset"))
			{
				if (player.hasMetadata("arena") && arenas.containsKey(player.getMetadata("arena").get(0).asString()))
				{
					Arena arena = arenas.get(player.getMetadata("arena").get(0).asString());
					arena.ResetMap(this);
					arena.Reset();
				}
				else
					sender.sendMessage("You have to join an arena! (/joinarena)");
				return true;
			}
			else if (cmd.getName().equals("setlobby"))
			{
				lobby.setLocation(player.getLocation());
				sender.sendMessage("Lobby set!");
				return true;
			}
			else if(cmd.getName().equals("reloadzombieinvasion"))
			{
				this.Reload();
				sender.sendMessage("Reloaded!");
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
	
	public void Reload()
	{
		this.Load();
		this.lobby.Load();
		for(Arena a : arenas.values())
			a.Load(this);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPlayerQuit(PlayerQuitEvent event)
	{
		for(Arena a : arenas.values())
		if (a.players.contains(event.getPlayer()))
			a.onPlayerLeaveArena(event.getPlayer(), "left the arena", this);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPlayerDeath(PlayerDeathEvent event)
	{
		for(Arena a : arenas.values())
			if (a.players.contains(event.getEntity()))
				a.onPlayerDeath(event);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPlayerRespawn(PlayerRespawnEvent event)
	{
		for(Arena a : arenas.values())
			if (a.players.contains(event.getPlayer()))
				a.onPlayerRespawn(event);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPlayerInteract(PlayerInteractEvent event)
	{
		for(Arena a : arenas.values())
			if (a.players.contains(event.getPlayer()))
				a.onPlayerInteract(event);
	}

}
