package com.redabysslucia.dragonrise_reforge.entities.projectile;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/** AIM-54: retains the aircraft's existing artwork as a separate expendable store. */
public class LegacyAim54Entity extends Aim120Entity {
    public LegacyAim54Entity(EntityType<? extends LegacyAim54Entity> type, Level level) { super(type, level); }
}
