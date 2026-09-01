package com.redabysslucia.dragonrise_reforge.entities.projectile;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * GBU-12 制导炸弹实体：继承自研的制导炸弹基类 GuidedBombEntity。
 * 实体 id dragonrise_reforge:gbu_12 → models/bedrock/projectile/gbu_12.geo.json（渲染路径自动解析）。
 *
 * 制导模式（详见 GuidedBombEntity）：
 * - 激光架束制导：未锁定地面直接投弹，炸弹每 tick 向吊舱当前瞄准点轻微修正；
 * - 卫星制导：在吊舱视角按锁定键锁定地面后投弹，炸弹每 tick 向锁定坐标轻微修正。
 */
public class Gbu12Entity extends GuidedBombEntity {

    public Gbu12Entity(EntityType<? extends GuidedBombEntity> type, Level level) {
        super(type, level);
        // 250kg 级制导炸弹：范围/威力缩减为基线的 1/4（1300/32 → 325/8）
        this.setExplosionDamage(325f);
        this.setExplosionRadius(8f);
    }
}
