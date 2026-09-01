package com.redabysslucia.dragonrise_reforge.entities.projectile;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * KD-88A 空对地导弹实体（空地-88A）。
 * 继承 AGM-65 实体（Agm65CustomEntity 框架）：转向上限 10°/秒、削弱高抛。
 * 实体 id dragonrise_reforge:kd_88a → 模型/贴图自动解析。
 */
public class Kd88aEntity extends Agm65CustomEntity {

    public Kd88aEntity(EntityType<? extends Kd88aEntity> type, Level level) {
        super(type, level);
    }
}
