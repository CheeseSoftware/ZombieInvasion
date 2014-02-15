package io.github.gustav9797.ZombieInvasion;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.data.DataException;

public abstract class Arena implements Listener
{
	protected int size;
	protected Location middle;
	protected Random r = new Random();
	protected String name;
	protected FileConfiguration config;
	protected File configFile;
	
	protected int tickTaskId = -1;
	protected int ticksPassed = -1;
	protected int oldMinutesPassed = -1;
	protected int ticksSinceLastWave = -1;
	
	public List<Player> players = new LinkedList<Player>();

	// Stores the original blocks to be able to restore the border later
	protected LinkedList<BlockState> border;

	public Arena(String name, JavaPlugin plugin)
	{
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		this.name = name;
		border = new LinkedList<BlockState>();
		File dir = new File(plugin.getDataFolder() + File.separator + name);
		if (!dir.exists())
			dir.mkdir();

		String configPath = plugin.getDataFolder() + File.separator + name + File.separator + "config.yml";
		File file = new File(configPath);
		if (!file.exists())
		{
			try
			{
				file.createNewFile();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		configFile = file;
		config = YamlConfiguration.loadConfiguration(file);
	}

	public void Reset(JavaPlugin plugin)
	{
		EditSession es = new EditSession(new BukkitWorld(middle.getWorld()), 999999999);
		File schematic = new File(plugin.getDataFolder() + File.separator + this.name + File.separator + name + ".schematic");
		try
		{
			@SuppressWarnings("deprecation")
			CuboidClipboard cc = CuboidClipboard.loadSchematic(schematic);
			Vector location = new Vector(this.middle.getBlockX() + this.size / 2 - 1, this.middle.getBlockY() + cc.getHeight() - 2, this.middle.getBlockZ() + this.size / 2 - 1);
			cc.paste(es, location, false);
		}
		catch (MaxChangedBlocksException | DataException | IOException e)
		{
			e.printStackTrace();
		}
	}

	public void Load(JavaPlugin plugin)
	{
		String world = config.getString("world");
		if (plugin.getServer().getWorld(world) != null)
		{
			this.middle = new Location(plugin.getServer().getWorld(world), config.getInt("x"), config.getInt("y"), config.getInt("z"));
			this.size = config.getInt("size");
		}
	}

	public void Save(JavaPlugin plugin)
	{
		try
		{
			config.set("world", middle.getWorld().getName());
			config.set("x", middle.getBlockX());
			config.set("y", middle.getBlockY());
			config.set("z", middle.getBlockZ());
			config.set("size", this.size);
			config.save(configFile);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	abstract void StartWave(int wave, JavaPlugin plugin);

	public void CreateBorder(int height, Material material)
	{
		if (!border.isEmpty())
			RestoreBorder();

		int radius = size / 2;
		BlockState originalBlock = null;
		World world = middle.getWorld();
		for (int y = 0; y < height; y++)
		{
			for (int x = -radius; x < radius; x++)
			{
				originalBlock = world.getBlockAt(x + middle.getBlockX(), y, -radius + middle.getBlockZ()).getState();
				if (originalBlock.getType() == Material.AIR)
				{
					this.border.push(originalBlock);
					world.getBlockAt(x + middle.getBlockX(), y, -radius + middle.getBlockZ()).setType(material);
				}

				originalBlock = world.getBlockAt(x + middle.getBlockX(), y, radius + middle.getBlockZ()).getState();
				if (originalBlock.getType() == Material.AIR)
				{
					this.border.push(originalBlock);
					world.getBlockAt(x + middle.getBlockX(), y, radius + middle.getBlockZ()).setType(material);
				}
			}

			for (int z = -radius; z < radius; z++)
			{
				originalBlock = world.getBlockAt(-radius + middle.getBlockX(), y, z + middle.getBlockZ()).getState();
				if (originalBlock.getType() == Material.AIR)
				{
					this.border.push(originalBlock);
					world.getBlockAt(-radius + middle.getBlockX(), y, z + middle.getBlockZ()).setType(material);
				}

				originalBlock = world.getBlockAt(radius + middle.getBlockX(), y, z + middle.getBlockZ()).getState();
				if (originalBlock.getType() == Material.AIR)
				{
					this.border.push(originalBlock);
					world.getBlockAt(radius + middle.getBlockX(), y, z + middle.getBlockZ()).setType(material);
				}
			}
		}
	}

	public void SendWaves(final JavaPlugin plugin)
	{
		this.StartWave(5, plugin);
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		this.tickTaskId = scheduler.scheduleSyncRepeatingTask(plugin, new Runnable()
		{
			@Override
			public void run()
			{
				Tick(plugin);
			}
			// Do something
		}, 0L, 100L);
	}

	public void Tick(JavaPlugin plugin)
	{
		this.ticksSinceLastWave += 100;
		ticksPassed += 100;
		int minutesPassed = ticksPassed / 20 / 60;
		if(minutesPassed != oldMinutesPassed)
		{
			for(Player p : players)
			{
				p.sendMessage(minutesPassed  + " minutes have passed!");
			}
			oldMinutesPassed = minutesPassed;
		}
	}

	public void RestoreBorder()
	{
		for (BlockState block : border)
		{
			block.getBlock().setType(block.getType());
		}
	}

	public void setSize(int size, JavaPlugin plugin)
	{
		this.size = size;
		Save(plugin);
	}

	public int getSize()
	{
		return this.size;
	}

	public void setMiddle(Location middle, JavaPlugin plugin)
	{
		this.middle = middle;
		Save(plugin);
	}

	public Location getMiddle()
	{
		return this.middle;
	}
	
	public void onPlayerLogin(PlayerLoginEvent event)
	{
		if(players.contains(event.getPlayer()))
		{
			players.remove(event.getPlayer());
		}
	}
	
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		if(players.contains(event.getPlayer()))
		{
			players.remove(event.getPlayer());
		}
	}

}
