package io.github.gustav9797.ZombieInvasion;

import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

public class SpawnZombieTask extends BukkitRunnable
{

	private final Location l;
	private final Location middle;
	private final ZombieArena arena;

	public SpawnZombieTask(Location l, Location middle, ZombieArena arena)
	{
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