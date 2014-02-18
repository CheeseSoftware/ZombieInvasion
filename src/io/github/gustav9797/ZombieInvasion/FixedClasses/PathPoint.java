//package io.github.gustav9797.ZombieInvasion.FixedClasses;
//
//import net.minecraft.server.v1_7_R1.MathHelper;
//
//public class PathPoint
//{
//
//	public final int x; // a
//	public final int y; // b
//	public final int z; // c
//	private final int combined; // j
//
//	/*
//	 * int d = -1; float e; float f; float g; PathPoint h; public boolean i;
//	 */
//
//	// PathPoint(int i, int j, int k)
//	public PathPoint(int x, int y, int z)
//	{
//		this.x = x;
//		this.y = y;
//		this.z = z;
//		this.combined = toCombined(x, y, z);
//	}
//
//	// int a(int i, int j, int k)
//	public static int toCombined(int x, int y, int z)
//	{
//		return y & 255 | (x & 32767) << 8 | (z & 32767) << 24 | (x < 0 ? Integer.MIN_VALUE : 0) | (z < 0 ? '\u8000' : 0);
//	}
//
//	// float g(PathPoint pathpoint)
//	public float getDistanceBetween(PathPoint pathpoint)
//	{
//		float f = (float) (pathpoint.x - this.x);
//		float f1 = (float) (pathpoint.y - this.y);
//		float f2 = (float) (pathpoint.z - this.z);
//
//		return MathHelper.c(f * f + f1 * f1 + f2 * f2);
//	}
//
//	// float b(PathPoint pathpoint)
//	public float getDistanceBetweenNoneSqrt(PathPoint pathpoint)
//	{
//		float f = (float) (pathpoint.x - this.x);
//		float f1 = (float) (pathpoint.y - this.y);
//		float f2 = (float) (pathpoint.z - this.z);
//
//		return f * f + f1 * f1 + f2 * f2;
//	}
//
//	public boolean equals(Object object)
//	{
//		if (!(object instanceof PathPoint))
//		{
//			return false;
//		}
//		else
//		{
//			PathPoint pathpoint = (PathPoint) object;
//
//			return this.combined == pathpoint.combined && this.x == pathpoint.x && this.y == pathpoint.y && this.z == pathpoint.z;
//		}
//	}
//
//	public int hashCode()
//	{
//		return this.combined;
//	}
//
//	/*
//	 * public boolean a() { return this.d >= 0; }
//	 */
//
//	public String toString()
//	{
//		return this.x + ", " + this.y + ", " + this.z;
//	}
//}