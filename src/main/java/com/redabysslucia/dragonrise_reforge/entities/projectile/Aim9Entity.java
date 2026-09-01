package com.redabysslucia.dragonrise_reforge.entities.projectile;

import com.atsuishio.superbwarfare.entity.projectile.Ru9m336MissileEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * AIM-9 响尾蛇导弹实体：继承官方 RU-9M336（红外制导空空导弹）的全部制导/追踪逻辑，
 * 仅更换模型与贴图（实体 id dragonrise_reforge:aim9 → models/bedrock/projectile/aim9.geo.json）。
 */
public class Aim9Entity extends Ru9m336MissileEntity {

    public Aim9Entity(EntityType<? extends Ru9m336MissileEntity> type, Level level) {
        super(type, level);
    }
}
