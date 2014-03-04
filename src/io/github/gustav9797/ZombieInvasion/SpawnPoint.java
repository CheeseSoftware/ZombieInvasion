package io.github.gustav9797.ZombieInvasion;

import org.bukkit.util.Vector;

public class SpawnPoint
{
	protected int id;
	protected Vector position;
	
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
}
