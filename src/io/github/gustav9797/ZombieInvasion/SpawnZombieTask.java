package io.github.gustav9797.ZombieInvasion;

import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

public class SpawnZombieTask extends BukkitRunnable
{

	private final Location l;
	private final ZombieArena arena;

	public SpawnZombieTask(Location l, ZombieArena arena)
	{
		this.l = l;
		this.arena = arena;
	}

	@Override
	public void run()
	{
		arena.SpawnZombie(l);
	}

}