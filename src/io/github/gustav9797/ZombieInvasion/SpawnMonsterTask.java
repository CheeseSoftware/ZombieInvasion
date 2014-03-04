package io.github.gustav9797.ZombieInvasion;

import org.bukkit.scheduler.BukkitRunnable;

public class SpawnMonsterTask extends BukkitRunnable
{

	private final MonsterSpawnPoint spawnPoint;
	private final ZombieArena arena;

	public SpawnMonsterTask(MonsterSpawnPoint spawnPoint, ZombieArena arena)
	{
		this.spawnPoint = spawnPoint;
		this.arena = arena;
	}

	@Override
	public void run()
	{
		arena.SpawnZombie(spawnPoint);
	}

}