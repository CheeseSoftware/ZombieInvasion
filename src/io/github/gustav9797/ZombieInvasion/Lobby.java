package io.github.gustav9797.ZombieInvasion;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Lobby implements Listener
{
	Location l;
	String worldName = "world";
	Map<String, Arena> arenas;
	JavaPlugin plugin;
	List<Location> signs = new LinkedList<Location>();
	File configFile;

	public Lobby(Map<String, Arena> arenas, JavaPlugin plugin)
	{
		configFile = new File(plugin.getDataFolder() + File.separator + "lobby.yml");
		l = new Location(plugin.getServer().getWorld(worldName), 0, 0, 0);
		this.arenas = arenas;
		this.plugin = plugin;
		
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
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	public Location getLocation()
	{
		return this.l;
	}
	
	public void setLocation(Location l)
	{
		this.l = l;
		Save();
	}
	
	public void Save()
	{
		YamlConfiguration config = new YamlConfiguration();
		try
		{
			config.set("location", this.l.toVector());
			config.set("world", this.worldName);
			//config.set("signs", this.signs);
			config.save(this.configFile);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public void Load()
	{
		YamlConfiguration config = new YamlConfiguration();
		try
		{
			config.load(configFile);
			World world = Bukkit.getServer().getWorld(config.getString("world"));
			l = config.getVector("location").toLocation(world);
			//this.signs = (List<Location>)config.getList("signs");
		}
		catch (IOException | InvalidConfigurationException e)
		{
			e.printStackTrace();
		}
	}

	@EventHandler
	private void onPlayerInteract(PlayerInteractEvent event)
	{
		if (event.hasBlock())
		{
			Block block = event.getClickedBlock();
			if (block.getType() == Material.WALL_SIGN)
			{
				Sign sign = (Sign) block.getState();
				String[] text = sign.getLines();
				if(this.signs.contains(sign.getLocation()))
				{
					String arenaName = text[1];
					if (arenas.containsKey(arenaName))
					{
						Arena arena = arenas.get(arenaName);
						Player player = event.getPlayer();
						if (arena != null)
						{
							if (event.getAction() == Action.RIGHT_CLICK_BLOCK)
							{
								arena.onPlayerJoinArena(player, plugin);
							}
						}
					}
				}
			}
		}
	}

	@EventHandler
	private void onSignChange(SignChangeEvent event)
	{
		Player player = event.getPlayer();
		if (player.hasPermission("zombieinvasion.admin"))
		{
			//Block block = event.getBlock();
			//if(block.getType() == Material.SIGN)
			//{
				//Sign sign = (Sign) block;
				String[] text = event.getLines();
				if (text[0].equals("ZombieInvasion"))
				{
					String arenaName = text[1];
					if (arenas.containsKey(arenaName))
					{
						signs.add(event.getBlock().getLocation());
						Save();
						player.sendMessage("Lobby sign successfully placed!");
					}
					else
						player.sendMessage("Arena " + arenaName + " doesn't exist.");
				}
			//}
		}
	}
}
