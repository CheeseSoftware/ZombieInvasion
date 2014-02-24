package io.github.gustav9797.ZombieInvasion;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.schematic.SchematicFormat;

public abstract class Arena implements Listener
{
	protected int size;
	protected Location middle;
	protected String schematicFileName;
	protected Location spawnLocation;
	protected Random r = new Random();
	protected String name;
	protected Material borderMaterial;
	protected int sendWavesTaskId = -1;
	protected int tickTaskId = -1;
	protected int ticksPassed = -1;
	protected int oldMinutesPassed = -1;
	protected int ticksUntilNextWave = -1;
	protected int ticksSinceLastWave = -1;
	protected int maxPlayers = 10;
	protected int startAtPlayerCount = 1;
	protected int secondsAfterStart = 20;

	public List<Player> players = new ArrayList<Player>();
	public List<Player> spectators = new ArrayList<Player>();
	public Map<Player, ItemStack[]> spectatorInventories = new HashMap<Player, ItemStack[]>();
	public ArenaScoreboard scoreboard;
	protected ArrayList<BorderBlock> border;
	protected Lobby lobby;
	protected YamlConfiguration config;
	protected File configFile;
	protected File borderConfigFile;
	protected File directory;

	public Arena(String name, Lobby lobby)
	{
		this.lobby = lobby;
		this.name = name;
		this.schematicFileName = name;
		this.border = new ArrayList<BorderBlock>();
		this.middle = new Location(Bukkit.getServer().getWorlds().get(0), 0, 0, 0);
		this.spawnLocation = middle;
		this.directory = new File(ZombieInvasion.getPlugin().getDataFolder() + File.separator + name);
		if (!directory.exists())
			directory.mkdir();
		this.configFile = new File(ZombieInvasion.getPlugin().getDataFolder() + File.separator + name + File.separator + "config.yml");
		this.borderConfigFile = new File(ZombieInvasion.getPlugin().getDataFolder() + File.separator + name + File.separator + "border.yml");

		if (!borderConfigFile.exists())
		{
			try
			{
				borderConfigFile.createNewFile();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			this.SaveBorderConfig();
		}
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
			this.SaveConfig();
		}
		this.Load();
		this.scoreboard = new ArenaScoreboard(this);
	}

	abstract void SendWave(int wave);

	protected void Broadcast(String message)
	{
		for (Player p : players)
		{
			p.sendMessage("[ZombieInvasion] " + message);
		}
	}

	public void setSchematic(String name)
	{
		this.schematicFileName = name;
	}

	public void SaveMap()
	{
		int topY = 0;
		if (!border.isEmpty())
		{
			for (BorderBlock borderBlock : border)
				if (borderBlock.getLocation().getBlockY() > topY)
					topY = borderBlock.getLocation().getBlockY();
			topY--;
		}
		else
			topY = 100;
		int groundLevel = 4;
		EditSession session = new EditSession(new BukkitWorld(middle.getWorld()), 999999999);
		File schematic = new File(ZombieInvasion.getPlugin().getDataFolder() + File.separator + "schematics" + File.separator + this.schematicFileName + ".schematic");
		CuboidClipboard clipboard = new CuboidClipboard(new com.sk89q.worldedit.Vector(this.getSize() - 1, topY, this.getSize() - 1), new com.sk89q.worldedit.Vector(middle.getBlockX() - getRadius()
				+ 1, groundLevel, middle.getBlockZ() - getRadius() + 1));
		clipboard.copy(session);
		try
		{
			SchematicFormat.MCEDIT.save(clipboard, schematic);
		}
		catch (IOException | DataException e)
		{
			e.printStackTrace();
		}
	}

