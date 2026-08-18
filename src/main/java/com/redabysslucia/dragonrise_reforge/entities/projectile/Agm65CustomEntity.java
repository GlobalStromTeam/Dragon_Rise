package com.redabysslucia.dragonrise_reforge.entities.projectile;

import com.atsuishio.superbwarfare.entity.projectile.Agm65Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * 自研 AGM-65 导弹实体：继承 superb 官方 Agm65Entity 的全部制导/追踪/爆炸逻辑，
 * 仅更换模型与贴图（实体 id dragonrise_reforge:agm65 → models/bedrock/projectile/agm65.geo.json）。
 */
public class Agm65CustomEntity extends Agm65Entity {

    public Agm65CustomEntity(EntityType<? extends Agm65Entity> type, Level level) {
        super(type, level);
    }
}
