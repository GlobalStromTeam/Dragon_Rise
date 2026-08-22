package com.redabysslucia.dragonrise_reforge.entities.projectile;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * R-77 主动雷达制导空空导弹实体（R-77 蝰蛇）。
 * 继承 AIM-120 实体（Agm65Entity 框架）：转向上限 18°/秒、无高抛、近炸引信。
 * 实体 id dragonrise_reforge:r_77 → 模型/贴图自动解析。
 */
public class R77Entity extends Aim120Entity {

    public R77Entity(EntityType<? extends R77Entity> type, Level level) {
        super(type, level);
    }
}