	@SuppressWarnings("deprecation")
	public void LoadMap()
	{
		EditSession es = new EditSession(new BukkitWorld(middle.getWorld()), 999999999);
		File schematic = new File(ZombieInvasion.getPlugin().getDataFolder() + File.separator + "schematics" + File.separator + this.schematicFileName + ".schematic");
		int groundLevel = 4;
		if (schematic.exists())
		{
			try
			{
				CuboidClipboard cc = CuboidClipboard.loadSchematic(schematic);
				com.sk89q.worldedit.Vector location = new com.sk89q.worldedit.Vector(this.middle.getBlockX() - getRadius() + 1, groundLevel, this.middle.getBlockZ() - getRadius() + 1);
				cc.paste(es, location, false);
			}
			catch (MaxChangedBlocksException | DataException | IOException e)
			{
				e.printStackTrace();
			}
		}
		else
			Bukkit.getLogger().warning("[ZombieInvasion] Schematic file for arena " + this.name + " was not found! This will cause the arena to not get reset properly.");
	}

	public void ClearMap()
	{
		int topY = 0;
		if (!border.isEmpty())
		{
			for (BorderBlock borderBlock : border)
				if (borderBlock.getLocation().getBlockY() > topY)
					topY = borderBlock.getLocation().getBlockY();
			topY--;
		}
		else
			topY = 100;
		int groundLevel = 4;
		EditSession es = new EditSession(new BukkitWorld(middle.getWorld()), 999999999);
		CuboidRegion region = new CuboidRegion(new com.sk89q.worldedit.Vector(middle.getBlockX() - getRadius() + 1, groundLevel, middle.getBlockZ() - getRadius() + 1), new com.sk89q.worldedit.Vector(
				middle.getBlockX() + getRadius() - 1, topY, middle.getBlockZ() + getRadius() - 1));
		try
		{
			es.setBlocks(region, new BaseBlock(0));
		}
		catch (MaxChangedBlocksException e)
		{
			e.printStackTrace();
		}
	}

	public void ResetSpectators()
	{
		List<Player> tempspectators = new ArrayList<Player>(this.spectators);
		for (Player player : tempspectators)
		{
			this.RemoveSpectator(player);
			player.teleport(spawnLocation);
		}
		spectators.clear();
		tempspectators.clear();
	}

	private void CheckSpectators()
	{
		if (this.spectators.size() >= this.players.size())
		{
			Broadcast("Everyone have died. Reseting arena...");
			this.Reset();
			this.LoadMap();
			this.TryStart();
		}
	}

	@SuppressWarnings("deprecation")
	public void MakeSpectator(Player player)
	{
		if (!spectators.contains(player))
			spectators.add(player);
		this.spectatorInventories.put(player, player.getInventory().getContents());
		player.getInventory().clear();
		player.updateInventory();
		player.setGameMode(GameMode.ADVENTURE);
		player.setAllowFlight(true);
		player.setFlying(true);
		for (Player p : players)
		{
			p.hidePlayer(player);
		}
		player.teleport(this.spawnLocation);
		CheckSpectators();
		player.sendMessage("[ZombieInvasion] You are now a spectator.");
	}

	@SuppressWarnings("deprecation")
	public void RemoveSpectator(Player player)
	{
		while (spectators.contains(player))
			spectators.remove(player);
		for (Player p : players)
		{
			p.showPlayer(player);
		}
		if (spectatorInventories.containsKey(player))
		{
			ItemStack[] oldContents = spectatorInventories.get(player);
			player.getInventory().setContents(oldContents);
			player.updateInventory();
			spectatorInventories.remove(player);
		}
		player.setGameMode(GameMode.SURVIVAL);
		player.setFlying(false);
		player.setAllowFlight(false);
	}

	public boolean isSpectator(Player player)
	{
		return this.spectators.contains(player);
	}

	public void SetAlive(Player player)
	{
		this.RemoveSpectator(player);
		player.setHealth((double) 20);
		player.setFoodLevel(20);
		player.teleport(this.spawnLocation);
		player.sendMessage("[ZombieInvasion] You are now alive again!");

	}

	public void RespawnPlayers()
	{
		for (Player player : this.players)
		{
			player.teleport(spawnLocation);
		}
	}

