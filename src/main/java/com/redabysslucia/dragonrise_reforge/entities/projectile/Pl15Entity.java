package com.redabysslucia.dragonrise_reforge.entities.projectile;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * PL-15 主动雷达制导空空导弹实体（霹雳-15）。
 * 继承 AIM-120 实体（Agm65Entity 框架）：转向上限 18°/秒、无高抛、近炸引信。
 * 实体 id dragonrise_reforge:pl_15 → 模型/贴图自动解析。
 */
public class Pl15Entity extends Aim120Entity {

    public Pl15Entity(EntityType<? extends Pl15Entity> type, Level level) {
        super(type, level);
    }
}
