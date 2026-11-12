package com.redabysslucia.dragonrise_reforge.entities.projectile;

import com.atsuishio.superbwarfare.entity.projectile.Agm65Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/** AC-130 Missile: retains the aircraft's existing artwork as a separate expendable store. */
public class Ac130MissileEntity extends Agm65Entity {
    public Ac130MissileEntity(EntityType<? extends Ac130MissileEntity> type, Level level) { super(type, level); }
}
