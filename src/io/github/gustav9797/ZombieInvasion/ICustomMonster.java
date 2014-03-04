package io.github.gustav9797.ZombieInvasion;

import net.minecraft.server.v1_7_R1.EntityHuman;

public interface ICustomMonster {
	public void setArena(Arena arena);
	public EntityHuman findNearbyVulnerablePlayer(double d0, double d1, double d2);
}
