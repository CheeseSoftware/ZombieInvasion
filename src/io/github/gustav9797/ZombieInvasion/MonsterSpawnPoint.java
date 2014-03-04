package io.github.gustav9797.ZombieInvasion;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

public class MonsterSpawnPoint extends SpawnPoint implements Cloneable, ConfigurationSerializable
{
	private List<EntityType> monsters = new ArrayList<EntityType>();

	public MonsterSpawnPoint(int id, Vector position)
	{
		super(id, position);
	}

	public void AddEntityType(EntityType entityType)
	{
		this.monsters.add(entityType);
	}

	public void RemoveEntityType(EntityType entityType)
	{
		if (this.monsters.contains(entityType))
			this.monsters.remove(entityType);
	}
	
	@SuppressWarnings("deprecation")
	public boolean hasEntityType(EntityType entityType)
	{
		for(EntityType e : this.monsters)
			if(e.getTypeId() == entityType.getTypeId())
				return true;
		return false;
	}
	
	public List<EntityType> getEntityTypes()
	{
		return this.monsters;
	}

	@Override
	public Map<String, Object> serialize()
	{
		Map<String, Object> result = new LinkedHashMap<String, Object>();
		result.put("id", this.id);
		result.put("position", this.position);
		List<String> temp = new ArrayList<String>();
		for(EntityType e : this.monsters)
			temp.add(e.toString());
		result.put("monsters", temp);
		return result;
	}

	@SuppressWarnings({ "unchecked", "deprecation" })
	public static MonsterSpawnPoint deserialize(Map<String, Object> args)
	{
		Vector position = (Vector) args.get("position");
		if (position != null)
		{
			MonsterSpawnPoint spawnPoint = new MonsterSpawnPoint((int) args.get("id"), position);
			List<String> temp = (List<String>) args.get("monsters");
			if (temp != null)
			{
				ArrayList<EntityType> entityTypes = new ArrayList<EntityType>();
				for(String s : temp)
					entityTypes.add(EntityType.fromName(s));
				for (EntityType entityType : entityTypes)
					spawnPoint.AddEntityType(entityType);
			}
			else
				Bukkit.getLogger().severe("Could not load monsters for MonsterSpawnPoint");
			return spawnPoint;
		}
		else
			Bukkit.getLogger().severe("Could not load position for MonsterSpawnPoint");
		return null;
	}
}
