package com.redabysslucia.dragonrise_reforge.entities.projectile;

import com.atsuishio.superbwarfare.entity.projectile.Agm65Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * AIM-120 AMRAAM 导弹实体。
 * 以 superb 的 AGM-65 (Agm65Entity) 为基底：继承其全部制导/追踪/爆炸逻辑，
 * 实体 id 为 dragonrise_reforge:aim120，因此渲染时自动从
 * models/bedrock/projectile/aim120.geo.json 加载新版模型（SBM v2 按实体注册名推导模型路径）。
 */
public class Aim120Entity extends Agm65Entity {

    public Aim120Entity(EntityType<? extends Agm65Entity> type, Level level) {
        super(type, level);
    }
}
