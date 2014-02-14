package io.github.gustav9797.ZombieInvasion;

import java.util.Collections;
import java.util.List;

import net.minecraft.server.v1_7_R1.DistanceComparator;
import net.minecraft.server.v1_7_R1.EntityCreature;
import net.minecraft.server.v1_7_R1.EntityLiving;
import net.minecraft.server.v1_7_R1.IEntitySelector;
import net.minecraft.server.v1_7_R1.PathfinderGoalTarget;

public class CustomPathfinderGoalNearestAttackableTarget extends PathfinderGoalTarget {

    private final Class a;
    private final int b;
    private final DistanceComparator distanceComparator;
    private final IEntitySelector entitySelector;
    private EntityLiving entityLiving;

    public CustomPathfinderGoalNearestAttackableTarget(EntityCreature entitycreature, Class oclass, int i, boolean flag) {
        this(entitycreature, oclass, i, flag, false);
    }

    public CustomPathfinderGoalNearestAttackableTarget(EntityCreature entitycreature, Class oclass, int i, boolean flag, boolean flag1) {
        this(entitycreature, oclass, i, flag, flag1, (IEntitySelector) null);
    }

    public CustomPathfinderGoalNearestAttackableTarget(EntityCreature entitycreature, Class oclass, int i, boolean flag, boolean flag1, IEntitySelector ientityselector) {
        super(entitycreature, flag, flag1);
        this.a = oclass;
        this.b = i;
        this.distanceComparator = new DistanceComparator(entitycreature);
        this.a(1);
        this.entitySelector = ientityselector;
    }

    @SuppressWarnings("unchecked")
	public boolean a() {
        if (this.b > 0 && this.c.aI().nextInt(this.b) != 0) {
            return false;
        } else {
            double d0 = this.f();
            List list = this.c.world.a(this.a, this.c.boundingBox.grow(256, 256, 256), this.entitySelector);

            Collections.sort(list, this.distanceComparator);
            if (list.isEmpty()) {
                return false;
            } else {
                this.entityLiving = (EntityLiving) list.get(0);
                return true;
            }
        }
    }

    public void c() {
        this.c.setGoalTarget(this.entityLiving);
        super.c();
    }
}