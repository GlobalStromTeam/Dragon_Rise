package com.redabysslucia.dragonrise_reforge.entities.projectile;

import com.atsuishio.superbwarfare.entity.projectile.Mk82Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/** Original TJGC bomb behavior with its independently authored model and texture. */
public class TjgcBombEntity extends Mk82Entity {
    public TjgcBombEntity(EntityType<? extends TjgcBombEntity> type, Level level) {
        super(type, level);
    }
}
