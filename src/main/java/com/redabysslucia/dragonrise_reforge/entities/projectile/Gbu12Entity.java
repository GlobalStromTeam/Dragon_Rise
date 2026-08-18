package com.redabysslucia.dragonrise_reforge.entities.projectile;

import com.atsuishio.superbwarfare.entity.projectile.Mk84Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * GBU-12 激光制导炸弹实体（暂未做制导）：完全继承官方 Mk84 的自由落体行为，
 * 仅更换模型与贴图（实体 id dragonrise_reforge:gbu_12 → models/bedrock/projectile/gbu_12.geo.json）。
 */
public class Gbu12Entity extends Mk84Entity {

    public Gbu12Entity(EntityType<? extends Mk84Entity> type, Level level) {
        super(type, level);
    }
}