	public void Reset()
	{
		if (this.tickTaskId != -1)
		{
			Bukkit.getServer().getScheduler().cancelTask(this.tickTaskId);
			this.tickTaskId = -1;
		}
		if (this.sendWavesTaskId != -1)
		{
			Bukkit.getServer().getScheduler().cancelTask(sendWavesTaskId);
			sendWavesTaskId = -1;
		}
		for (Player player : players)
		{
			player.teleport(this.spawnLocation);
		}
		this.ticksPassed = -1;
		this.ticksSinceLastWave = -1;
		this.oldMinutesPassed = -1;
		this.ResetSpectators();
		this.RespawnPlayers();

		List<Entity> entList = this.middle.getWorld().getEntities();
		for (Entity entity : entList)
		{
			if (entity instanceof Item)
			{
				Item item = (Item) entity;
				if (this.ContainsLocation(item.getLocation()))
					item.remove();
			}
		}

		this.Broadcast("Arena was reset!");
	}

	public boolean ContainsLocation(Location location)
	{
		if (location.getWorld().getName().equals(this.middle.getWorld().getName()))
		{
			if (location.getBlockX() >= (-getRadius() + this.middle.getBlockX()) && location.getBlockX() <= (getRadius() + this.middle.getBlockX()))
			{
				if (location.getBlockZ() >= (-getRadius() + this.middle.getBlockZ()) && location.getBlockZ() <= (getRadius() + this.middle.getBlockZ()))
				{
					return true;
				}
			}
		}
		return false;
	}

	public void Save()
	{
		this.SaveConfig();
		this.SaveBorderConfig();
	}

	public void Load()
	{
		this.LoadConfig();
		this.LoadBorderConfig();
	}

