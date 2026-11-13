package com.redabysslucia.dragonrise_reforge.entities.projectile;

import com.atsuishio.superbwarfare.entity.projectile.WireGuideMissileEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/** EC-665 ATGM: retains the aircraft's existing artwork as a separate expendable store. */
public class Ec665AtgmEntity extends WireGuideMissileEntity {
    public Ec665AtgmEntity(EntityType<? extends Ec665AtgmEntity> type, Level level) { super(type, level); }
}
