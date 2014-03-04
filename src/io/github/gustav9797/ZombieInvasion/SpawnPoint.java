package io.github.gustav9797.ZombieInvasion;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

public class SpawnPoint implements Cloneable, ConfigurationSerializable
{
	protected int id;
	protected Vector position;
	protected Map<EntityType, Integer> entityTypes = new HashMap<EntityType, Integer>();
	protected Random r = new Random();

	public SpawnPoint(int id, Vector position)
	{
		this.id = id;
		this.position = position;
	}

	public int getId()
	{
		return this.id;
	}

	public Vector getPosition()
	{
		return this.position;
	}

	public void AddEntityType(EntityType entityType, int chance)
	{
		this.entityTypes.put(entityType, chance);
	}

	public void RemoveEntityType(EntityType entityType)
	{
		if (this.entityTypes.containsKey(entityType))
			this.entityTypes.remove(entityType);
	}

	public boolean hasEntityType(EntityType entityType)
	{
		for (EntityType e : this.entityTypes.keySet())
		{
			if (e.equals(entityType))
				return true;
		}
		return false;
	}

	public Set<EntityType> getEntityTypes()
	{
		return this.entityTypes.keySet();
	}

	public EntityType getRandomEntityType()
	{
		if (this.entityTypes.size() > 0)
		{
			Map<EntityType, Integer> bottomChances = new HashMap<EntityType, Integer>();
			Map<EntityType, Integer> topChances = new HashMap<EntityType, Integer>();

			int currentChance = 0;
			for (Entry<EntityType, Integer> pair : this.entityTypes.entrySet())
			{
				bottomChances.put(pair.getKey(), currentChance);
				topChances.put(pair.getKey(), pair.getValue() + currentChance);
				currentChance += pair.getValue();
			}
			int totalChance = currentChance;
			int selected = r.nextInt(totalChance);

			for (Entry<EntityType, Integer> pair : bottomChances.entrySet())
			{
				if (selected >= pair.getValue())
				{
					int topChance = topChances.get(pair.getKey());
					if (selected < topChance)
					{
						return pair.getKey();
					}
				}
			}
		}
		return null;
	}

	public int getChancePercentage(EntityType entityType)
	{
		int totalChance = 0;
		for (int chance : this.entityTypes.values())
			totalChance += chance;

		if (this.entityTypes.containsKey(entityType))
			return this.entityTypes.get(entityType) / totalChance * 100;
		return 0;
	}

	@Override
	public Map<String, Object> serialize()
	{
		Map<String, Object> result = new LinkedHashMap<String, Object>();
		result.put("id", this.id);
		result.put("position", this.position);
		Map<String, Integer> temp = new HashMap<String, Integer>();
		for (Entry<EntityType, Integer> pair : this.entityTypes.entrySet())
			temp.put(pair.getKey().toString(), pair.getValue());
		result.put("entitytypes", temp);
		return result;
	}

	@SuppressWarnings(
	{ "unchecked", "deprecation" })
	public static SpawnPoint deserialize(Map<String, Object> args)
	{
		Vector position = (Vector) args.get("position");
		if (position != null)
		{
			SpawnPoint spawnPoint = new SpawnPoint((int) args.get("id"), position);
			Map<String, Integer> temp = (Map<String, Integer>) args.get("entitytypes");
			if (temp != null)
			{
				for (Entry<String, Integer> pair : temp.entrySet())
					spawnPoint.AddEntityType(EntityType.fromName(pair.getKey()), pair.getValue());
			}
			return spawnPoint;
		}
		return null;
	}
}
