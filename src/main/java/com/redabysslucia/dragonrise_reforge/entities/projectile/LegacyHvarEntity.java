package com.redabysslucia.dragonrise_reforge.entities.projectile;

import com.atsuishio.superbwarfare.entity.projectile.SmallRocketEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/** HVAR: retains the aircraft's existing artwork as a separate expendable store. */
public class LegacyHvarEntity extends SmallRocketEntity {
    public LegacyHvarEntity(EntityType<? extends LegacyHvarEntity> type, Level level) { super(type, level); }
}
