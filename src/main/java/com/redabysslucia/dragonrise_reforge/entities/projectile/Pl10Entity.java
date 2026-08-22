package com.redabysslucia.dragonrise_reforge.entities.projectile;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * PL-10 红外制导空空导弹实体（霹雳-10）。
 * 继承 AIM-9 实体（Ru9m336MissileEntity 框架）：红外制导/追踪逻辑。
 * 实体 id dragonrise_reforge:pl_10 → 模型/贴图自动解析。
 */
public class Pl10Entity extends Aim9Entity {

    public Pl10Entity(EntityType<? extends Pl10Entity> type, Level level) {
        super(type, level);
    }
}
