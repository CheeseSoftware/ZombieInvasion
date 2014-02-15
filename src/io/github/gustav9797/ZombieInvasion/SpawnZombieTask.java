package io.github.gustav9797.ZombieInvasion;

import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class SpawnZombieTask extends BukkitRunnable
{

	private final JavaPlugin plugin;
	private final Location l;
	private final Location middle;
	private final ZombieArena arena;

	public SpawnZombieTask(JavaPlugin plugin, Location l, Location middle, ZombieArena arena)
	{
		this.plugin = plugin;
		this.l = l;
		this.middle = middle;
		this.arena = arena;
	}

	@Override
	public void run()
	{
		arena.SpawnZombie(l, middle);
	}

}