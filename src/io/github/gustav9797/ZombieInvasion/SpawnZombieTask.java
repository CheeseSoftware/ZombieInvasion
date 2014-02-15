package io.github.gustav9797.ZombieInvasion;

import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class SpawnZombieTask extends BukkitRunnable
{

	private final JavaPlugin plugin;
	private final Location l;
	private final Location middle;

	public SpawnZombieTask(JavaPlugin plugin, Location l, Location middle)
	{
		this.plugin = plugin;
		this.l = l;
		this.middle = middle;
	}

	@Override
	public void run()
	{
		ZombieArena.SpawnZombie(l, middle);
	}

}