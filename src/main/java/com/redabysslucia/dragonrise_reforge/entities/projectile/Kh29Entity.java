package com.redabysslucia.dragonrise_reforge.entities.projectile;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * Kh-29 空对地导弹实体（Kh-29 小牛斯基）。
 * 继承 AGM-65 实体（Agm65CustomEntity 框架）：转向上限 10°/秒、削弱高抛。
 * 实体 id dragonrise_reforge:kh_29 → 模型/贴图自动解析。
 */
public class Kh29Entity extends Agm65CustomEntity {

    public Kh29Entity(EntityType<? extends Kh29Entity> type, Level level) {
        super(type, level);
    }
}
