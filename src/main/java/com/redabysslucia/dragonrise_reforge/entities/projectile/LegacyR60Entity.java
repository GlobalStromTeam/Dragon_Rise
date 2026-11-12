package com.redabysslucia.dragonrise_reforge.entities.projectile;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/** R-60: retains the aircraft's existing artwork as a separate expendable store. */
public class LegacyR60Entity extends Aim9Entity {
    public LegacyR60Entity(EntityType<? extends LegacyR60Entity> type, Level level) { super(type, level); }
}
