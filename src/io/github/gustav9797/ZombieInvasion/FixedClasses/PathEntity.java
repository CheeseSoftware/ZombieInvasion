//package io.github.gustav9797.ZombieInvasion.FixedClasses;
//
//import net.minecraft.server.v1_7_R1.Entity;
//import net.minecraft.server.v1_7_R1.Vec3D;
//
//public class PathEntity
//{
//
//	private final PathPoint[] pathPoints;
//	private int b;
//	private int pathPointsLength;
//
//	public PathEntity(PathPoint[] apathpoint)
//	{
//		this.pathPoints = apathpoint;
//		this.pathPointsLength = apathpoint.length;
//	}
//
//	public void a()
//	{
//		++this.b;
//	}
//
//	public boolean b()
//	{
//		return this.b >= this.pathPointsLength;
//	}
//
//	//PathPoint c()
//	public PathPoint getFirstPathPoint()
//	{
//		return this.pathPointsLength > 0 ? this.pathPoints[this.pathPointsLength - 1] : null;
//	}
//
//	//PathPoint a(int i)
//	public PathPoint getPathPointAt(int i)
//	{
//		return this.pathPoints[i];
//	}
//
//	// int d()
//	public int getPathPointsLength()
//	{
//		return this.pathPointsLength;
//	}
//
//	// void b(int i)
//	public void setPathPointsLength(int i)
//	{
//		this.pathPointsLength = i;
//	}
//
//	public int e()
//	{
//		return this.b;
//	}
//
//	public void c(int i)
//	{
//		this.b = i;
//	}
//
//	//Vec3D a(Entity entity, int i)
//	public Vec3D getExactEntityLocationAt(Entity entity, int i)
//	{
//		double d0 = (double) this.pathPoints[i].x + (double) ((int) (entity.width + 1.0F)) * 0.5D;
//		double d1 = (double) this.pathPoints[i].y;
//		double d2 = (double) this.pathPoints[i].z + (double) ((int) (entity.width + 1.0F)) * 0.5D;
//
//		return entity.world.getVec3DPool().create(d0, d1, d2);
//	}
//
//	//Vec3D a(Entity entity)
//	public Vec3D getExactEntityLocationAtB(Entity entity)
//	{
//		return this.getExactEntityLocationAt(entity, this.b);
//	}
//
//	//boolean a(PathEntity pathentity)
//	public boolean equals(PathEntity pathentity)
//	{
//		if (pathentity == null)
//		{
//			return false;
//		}
//		else if (pathentity.pathPoints.length != this.pathPoints.length)
//		{
//			return false;
//		}
//		else
//		{
//			for (int i = 0; i < this.pathPoints.length; ++i)
//			{
//				if (this.pathPoints[i].x != pathentity.pathPoints[i].x || this.pathPoints[i].y != pathentity.pathPoints[i].y || this.pathPoints[i].z != pathentity.pathPoints[i].z)
//				{
//					return false;
//				}
//			}
//
//			return true;
//		}
//	}
//
//	//boolean b(Vec3D vec3d)
//	public boolean nextPathpointEquals(Vec3D vec3d)
//	{
//		PathPoint pathpoint = this.getFirstPathPoint();
//
//		return pathpoint == null ? false : pathpoint.x == (int) vec3d.c && pathpoint.z == (int) vec3d.e;
//	}
//}