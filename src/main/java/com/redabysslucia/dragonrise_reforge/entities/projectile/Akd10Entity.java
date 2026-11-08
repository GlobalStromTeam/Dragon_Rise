package com.redabysslucia.dragonrise_reforge.entities.projectile;

import com.atsuishio.superbwarfare.entity.projectile.WireGuideMissileEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/** AKD-10: retains the aircraft's existing artwork as a separate expendable store. */
public class Akd10Entity extends WireGuideMissileEntity {
    public Akd10Entity(EntityType<? extends Akd10Entity> type, Level level) { super(type, level); }
}
