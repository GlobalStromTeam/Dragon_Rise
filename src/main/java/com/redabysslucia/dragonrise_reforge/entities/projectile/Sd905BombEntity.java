package com.redabysslucia.dragonrise_reforge.entities.projectile;

import com.atsuishio.superbwarfare.entity.projectile.Mk82Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/** SD-905 Bomb: retains the aircraft's existing artwork as a separate expendable store. */
public class Sd905BombEntity extends Mk82Entity {
    public Sd905BombEntity(EntityType<? extends Sd905BombEntity> type, Level level) { super(type, level); }
}
