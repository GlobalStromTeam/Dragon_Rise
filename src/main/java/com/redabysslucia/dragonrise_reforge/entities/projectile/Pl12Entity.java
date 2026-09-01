package com.redabysslucia.dragonrise_reforge.entities.projectile;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * PL-12 主动雷达制导空空导弹实体（霹雳-12）。
 * 继承 AIM-120 实体（Agm65Entity 框架）：转向上限 18°/秒、无高抛、近炸引信。
 * 实体 id dragonrise_reforge:pl_12 → 模型/贴图自动解析。
 */
public class Pl12Entity extends Aim120Entity {

    public Pl12Entity(EntityType<? extends Pl12Entity> type, Level level) {
        super(type, level);
    }
}
