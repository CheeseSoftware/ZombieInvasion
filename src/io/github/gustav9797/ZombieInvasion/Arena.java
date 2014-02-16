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
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.data.DataException;

public abstract class Arena
{
	protected int size;
	protected Location middle;
	protected Vector schematicOffset = new Vector(0, 0, 0);
	protected Location spawnLocation = new Location(null, 0, 0, 0, 0, 0);
	protected Random r = new Random();
	protected String name;
	protected File configFile;
	protected Material borderMaterial; // save tihs
	protected int tickTaskId = -1;
	protected int ticksPassed = -1;
	protected int oldMinutesPassed = -1;
	protected int ticksSinceLastWave = -1;
	protected int maxPlayers = 10;
	protected int startAtPlayerCount = 2;
	protected int secondsAfterStart = 20;

	public List<Player> players = new LinkedList<Player>();
	protected LinkedList<BlockState> border;
	protected Lobby lobby;

	public Arena(String name, JavaPlugin plugin, Lobby lobby)
	{
		this.lobby = lobby;
		this.name = name;
		border = new LinkedList<BlockState>();
		File dir = new File(plugin.getDataFolder() + File.separator + name);
		if (!dir.exists())
			dir.mkdir();
		middle = new Location(Bukkit.getServer().getWorlds().get(0), 0, 0, 0);
		String configPath = plugin.getDataFolder() + File.separator + name + File.separator + "config.yml";
		configFile = new File(configPath);

		if (!configFile.exists())
		{
			try
			{
				configFile.createNewFile();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			this.Save(plugin);
		}
		this.Load(plugin);
	}

	public void Reset(JavaPlugin plugin)
	{
		EditSession es = new EditSession(new BukkitWorld(middle.getWorld()), 999999999);
		File schematic = new File(plugin.getDataFolder() + File.separator + this.name + File.separator + name + ".schematic");
		try
		{
			@SuppressWarnings("deprecation")
			CuboidClipboard cc = CuboidClipboard.loadSchematic(schematic);
			com.sk89q.worldedit.Vector location = new com.sk89q.worldedit.Vector(this.middle.getBlockX() + this.size / 2 + this.schematicOffset.getBlockX(), this.middle.getBlockY() + cc.getHeight()
					+ this.schematicOffset.getBlockY(), this.middle.getBlockZ() + this.size / 2 + this.schematicOffset.getBlockZ());
			cc.paste(es, location, false);
		}
		catch (MaxChangedBlocksException | DataException | IOException e)
		{
			e.printStackTrace();
		}
	}

	public void Load(JavaPlugin plugin)
	{
		YamlConfiguration config = new YamlConfiguration();
		try
		{
			config.load(configFile);
			String world = config.getString("world");
			if (world != null && plugin.getServer().getWorld(world) != null && config.getVector("location") != null)
			{
				this.size = config.getInt("size");
				this.startAtPlayerCount = config.getInt("startAtPlayerCount");
				this.maxPlayers = config.getInt("maxPlayers");
				this.secondsAfterStart = config.getInt("secondsAfterStart");
				this.middle = config.getVector("location").toLocation(plugin.getServer().getWorld(world));
				Vector spawnPos = config.getVector("spawnLocation");
				this.spawnLocation = spawnPos.toLocation(plugin.getServer().getWorld(world), (float) config.getDouble("spawnLocationYaw"), (float) config.getDouble("SpawnLocationPitch"));
				this.schematicOffset = config.getVector("schematicOffset");
			}
		}
		catch (IOException | InvalidConfigurationException e)
		{
			e.printStackTrace();
		}
	}

	public void Save(JavaPlugin plugin)
	{
		YamlConfiguration config = new YamlConfiguration();
		try
		{
			config.set("world", middle.getWorld().getName());
			config.set("size", this.size);
			config.set("startAtPlayerCount", this.startAtPlayerCount);
			config.set("maxPlayers", this.maxPlayers);
			config.set("secondsAfterStart", this.secondsAfterStart);
			config.set("spawnLocation", this.spawnLocation.toVector());
			config.set("spawnLocationYaw", this.spawnLocation.getYaw());
			config.set("spawnLocationPitch", this.spawnLocation.getPitch());
			config.set("location", middle.toVector());
			config.set("schematicOffset", this.schematicOffset);
			config.save(configFile);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	abstract void StartWave(int wave, JavaPlugin plugin);

	abstract String getType();

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
		if (minutesPassed != oldMinutesPassed)
		{
			this.Broadcast(minutesPassed + " minutes have passed!");
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

	protected void Broadcast(String message)
	{
		for (Player p : players)
		{
			p.sendMessage("[ZombieInvasion] " + message);
		}
	}

	public boolean isRunning()
	{
		return this.ticksPassed != -1;
	}

	public void onPlayerJoinArena(Player player, JavaPlugin plugin)
	{
		while (players.contains(player))
			players.remove(player);
		players.add(player);
		player.teleport(this.middle);
		player.setMetadata("arena", new FixedMetadataValue(plugin, this.name));
		ZombieInvasion.economyPlugin.ResetStats(player);
		this.Broadcast(player.getName() + " has joined the arena!");

		if (!isRunning())
		{
			if (players.size() >= this.startAtPlayerCount)
			{
				this.Broadcast("Waves are coming in " + this.secondsAfterStart + " seconds!");
				BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
				scheduler.scheduleSyncDelayedTask(plugin, new Runnable()
				{
					@Override
					public void run()
					{
						SendWaves((JavaPlugin) Bukkit.getServer().getPluginManager().getPlugin("ZombieInvasion"));
					}
				}, 20 * this.secondsAfterStart);
			}
		}
	}

	public void onPlayerLeaveArena(Player player, String reason, JavaPlugin plugin)
	{
		this.Broadcast(player.getName() + " has " + reason + "!");
		while (players.contains(player))
			players.remove(player);
		player.removeMetadata("arena", plugin);
		player.teleport(lobby.getLocation());

		if (players.size() <= 0)
		{
			if (this.tickTaskId != -1)
			{
				plugin.getServer().getScheduler().cancelTask(this.tickTaskId);
				this.tickTaskId = -1;
			}
			this.Reset(plugin);
		}

		this.ticksPassed = -1;
		this.ticksSinceLastWave = -1;
		this.oldMinutesPassed = -1;
		player.getInventory().clear();
	}

}
