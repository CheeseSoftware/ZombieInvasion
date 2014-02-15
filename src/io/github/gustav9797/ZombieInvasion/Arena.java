package io.github.gustav9797.ZombieInvasion;

import java.util.LinkedList;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class Arena
{
	protected int size;
	protected Location middle;
	protected Random r = new Random();
	
	//Stores the original blocks to be able to restore the border later
	protected LinkedList<BlockState> border;

	public Arena(int size, Location middle)
	{
		this.size = size;
		this.middle = middle;
		border = new LinkedList<BlockState>();
	}
	
	abstract void StartWave(int wave, JavaPlugin plugin);

	public void CreateBorder(int height, Material material)
	{
		if(!border.isEmpty())
			RestoreBorder();
		
		int radius = size / 2;
		BlockState originalBlock = null;
		World world = middle.getWorld();
		for (int y = 0; y < height; y++)
		{
			for (int x = -radius - 1; x < radius + 1; x++)
			{
				originalBlock = world.getBlockAt(x + middle.getBlockX(), y, -radius + middle.getBlockZ()).getState();
				this.border.push(originalBlock);
				world.getBlockAt(x + middle.getBlockX(), y, -radius + middle.getBlockZ()).setType(material);
				
				originalBlock = world.getBlockAt(x + middle.getBlockX(), y, radius + middle.getBlockZ()).getState();
				this.border.push(originalBlock);
				world.getBlockAt(x + middle.getBlockX(), y, radius + middle.getBlockZ()).setType(material);
			}
			
			for (int z = -radius - 1; z < radius + 1; z++)
			{
				originalBlock = world.getBlockAt(-radius + middle.getBlockX(), y, z + middle.getBlockZ()).getState();
				this.border.push(originalBlock);
				world.getBlockAt(-radius + middle.getBlockX(), y, z + middle.getBlockZ()).setType(material);
				
				originalBlock = world.getBlockAt(radius + middle.getBlockX(), y, z + middle.getBlockZ()).getState();
				this.border.push(originalBlock);
				world.getBlockAt(radius + middle.getBlockX(), y, z + middle.getBlockZ()).setType(material);
			}
		}
	}
	
	public void RestoreBorder()
	{
		for(BlockState block : border)
		{
			block.getBlock().setType(block.getType());
		}
	}

	public void setSize(int size)
	{
		this.size = size;
	}

	public int getSize()
	{
		return this.size;
	}

	public void setMiddle(Location middle)
	{
		this.middle = middle;
	}

	public Location getMiddle()
	{
		return this.middle;
	}

}
