package io.github.gustav9797.ZombieInvasion;

import io.github.gustav9797.ZombieInvasion.Entity.EntityBlockBreakingSkeleton;
import io.github.gustav9797.ZombieInvasion.Entity.EntityBlockBreakingVillager;
import io.github.gustav9797.ZombieInvasion.Entity.EntityBlockBreakingZombie;
import io.github.gustav9797.ZombieInvasion.Entity.ICustomMonster;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.server.v1_7_R1.EntityCreature;
import net.minecraft.server.v1_7_R1.EntityPlayer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_7_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

public class ZombieArena extends Arena
{
	protected Map<UUID, EntityCreature> monsters = new HashMap<UUID, EntityCreature>();
	protected List<SpawnPoint> monstersToSpawn = new ArrayList<SpawnPoint>();
	protected SpawnPointManager spawnPointManager;
	protected int currentWave = 0;
	protected int ticksUntilNextWave = -1;
	protected int sendWavesTaskId = -1;
	protected int monsterSpawnLimit = 130;

	protected int zombieGroups = 10;
	protected int zombieStartAmount = 20;
	protected int zombieAmountIncrease = 10;
	protected int maxZombieAmount = 200;
	protected int startWave = 1;
	protected int waveIncrease = 1;
	protected int ticksBetweenZombieSpawns = 20;

	public ZombieArena(String name, Lobby lobby)
	{
		super(name, lobby);
		spawnPointManager = new SpawnPointManager(this);
	}

	@Override
	public void Load()
	{
		super.Load();
		this.spawnPointManager.Load();
	}

	@Override
	public void Save()
	{
		super.Save();
		this.spawnPointManager.Save();
	}

	public SpawnPointManager getSpawnPointManager()
	{
		return this.spawnPointManager;
	}

	public void SpawnMonster(SpawnPoint spawnPoint)
	{
		monstersToSpawn.add(spawnPoint);
	}

	public void SpawnMonsterGroup(SpawnPoint spawnPoint, int amount)
	{
		int delay = 1;
		Vector spawnPosition = spawnPoint.getPosition();
		
		for (int i = 0; i < amount; i++)
		{
			while (this.middle.getWorld().getBlockAt(spawnPoint.getPosition().toLocation(this.middle.getWorld())).getType() == Material.AIR)
				spawnPosition.setY(spawnPosition.getBlockY() - 1);
			
			spawnPosition.setY(spawnPosition.getBlockY() + 2);
			
			new SpawnMonsterTask(spawnPoint, this).runTaskLater(ZombieInvasion.getPlugin(), delay);
			
			delay += this.ticksBetweenZombieSpawns;
		}
	}

