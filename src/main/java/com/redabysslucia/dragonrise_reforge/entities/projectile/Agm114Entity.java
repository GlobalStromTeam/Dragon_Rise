package com.redabysslucia.dragonrise_reforge.entities.projectile;

import com.atsuishio.superbwarfare.entity.projectile.WireGuideMissileEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/** AGM-114: retains the aircraft's existing artwork as a separate expendable store. */
public class Agm114Entity extends WireGuideMissileEntity {
    public Agm114Entity(EntityType<? extends Agm114Entity> type, Level level) { super(type, level); }
}
