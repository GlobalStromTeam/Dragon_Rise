package com.redabysslucia.dragonrise_reforge.entities.projectile;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/** AASM: retains the aircraft's existing artwork as a separate expendable store. */
public class AasmEntity extends GuidedBombEntity {
    public AasmEntity(EntityType<? extends AasmEntity> type, Level level) { super(type, level); }
}
