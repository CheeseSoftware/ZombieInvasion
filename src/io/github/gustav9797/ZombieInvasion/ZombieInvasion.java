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
import net.minecraft.server.v1_7_R1.EntitySkeleton;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import ostkaka34.OstEconomyPlugin.IOstEconomy;
import ostkaka34.OstEconomyPlugin.OstEconomyPlugin;

public final class ZombieInvasion extends JavaPlugin implements Listener
{
	LinkedList<CustomEntityType> entityTypes;
	Random r = new Random();
	Map<String, Arena> arenas;
	Lobby lobby;
	File configFile;
	File schematicsDirectory;
	public static IOstEconomy economyPlugin;

	@Override
	public void onEnable()
	{
		ConfigurationSerialization.registerClass(BorderBlock.class, "BorderBlock");
		this.configFile = new File(this.getDataFolder() + File.separator + "config.yml");
		this.schematicsDirectory = new File(this.getDataFolder() + File.separator + "schematics");
		this.entityTypes = new LinkedList<CustomEntityType>();
		this.entityTypes.add(new CustomEntityType("Zombie", 54, EntityType.ZOMBIE, EntityZombie.class, EntityFastZombie.class));
		this.entityTypes.add(new CustomEntityType("Skeleton", 51, EntityType.SKELETON, EntitySkeleton.class, EntityBlockBreakingSkeleton.class));
		this.registerEntities();
		this.arenas = new HashMap<String, Arena>();
		this.lobby = new Lobby(arenas, this);

		if (!schematicsDirectory.exists())
			schematicsDirectory.mkdir();

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
		for (Arena a : arenas.values())
		{
			for (Player player : a.players)
				player.removeMetadata("arena", this);
			a.Reset();
			a.LoadMap();
		}
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
						Arena a = new ZombieArena(name, this.lobby);
						a.setMiddle(player.getLocation());
						a.setSpawnLocation(player.getLocation());
						a.setSize(96);
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
				if (player.hasMetadata("selectedarena"))
				{
					String name = player.getMetadata("selectedarena").get(0).asString();
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
					sender.sendMessage("You don't have any arena selected.");
				return true;
			}
			else if (cmd.getName().equals("selectarena"))
			{
				if (args.length > 0)
				{
					String name = args[0];
					if (arenas.containsKey(name))
					{
						player.setMetadata("selectedarena", new FixedMetadataValue(this, name));
						sender.sendMessage("Arena selected.");
					}
					else
						sender.sendMessage("Arena doesn't exist.");
				}
				else
					sender.sendMessage("Usage: /selectarena <name>");
				return true;
			}
			else if (cmd.getName().equals("joinarena"))
			{
				if (args.length > 0)
				{
					if (!player.hasMetadata("arena"))
					{
						String name = args[0];
						if (arenas.containsKey(name))
						{
							Arena arena = arenas.get(name);
							arena.JoinPlayer(player);
						}
						else
							sender.sendMessage("Arena doesn't exist.");
					}
					else
						sender.sendMessage("You are already inside an arena! (/leave)");
				}
				else
					sender.sendMessage("Usage: /joinarena <name>");
				return true;
			}
			else if (cmd.getName().equals("leave"))
			{
				if (player.hasMetadata("arena"))
				{
					if (arenas.containsKey(player.getMetadata("arena").get(0).asString()))
					{
						Arena arena = arenas.get(player.getMetadata("arena").get(0).asString());
						arena.RemovePlayer(player, "left the arena");
					}
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
					arena.SendWaves();
					this.getServer().broadcastMessage("Waves are coming! Hide!");
				}
				else
					sender.sendMessage("You have to join an arena! (/joinarena)");
				return true;
			}
			else if (cmd.getName().equals("setlocation"))
			{
				if (player.hasMetadata("selectedarena"))
				{
					String name = player.getMetadata("selectedarena").get(0).asString();
					if (arenas.containsKey(name))
					{
						Arena arena = arenas.get(name);
						arena.setMiddle(player.getLocation());
						sender.sendMessage("Arena middle was set!");
					}
					else
						sender.sendMessage("Arena doesn't exist.");
				}
				else
					sender.sendMessage("You don't have any arena selected.");
				return true;
			}
			else if (cmd.getName().equals("setarenaspawn"))
			{
				if (player.hasMetadata("selectedarena"))
				{
					String name = player.getMetadata("selectedarena").get(0).asString();
					if (arenas.containsKey(name))
					{
						Arena arena = arenas.get(name);
						arena.setSpawnLocation(player.getLocation());
						sender.sendMessage("Arena spawn was set!");
					}
					else
						sender.sendMessage("Arena doesn't exist.");
				}
				else
					sender.sendMessage("You don't have any arena selected.");
				return true;
			}
			else if (cmd.getName().equals("setsize"))
			{
				if (player.hasMetadata("selectedarena"))
				{
					String name = player.getMetadata("selectedarena").get(0).asString();
					if (arenas.containsKey(name))
					{
						Arena arena = arenas.get(name);
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
						sender.sendMessage("Arena doesn't exist.");
				}
				else
					sender.sendMessage("You don't have any arena selected.");
				return true;
			}
			else if (cmd.getName().equals("createborder"))
			{
				if (player.hasMetadata("selectedarena"))
				{
					String name = player.getMetadata("selectedarena").get(0).asString();
					if (arenas.containsKey(name))
					{
						Arena arena = arenas.get(name);
						boolean roof = false;
						int height = 100;
						Material material = Material.GLASS;
						if (args.length >= 1)
						{
							material = Material.getMaterial(args[0]);
							if (material != null)
							{
								if (args.length >= 2)
									height = Integer.parseInt(args[1]);
								if (args.length >= 3)
									roof = Boolean.parseBoolean(args[2]);
								arena.CreateBorder(material, height, roof);
								sender.sendMessage("Border created.");
							}
							else
								sender.sendMessage("Invalid material!");
						}
						else
							sender.sendMessage("Usage: /createborder <string material> <int height> <bool buildroof=true>");
					}
					else
						sender.sendMessage("Arena doesn't exist.");
				}
				else
					sender.sendMessage("You don't have any arena selected.");
				return true;
			}
			else if (cmd.getName().equals("removeborder"))
			{
				if (player.hasMetadata("selectedarena"))
				{
					String name = player.getMetadata("selectedarena").get(0).asString();
					if (arenas.containsKey(name))
					{
						Arena arena = arenas.get(name);
						arena.RestoreBorder();
						sender.sendMessage("Border removed.");
					}
					else
						sender.sendMessage("Arena doesn't exist.");
				}
				else
					sender.sendMessage("You don't have any arena selected.");
				return true;
			}
			else if (cmd.getName().equals("reset"))
			{
				if (player.hasMetadata("selectedarena"))
				{
					String name = player.getMetadata("selectedarena").get(0).asString();
					if (arenas.containsKey(name))
					{
						Arena arena = arenas.get(name);
						arena.LoadMap();
						arena.Reset();
					}
					else
						sender.sendMessage("Arena doesn't exist.");
				}
				else
					sender.sendMessage("You don't have any arena selected.");
				return true;
			}
			else if (cmd.getName().equals("savemap"))
			{
				if (player.hasMetadata("selectedarena"))
				{
					String name = player.getMetadata("selectedarena").get(0).asString();
					if (arenas.containsKey(name))
					{
						Arena arena = arenas.get(name);
						arena.SaveMap();
						sender.sendMessage("Map saved.");
					}
					else
						sender.sendMessage("Arena doesn't exist.");
				}
				else
					sender.sendMessage("You don't have any arena selected.");
				return true;
			}
			else if (cmd.getName().equals("loadmap"))
			{
				if (player.hasMetadata("selectedarena"))
				{
					String name = player.getMetadata("selectedarena").get(0).asString();
					if (arenas.containsKey(name))
					{
						Arena arena = arenas.get(name);
						arena.LoadMap();
						sender.sendMessage("Map loaded.");
					}
					else
						sender.sendMessage("Arena doesn't exist.");
				}
				else
					sender.sendMessage("You don't have any arena selected.");
				return true;
			}
			else if (cmd.getName().equals("clearmap"))
			{
				if (player.hasMetadata("selectedarena"))
				{
					String name = player.getMetadata("selectedarena").get(0).asString();
					if (arenas.containsKey(name))
					{
						Arena arena = arenas.get(name);
						arena.ClearMap();
						sender.sendMessage("Map cleared.");
					}
					else
						sender.sendMessage("Arena doesn't exist.");
				}
				else
					sender.sendMessage("You don't have any arena selected.");
				return true;
			}
			else if (cmd.getName().equals("setlobby"))
			{
				lobby.setLocation(player.getLocation());
				sender.sendMessage("Lobby set!");
				return true;
			}
			else if (cmd.getName().equals("reloadzombieinvasion"))
			{
				this.Reload();
				sender.sendMessage("Reloaded!");
				return true;
			}
		}
		return false;
	}