	protected void SaveConfig()
	{
		config = new YamlConfiguration();
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
			config.set("schematicFileName", this.schematicFileName);
			config.save(configFile);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	protected void LoadConfig()
	{
		config = new YamlConfiguration();
		try
		{
			config.load(configFile);
			String world = config.getString("world");
			if (world != null && ZombieInvasion.getPlugin().getServer().getWorld(world) != null && config.getVector("location") != null)
			{
				this.size = config.getInt("size");
				this.startAtPlayerCount = config.getInt("startAtPlayerCount");
				this.maxPlayers = config.getInt("maxPlayers");
				this.secondsAfterStart = config.getInt("secondsAfterStart");
				this.middle = config.getVector("location").toLocation(ZombieInvasion.getPlugin().getServer().getWorld(world));
				Vector spawnPos = config.getVector("spawnLocation");
				this.spawnLocation = spawnPos.toLocation(ZombieInvasion.getPlugin().getServer().getWorld(world), (float) config.getDouble("spawnLocationYaw"),
						(float) config.getDouble("SpawnLocationPitch"));
				this.schematicFileName = config.getString("schematicFileName");
			}
		}
		catch (IOException | InvalidConfigurationException e)
		{
			e.printStackTrace();
		}
	}

	protected void SaveBorderConfig()
	{
		config = new YamlConfiguration();
		try
		{
			config.set("border", this.border);
			config.save(borderConfigFile);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	protected void LoadBorderConfig()
	{
		config = new YamlConfiguration();
		try
		{
			config.load(borderConfigFile);
			ArrayList<BorderBlock> temp = (ArrayList<BorderBlock>) config.getList("border");
			if (temp == null)
				this.border = new ArrayList<BorderBlock>();
			else
				this.border = new ArrayList<BorderBlock>(temp);
		}
		catch (IOException | InvalidConfigurationException e)
		{
			e.printStackTrace();
		}
	}

	public void SendWaves()
	{
		if (this.tickTaskId != -1)
			Bukkit.getScheduler().cancelTask(tickTaskId);
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		this.tickTaskId = scheduler.scheduleSyncRepeatingTask(ZombieInvasion.getPlugin(), new Runnable()
		{
			@Override
			public void run()
			{
				Tick();
			}
		}, 0L, 1L);
	}

	protected void Tick()
	{
		this.ticksSinceLastWave += 1;
		ticksPassed += 1;
		int minutesPassed = Math.round(ticksPassed / 20 / 60);
		if (minutesPassed != oldMinutesPassed)
		{
			this.Broadcast(minutesPassed + " minutes have passed!");
			oldMinutesPassed = minutesPassed;
		}
		if (this.sendWavesTaskId != -1)
			this.ticksUntilNextWave += 1;
		else
			this.ticksUntilNextWave = 0;
	}

	public void CreateBorder(Material material, int height, boolean buildRoof)
	{
		@SuppressWarnings("deprecation")
		List<Material> replacableMaterials = new ArrayList<Material>(Arrays.asList(Material.AIR, Material.WATER, Material.getMaterial(8), Material.getMaterial(9), Material.LAVA, material));
		if (!border.isEmpty())
			RestoreBorder();

		int radius = size / 2;
		BlockState originalBlock = null;
		World world = middle.getWorld();

		for (int y = 0; y <= height; y++)
		{
			for (int x = -radius; x <= radius; x++)
			{
				for (int z = -radius; z <= radius; z++)
				{
					if (x == -radius || z == -radius || x == radius || z == radius)
					{
						originalBlock = world.getBlockAt(x + middle.getBlockX(), y, z + middle.getBlockZ()).getState();
						if (replacableMaterials.contains(originalBlock.getType()))
						{
							BorderBlock block = new BorderBlock(originalBlock.getLocation().toVector(), material, originalBlock.getType());
							while(this.border.contains(block))
								this.border.remove(block);
							this.border.add(block);
							world.getBlockAt(x + middle.getBlockX(), y, z + middle.getBlockZ()).setType(material);
						}
					}
				}
			}
		}

		if (buildRoof)
		{
			for (int x = -radius + 1; x < radius; x++)
			{
				for (int z = -radius + 1; z < radius; z++)
				{
					originalBlock = world.getBlockAt(x + middle.getBlockX(), height, z + middle.getBlockZ()).getState();
					this.border.add(new BorderBlock(new Vector(x + middle.getBlockX(), height, z + middle.getBlockZ()), material, originalBlock.getType()));
					world.getBlockAt(x + middle.getBlockX(), height, z + middle.getBlockZ()).setType(material);
				}
			}
		}
		this.SaveBorderConfig();
	}

	public void RestoreBorder()
	{
		for (BorderBlock block : border)
		{
			this.middle.getWorld().getBlockAt(block.getLocation().toLocation(this.middle.getWorld())).setType(block.getReplacedBlockType());
		}
		border.clear();
		this.SaveBorderConfig();
	}

	public void setSize(int size)
	{
		this.size = size;
		SaveConfig();
	}

	public int getSize()
	{
		return this.size;
	}

	public int getRadius()
	{
		return this.size / 2;
	}

	public int getTicksUntilNextWave()
	{
		return this.ticksUntilNextWave;
	}

	public int getTotalGameTicks()
	{
		return this.ticksPassed == -1 ? 0 : this.ticksPassed;
	}

	public void setMiddle(Location middle)
	{
		this.middle = middle;
		this.SaveConfig();
	}

	public Location getMiddle()
	{
		return this.middle;
	}

	public void setSpawnLocation(Location location)
	{
		this.spawnLocation = location;
		this.SaveConfig();
	}

	public Location getSpawnLocation()
	{
		return this.spawnLocation;
	}

	public boolean isBorder(Vector position)
	{
		for (BorderBlock block : this.border)
		{
			Vector loc = block.getLocation();
			if (loc.getBlockX() == position.getBlockX() && loc.getBlockY() == position.getBlockY() && loc.getBlockZ() == position.getBlockZ())
				return true;
		}
		return false;
	}

	public boolean isRunning()
	{
		return this.ticksPassed != -1;
	}

	public boolean isStarting()
	{
		return this.sendWavesTaskId != -1;
	}

	public void TryStart()
	{
		if (!isRunning() && !isStarting())
		{
			if (players.size() >= this.startAtPlayerCount)
			{
				this.Broadcast("Waves are coming in " + this.secondsAfterStart + " seconds!");
				if (this.sendWavesTaskId != -1)
					Bukkit.getServer().getScheduler().cancelTask(this.sendWavesTaskId);
				BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
				this.sendWavesTaskId = scheduler.scheduleSyncDelayedTask(ZombieInvasion.getPlugin(), new Runnable()
				{
					@Override
					public void run()
					{
						SendWaves();
						Bukkit.getScheduler().cancelTask(sendWavesTaskId);
						sendWavesTaskId = -1;
					}
				}, 20 * this.secondsAfterStart);
			}
		}
	}

	public void JoinPlayer(Player player)
	{
		while (players.contains(player))
			players.remove(player);
		player.setMetadata("arena", new FixedMetadataValue(ZombieInvasion.getPlugin(), this.name));
		ZombieInvasion.getEconomyPlugin().ResetStats(player);
		players.add(player);
		player.setGameMode(GameMode.SURVIVAL);
		player.setHealth((double) 20);
		player.setFoodLevel(20);
		lobby.UpdateSigns();
		scoreboard.AddPlayerScoreboard(player);
		if (this.isRunning())
		{
			this.MakeSpectator(player);
		}
		else
		{
			player.teleport(this.spawnLocation);
			this.Broadcast(player.getName() + " has joined the arena!");
			TryStart();
		}
	}

	public void RemovePlayer(Player player, String reason)
	{
		while (players.contains(player))
			players.remove(player);
		if (players.size() <= 0)
		{
			this.Reset();
			this.LoadMap();
		}
		RemoveSpectator(player);
		player.removeMetadata("arena", ZombieInvasion.getPlugin());
		player.teleport(lobby.getLocation());
		player.getInventory().clear();
		scoreboard.RemovePlayerScoreboard(player);
		lobby.UpdateSigns();
		this.Broadcast(player.getName() + " has " + reason + "!");
	}

	public void onPlayerQuit(PlayerQuitEvent event)
	{
		if (players.contains(event.getPlayer()))
		{
			RemovePlayer(event.getPlayer(), "quit");
		}
	}

	public void onPlayerDeath(PlayerDeathEvent event)
	{
		Player player = event.getEntity();
		if (players.contains(player))
		{
			if (this.isRunning() && !this.isStarting())
				this.MakeSpectator(player);
			else if (!this.isRunning())
				this.SetAlive(player);
		}
	}

	public void onPlayerRespawn(PlayerRespawnEvent event)
	{
		Player player = event.getPlayer();
		if (players.contains(player))
		{
			event.setRespawnLocation(this.spawnLocation);
		}
	}

	public void onPlayerInteract(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		if (spectators.contains(player))
		{
			event.setCancelled(true);
		}
	}

	public void onEntityTargetLivingEntity(EntityTargetLivingEntityEvent event)
	{
		if (event.getTarget() instanceof Player)
		{
			Player target = (Player) event.getTarget();
			if (this.isSpectator(target))
				event.setCancelled(true);
		}
	}

	public void onBlockBreak(BlockBreakEvent event)
	{
		if (this.players.contains(event.getPlayer()))
		{
			if (this.isBorder(event.getBlock().getLocation().toVector()))
			{
				event.setCancelled(true);
				event.getPlayer().sendMessage("Don't try to escape. You are ment to die with the monsters.");
			}
		}
	}

	public void onEntityDamageByEntity(EntityDamageByEntityEvent event)
	{
		if (event.getCause() == DamageCause.ENTITY_ATTACK)
		{
			if (event.getDamager() instanceof Player)
			{
				Player player = (Player) event.getDamager();
				if (spectators.contains(player))
				{
					event.setCancelled(true);
				}
			}
		}
	}

	public void onPlayerPickupItem(PlayerPickupItemEvent event)
	{
		if (spectators.contains(event.getPlayer()))
			event.setCancelled(true);
	}

	public void onPlayerDropItem(PlayerDropItemEvent event)
	{
		if (spectators.contains(event.getPlayer()))
			event.setCancelled(true);
	}

}
