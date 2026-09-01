package com.redabysslucia.dragonrise_reforge.entities.projectile;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * R-73 红外制导空空导弹实体（R-73 射手）。
 * 继承 AIM-9 实体（Ru9m336MissileEntity 框架）：红外制导/追踪逻辑。
 * 实体 id dragonrise_reforge:r_73 → 模型/贴图自动解析。
 */
public class R73Entity extends Aim9Entity {

    public R73Entity(EntityType<? extends R73Entity> type, Level level) {
        super(type, level);
    }
}