	public void Save()
	{
		this.SaveConfig();
	}

	public void Load()
	{
		this.LoadConfig();
	}

	public void SaveConfig()
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

	public void LoadConfig()
	{
		YamlConfiguration config = new YamlConfiguration();
		try
		{
			config.load(configFile);
		}
		catch (IOException | InvalidConfigurationException e)
		{
			e.printStackTrace();
		}
		@SuppressWarnings("unchecked")
		List<String> temp = (List<String>) config.getList("zombiearenas");
		for (String arena : temp)
		{
			ZombieArena a = new ZombieArena(arena, lobby);
			arenas.put(arena, a);
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
		for (Arena a : arenas.values())
			a.Load();
	}

	public static ZombieInvasion getPlugin()
	{
		return (ZombieInvasion) Bukkit.getPluginManager().getPlugin("ZombieInvasion");
	}

	public static OstEconomyPlugin getEconomyPlugin()
	{
		return (OstEconomyPlugin) Bukkit.getPluginManager().getPlugin("OstEconomyPlugin");
	}

	public static JavaPlugin getWeaponsPlugin()
	{
		return (JavaPlugin) Bukkit.getPluginManager().getPlugin("WeaponsPlugin");
	}

	@SuppressWarnings("rawtypes")
	private static Object getPrivateStatic(Class clazz, String f) throws Exception
	{
		Field field = clazz.getDeclaredField(f);
		field.setAccessible(true);
		return field.get(null);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onCreatureSpawn(CreatureSpawnEvent event)
	{
		for (Arena a : this.arenas.values())
		{
			if (a instanceof ZombieArena)
			{
				ZombieArena arena = (ZombieArena) a;
				if (!arena.monsters.contains((CraftEntity) event.getEntity()))
				{
					if (a.ContainsPosition(event.getEntity().getLocation().toVector()))
						if (event.getSpawnReason() == SpawnReason.NATURAL)
							event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPlayerQuit(PlayerQuitEvent event)
	{
		lobby.onPlayerQuit(event);
		for (Arena a : arenas.values())
			a.onPlayerQuit(event);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPlayerDeath(PlayerDeathEvent event)
	{
		for (Arena a : arenas.values())
			a.onPlayerDeath(event);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPlayerRespawn(PlayerRespawnEvent event)
	{
		for (Arena a : arenas.values())
			a.onPlayerRespawn(event);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPlayerInteract(PlayerInteractEvent event)
	{
		lobby.onPlayerInteract(event);
		for (Arena a : arenas.values())
			a.onPlayerInteract(event);
		/*if(event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getPlayer().getItemInHand().getType() == Material.MONSTER_EGG)
		{
			SpawnEgg egg = (SpawnEgg) event.getPlayer().getItemInHand().getData();
			if(egg.getSpawnedType() == EntityType.PIG_ZOMBIE)
			{
				EntityBlockBreakingPigman pigman = new EntityBlockBreakingPigman(((CraftWorld)event.getPlayer().getWorld()).getHandle());
				pigman.getBukkitEntity().teleport(event.getClickedBlock().getLocation());
				((CraftWorld)event.getPlayer().getWorld()).getHandle().addEntity(pigman);
			}
		}*/
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onEntityTargetLivingEntity(EntityTargetLivingEntityEvent event)
	{
		for (Arena a : arenas.values())
			a.onEntityTargetLivingEntity(event);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onBlockBreak(BlockBreakEvent event)
	{
		lobby.onBlockBreak(event);
		for (Arena a : arenas.values())
			a.onBlockBreak(event);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	private void onSignChange(SignChangeEvent event)
	{
		lobby.onSignChange(event);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	private void onPlayerJoin(PlayerJoinEvent event)
	{
		lobby.onPlayerJoin(event);
	}

	@EventHandler(priority = EventPriority.HIGH)
	private void onEntityDamageEByntity(EntityDamageByEntityEvent event)
	{
		for (Arena a : arenas.values())
			a.onEntityDamageEByntity(event);
	}
}
