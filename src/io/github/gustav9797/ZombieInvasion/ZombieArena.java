package io.github.gustav9797.ZombieInvasion;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_7_R1.CraftWorld;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class ZombieArena extends Arena
{

	public ZombieArena(int size, Location middle)
	{
		super(size, middle);
	}

	public static void SpawnZombie(Location l, Location middle)
	{
		net.minecraft.server.v1_7_R1.World mcWorld = ((CraftWorld) l.getWorld()).getHandle();
		EntityFastZombie zombie = new EntityFastZombie(mcWorld, middle);
		zombie.setPosition(l.getBlockX(), l.getBlockY(), l.getBlockZ());
		mcWorld.addEntity(zombie);
	}

	public void SpawnZombies(Location l, int amount, JavaPlugin plugin)
	{
		int delay = 1;
		for (int i = 0; i < amount; i++)
		{
			Location pos = new Location(l.getWorld(), r.nextInt(11) - 5 + l.getBlockX(), l.getBlockY(), r.nextInt(11) - 5 + l.getBlockZ());
			while (pos.getWorld().getBlockAt(pos).getType() == Material.AIR)
				pos.setY(pos.getBlockY() - 1);
			pos.setY(pos.getBlockY() + 2);
			BukkitTask task = new SpawnZombieTask(plugin, pos, this.middle).runTaskLater(plugin, delay);
			delay += 20;
		}
	}

	@Override
	public void StartWave(int wave, JavaPlugin plugin)
	{
		for (int i = 0; i < 10; i++)
		{
			SpawnZombies(new Location(middle.getWorld(), r.nextInt(size) + middle.getBlockX() - 64, r.nextInt(size) + middle.getBlockY(), r.nextInt(size) + middle.getBlockZ() - 64), wave, plugin);

		}
	}
}
