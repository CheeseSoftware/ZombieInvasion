package io.github.gustav9797.ZombieInvasion;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_7_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class ZombieArena extends Arena
{
	protected List<EntityFastZombie> zombies = new LinkedList<EntityFastZombie>();
	protected List<Location> zombiesToSpawn = new LinkedList<Location>();
	protected int ticksUntilNextWave = -1;
	protected int currentWave = 5;

	public ZombieArena(String name, JavaPlugin plugin)
	{
		super(name, plugin);
	}

	public void SpawnZombie(Location l, Location middle)
	{
		zombiesToSpawn.add(l);
	}

	public void SpawnZombies(Location l, int amount, JavaPlugin plugin)
	{
		int delay = 1;
		for (int i = 0; i < amount; i++)
		{
			Location pos = new Location(l.getWorld(), l.getBlockX(), l.getBlockY(), l.getBlockZ());
			while (pos.getWorld().getBlockAt(pos).getType() == Material.AIR)
				pos.setY(pos.getBlockY() - 1);
			pos.setY(pos.getBlockY() + 2);
			new SpawnZombieTask(pos, this.middle, this).runTaskLater(plugin, delay);
			delay += 20;
		}
	}

	@Override
	public void StartWave(int wave, JavaPlugin plugin)
	{
		for (int i = 0; i < 10; i++)
		{
			int x = 0;
			int z = 0;
			while (!isValidZombieSpawningPosition(x, z))
			{
				x = r.nextInt(size - 4) - this.size / 2 + 2;
				z = r.nextInt(size - 4) - this.size / 2 + 2;
			}
			SpawnZombies(new Location(middle.getWorld(), x + middle.getBlockX(), r.nextInt(size) + middle.getBlockY(), z + middle.getBlockZ()), wave, plugin);

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

	@Override
	public void Load(JavaPlugin plugin)
	{
		super.Load(plugin);
	}

	@Override
	public void Save(JavaPlugin plugin)
	{
		super.Save(plugin);
	}

	@Override
	public void SendWaves(JavaPlugin plugin)
	{
		zombies.clear();
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.scheduleSyncRepeatingTask(plugin, new Runnable()
		{
			@Override
			public void run()
			{
				TrySpawnZombies();
			}
			// Do something
		}, 0L, 5L);
		super.SendWaves(plugin);
	}

	public void TrySpawnZombies()
	{
		Iterator<Location> i = zombiesToSpawn.iterator();
		while (i.hasNext() && zombies.size() < 100)
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

	@Override
	public void Tick(JavaPlugin plugin)
	{
		super.Tick(plugin);
		if (this.currentWave >= 200)
		{
			for (Player player : players)
			{
				player.sendMessage("Waves finished, you won!");
				Bukkit.getServer().getScheduler().cancelTask(this.tickTaskId);
			}
		}

		if (this.ticksUntilNextWave != -1)
		{
			this.ticksUntilNextWave -= 100;
			if (this.ticksUntilNextWave <= 0)
			{
				this.ticksSinceLastWave = 0;
				this.StartWave(this.currentWave + 5, plugin);
				this.ticksUntilNextWave = -1;
				for (Player player : players)
				{
					player.sendMessage("Wave " + (int) (currentWave + 5) + " is coming!");
				}
				this.currentWave += 5;

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
			for (Player player : players)
			{
				player.sendMessage("Below 20 zombies left, prepare for the next wave in 30 seconds!");
			}
		}
	}
}
