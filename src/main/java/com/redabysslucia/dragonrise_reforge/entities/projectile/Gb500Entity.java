package com.redabysslucia.dragonrise_reforge.entities.projectile;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * GB-500 制导炸弹实体（500kg 激光/卫星制导炸弹）。
 * 继承 GuidedBombEntity：重力下落、穿水、命中即爆、激光架束/卫星制导。
 * 转向能力 0.75°/tick（15°/秒）。
 * 实体 id dragonrise_reforge:gb_500 → 模型/贴图自动解析。
 */
public class Gb500Entity extends GuidedBombEntity {

    public Gb500Entity(EntityType<? extends Gb500Entity> type, Level level) {
        super(type, level);
    }

    /**
     * GB-500 转向能力（每次判定 3.75° = 每 tick 平均 0.75° = 15°/秒）。
     */
    @Override
    protected double getCorrectionDegreesPerTick() {
        return 3.75;
    }
}