	public void onSpawnZombieTick()
	{
		Iterator<SpawnPoint> i = monstersToSpawn.iterator();
		while (i.hasNext() && monsters.size() < this.monsterSpawnLimit)
		{
			SpawnPoint spawnPoint = i.next();
			net.minecraft.server.v1_7_R1.World mcWorld = ((CraftWorld) this.middle.getWorld()).getHandle();
			EntityCreature monster = null;

			EntityType entityType = spawnPoint.getRandomEntityType();
			if (entityType != null)
			{
				switch (entityType)
				{
					case SKELETON:
						monster = new EntityBlockBreakingSkeleton(mcWorld);
						break;
					case ZOMBIE:
						if (this.getCurrentWave() < 10)
							monster = new EntityBlockBreakingZombie(mcWorld);
						break;
					case VILLAGER:
						monster = new EntityBlockBreakingVillager(mcWorld);
						break;
					default:
						break;
				}

				if (monster != null)
				{
					((ICustomMonster)monster).setArena(this);
					double xd = r.nextDouble()/10;
					double zd = r.nextDouble()/10;
					
					Location l = new Location(this.middle.getWorld(),
							spawnPoint.getPosition().getBlockX() + xd,
							spawnPoint.getPosition().getBlockY(),
							spawnPoint.getPosition().getBlockZ() + zd);
					
					monster.getBukkitEntity().teleport(l);
					monsters.put(monster.getBukkitEntity().getUniqueId(), monster);
					mcWorld.addEntity(monster, SpawnReason.CUSTOM);
				}
			}
			i.remove();
			break;
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
		int virtualWave = wave%10;
		
		amount += virtualWave*(virtualWave - 1)/2;
		// ^^ Siffertriangel, -1 betyder att det startar på 0, /2 betyder att det är en triangel.
		
		amount += virtualWave*increase;
		// ^^ det ökar också med "increase" på varje wave

		return amount;
	}

	public int getCurrentWave()
	{
		return this.currentWave;
	}

	@Override
	public void SendWave(int wave)
	{
		int spawnPointsSize = this.getSpawnPointManager().getSpawnPoints().size();
		for (int i = 0; i < zombieGroups; i++)
		{
			if (spawnPointsSize > 0)
				SpawnMonsterGroup(this.getSpawnPointManager().getRandomMonsterSpawnPoint(), getZombieSpawnAmount(wave) / zombieGroups);
			else
			{
				int x = Integer.MAX_VALUE;
				int z = Integer.MAX_VALUE;
				while (x == Integer.MAX_VALUE || !isValidZombieSpawningPosition(x, z))
				{
					x = r.nextInt(size - 4) - this.size / 2 + 2;
					z = r.nextInt(size - 4) - this.size / 2 + 2;
				}
				Location groupLocation = new Location(middle.getWorld(), x + middle.getBlockX(), r.nextInt(size) + middle.getBlockY(), z + middle.getBlockZ());
				SpawnPoint s = new SpawnPoint(-1, groupLocation.toVector());
				for (EntityType e : EntityType.values())
					s.AddEntityType(e, 1);
				SpawnMonsterGroup(s, getZombieSpawnAmount(wave) / zombieGroups);

			}
		}
	}

	@Override
	public void SendWaves()
	{
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		this.sendWavesTaskId = scheduler.scheduleSyncRepeatingTask(ZombieInvasion.getPlugin(), new Runnable()
		{
			@Override
			public void run()
			{
				onSpawnZombieTick();
			}
		}, 0L, 5L);
		super.SendWaves();
		this.SendWave(this.startWave);
	}

	@Override
	public void Reset()
	{
		super.Reset();
		this.currentWave = 0;
		if (this.sendWavesTaskId != -1)
		{
			Bukkit.getServer().getScheduler().cancelTask(this.sendWavesTaskId);
			this.sendWavesTaskId = -1;
		}
		for (EntityCreature monster : monsters.values())
		{
			if (monster.isAlive())
				monster.die();
		}
		monsters.clear();
	}

	@Override
	protected void LoadConfig()
	{
		super.LoadConfig();
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
	protected void SaveConfig()
	{
		super.SaveConfig();
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
	public void Tick()
	{
		super.Tick();
		if (this.currentWave >= 200)
			this.Reset();

		if (this.ticksUntilNextWave != -1)
		{
			this.ticksUntilNextWave -= 1;
			if (this.ticksUntilNextWave <= 0)
			{
				this.ticksSinceLastWave = 0;
				this.currentWave += this.waveIncrease;
				this.ResetSpectators();
				this.ticksUntilNextWave = -1;
				this.SendWave(this.currentWave);
				this.Broadcast("Wave " + (int) (currentWave) + " is coming!");

			}
		}

		Iterator<EntityCreature> i = monsters.values().iterator();
		while (i.hasNext())
		{
			EntityCreature monster = i.next();
			if (!monster.isAlive())
			{
				i.remove();
			}
		}

		if (this.monsters.size() <= 20 && this.ticksSinceLastWave >= 20 * 20 && this.ticksUntilNextWave == -1)
		{
			this.ticksUntilNextWave = 3 * 20;
			this.ResetSpectators();
			this.Broadcast("Below 20 zombies left, prepare for the next wave in 3 seconds!");
		}
	}

	@Override
	public void RemovePlayer(Player player, String reason)
	{
		super.RemovePlayer(player, reason);
		if (this.players.size() <= 0)
		{
			this.Reset();
		}
	}

	@Override
	public void MakeSpectator(Player player)
	{
		super.MakeSpectator(player);
		for (EntityCreature monster : monsters.values())
		{
			if (monster.target != null && monster.target instanceof EntityPlayer)
			{
				EntityPlayer target = (EntityPlayer) monster.target;
				if (this.spectators.contains(target.getBukkitEntity()))
				{
					List<Player> possiblePlayers = new ArrayList<Player>();
					for (Player poss : players)
					{
						if (!this.isSpectator(poss))
							possiblePlayers.add(poss);
					}
					if (possiblePlayers.size() != 0)
						monster.setTarget(((CraftPlayer) possiblePlayers.get(r.nextInt(possiblePlayers.size()))).getHandle());
				}
			}
		}
	}
}
