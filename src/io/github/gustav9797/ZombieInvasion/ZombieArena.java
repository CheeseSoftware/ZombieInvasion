package io.github.gustav9797.ZombieInvasion;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_7_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class ZombieArena extends Arena
{
	protected List<EntityFastZombie> zombies = new LinkedList<EntityFastZombie>();
	protected List<Location> zombiesToSpawn = new LinkedList<Location>();
	protected int currentWave = 0;
	protected int ticksUntilNextWave = -1;
	protected int sendWavesTaskId = -1;

	protected int zombieGroups = 5;
	protected int zombieStartAmount = 20;
	protected int zombieAmountIncrease = 10;
	protected int maxZombieAmount = 200;
	protected int startWave = 1;
	protected int waveIncrease = 1;
	protected int ticksBetweenZombieSpawns = 20;

	public ZombieArena(String name, JavaPlugin plugin, Lobby lobby)
	{
		super(name, plugin, lobby);
	}

	public void SpawnZombie(Location l)
	{
		zombiesToSpawn.add(l);
	}

	public void SpawnZombieGroup(Location l, int amount, JavaPlugin plugin)
	{
		int delay = 1;
		for (int i = 0; i < amount; i++)
		{
			Location pos = new Location(l.getWorld(), l.getBlockX(), l.getBlockY(), l.getBlockZ());
			while (pos.getWorld().getBlockAt(pos).getType() == Material.AIR)
				pos.setY(pos.getBlockY() - 1);
			pos.setY(pos.getBlockY() + 2);
			new SpawnZombieTask(pos, this).runTaskLater(plugin, delay);
			delay += this.ticksBetweenZombieSpawns;
		}
	}

	public void onSpawnZombieTick()
	{
		Iterator<Location> i = zombiesToSpawn.iterator();
		while (i.hasNext() && zombies.size() < 200)
		{
			Location l = i.next();
			net.minecraft.server.v1_7_R1.World mcWorld = ((CraftWorld) l.getWorld()).getHandle();
			EntityFastZombie zombie = new EntityFastZombie(mcWorld, middle);
			zombie.setPosition(l.getBlockX(), l.getBlockY(), l.getBlockZ());
			mcWorld.addEntity(zombie);
			zombies.add(zombie);
			i.remove();
		}
	}

	private boolean isValidZombieSpawningPosition(int x, int z)
	{
		if (x > ((this.size / 2 * -1) + 5) && x < (this.size / 2) - 10)
		{
			if (z > ((this.size / 2 * -1) + 5) && z < (this.size / 2) - 10)
				return false;
		}
		return true;
	}

	private int getZombieSpawnAmount(int wave)
	{
		int amount = this.zombieStartAmount;
		int increase = this.zombieAmountIncrease;
		for (int i = 1; i <= wave; i++)
		{
			amount += increase;
			increase++;
		}
		return amount;
	}

	@Override
	public void SendWave(int wave, JavaPlugin plugin)
	{
		for (int i = 0; i < zombieGroups; i++)
		{
			int x = Integer.MAX_VALUE;
			int z = Integer.MAX_VALUE;
			while (x == Integer.MAX_VALUE || !isValidZombieSpawningPosition(x, z))
			{
				x = r.nextInt(size - 4) - this.size / 2 + 2;
				z = r.nextInt(size - 4) - this.size / 2 + 2;
			}
			Location groupLocation = new Location(middle.getWorld(), x + middle.getBlockX(), r.nextInt(size) + middle.getBlockY(), z + middle.getBlockZ());
			SpawnZombieGroup(groupLocation, getZombieSpawnAmount(wave) / zombieGroups, plugin);
		}
	}

	@Override
	public void SendWaves(JavaPlugin plugin)
	{
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		this.sendWavesTaskId = scheduler.scheduleSyncRepeatingTask(plugin, new Runnable()
		{
			@Override
			public void run()
			{
				onSpawnZombieTick();
			}
		}, 0L, 5L);
		super.SendWaves(plugin);
		this.SendWave(this.startWave, plugin);
	}

	@Override
	public void Reset()
	{
		super.Reset();
		if (this.sendWavesTaskId != -1)
		{
			Bukkit.getServer().getScheduler().cancelTask(this.sendWavesTaskId);
			this.sendWavesTaskId = -1;
		}
		for(EntityFastZombie zombie : zombies)
		{
			if(zombie.isAlive())
				zombie.die();
		}
		zombies.clear();
	}

	@Override
	public void Load(JavaPlugin plugin)
	{
		super.Load(plugin);
		config = new YamlConfiguration();
		try
		{
			config.load(configFile);
			this.zombieGroups = config.getInt("zombieGroups");
			this.zombieStartAmount = config.getInt("zombieStartAmount");
			this.zombieAmountIncrease = config.getInt("zombieAmountIncrease");
			this.maxZombieAmount = config.getInt("maxZombieAmount");
			this.startWave = config.getInt("startWave");
			this.waveIncrease = config.getInt("waveIncrease");
			this.ticksBetweenZombieSpawns = config.getInt("ticksBetweenZombieSpawns");
		}
		catch (IOException | InvalidConfigurationException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void Save(JavaPlugin plugin)
	{
		super.Save(plugin);
		try
		{
			config.set("zombieGroups", this.zombieGroups);
			config.set("zombieStartAmount", this.zombieStartAmount);
			config.set("zombieAmountIncrease", this.zombieAmountIncrease);
			config.set("maxZombieAmount", this.maxZombieAmount);
			config.set("startWave", this.startWave);
			config.set("waveIncrease", this.waveIncrease);
			config.set("ticksBetweenZombieSpawns", this.ticksBetweenZombieSpawns);
			config.save(configFile);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void Tick(JavaPlugin plugin)
	{
		super.Tick(plugin);
		if (this.currentWave >= 200)
			this.Reset();

		if (this.ticksUntilNextWave != -1)
		{
			this.ticksUntilNextWave -= 100;
			if (this.ticksUntilNextWave <= 0)
			{
				this.ticksSinceLastWave = 0;
				this.currentWave += this.waveIncrease;
				this.ResetSpectators();
				this.ticksUntilNextWave = -1;
				this.SendWave(this.currentWave, plugin);
				this.Broadcast("Wave " + (int) (currentWave) + " is coming!");

			}
		}

		Iterator<EntityFastZombie> i = zombies.iterator();
		while (i.hasNext())
		{
			EntityFastZombie zombie = i.next();
			if (!zombie.isAlive())
			{
				i.remove();
			}
		}

		if (this.zombies.size() <= 20 && this.ticksSinceLastWave >= 20 * 20 && this.ticksUntilNextWave == -1)
		{
			this.ticksUntilNextWave = 30 * 20;
			this.ResetSpectators();
			this.Broadcast("Below 20 zombies left, prepare for the next wave in 30 seconds!");
		}
	}

	@Override
	public void onPlayerLeaveArena(Player player, String reason, JavaPlugin plugin)
	{
		super.onPlayerLeaveArena(player, reason, plugin);
		if (this.players.size() <= 0)
		{
			this.Reset();
		}
	}

	@Override
	String getType()
	{
		return "ZombieArena";
	}
}
