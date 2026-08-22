package com.redabysslucia.dragonrise_reforge.entities.projectile;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * GB-250 制导炸弹实体（仅卫星制导）。
 * 继承 GuidedBombEntity：重力下落、穿水、命中即爆。
 * 无滑翔能力，性能与 GBU-12 相同（游戏简化）：转向 20°/秒（基类默认）、
 * 250kg 级威力范围缩减为基线的 1/4（1300/32 → 325/8）。
 * 仅卫星制导：只向锁定的地面坐标修正，未锁定时纯弹道下落（无激光架束）。
 * 实体 id dragonrise_reforge:gb_250 → 模型/贴图自动解析。
 */
public class Gb250Entity extends GuidedBombEntity {

    public Gb250Entity(EntityType<? extends Gb250Entity> type, Level level) {
        super(type, level);
        // 250kg 级制导炸弹：范围/威力缩减为基线的 1/4（1300/32 → 325/8），与 GBU-12 一致
        this.setExplosionDamage(325f);
        this.setExplosionRadius(8f);
    }

    @Override
    protected void updateTarget() {
        // 仅卫星制导：只向锁定的地面坐标修正；未锁定（guideType 0）时纯弹道下落
        if (getGuideType() == 1) {
            currentTarget = getTargetPos();
        }
    }
}
