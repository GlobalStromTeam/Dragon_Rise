package com.redabysslucia.dragonrise_reforge.entities.projectile;

import com.atsuishio.superbwarfare.entity.projectile.Agm65Entity;
import com.atsuishio.superbwarfare.tools.EntityFindUtil;
import com.atsuishio.superbwarfare.tools.RangeTool;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * 自研 AGM-65 导弹实体：继承 superb 官方 Agm65Entity 的全部制导/追踪/爆炸逻辑，
 * 仅更换模型与贴图（实体 id dragonrise_reforge:agm65 → models/bedrock/projectile/agm65.geo.json）。
 *
 * 与原版 AGM-65 的差异：
 * 1. 转向速度上限从 15°/tick 降为 10°/秒（= 0.5°/tick，按 20 TPS 换算）——
 *    Agm65Entity.tick 每 tick 以 invokevirtual 调用 turn()，子类覆写即可拦截并限制转向速率；
 * 2. 高抛大幅削弱：阈值从 30 格改为 300 格，高度系数从 0.4 大幅降至 0.05——
 *    距离 500 格时抬高量仅 0.05×(500−300)=10 格（原版为 0.4×470=188 格），
 *    300 格以内完全不抬。实现方式与 Aim120Entity 相同：覆写 turn() 忽略原版算出的
 *    转向方向，按新参数重新解算后再转向。
 */
public class Agm65CustomEntity extends Agm65Entity {

    /** 最大转向速度：10°/秒 ÷ 20 tick/秒 = 0.5°/tick */
    private static final float MAX_TURN_SPEED_PER_TICK = 10.0f / 20.0f;

    /** 高抛阈值：水平距离大于该值（格）时才抬高瞄准点（原版 AGM-65 为 30） */
    private static final double LOFT_THRESHOLD = 300.0;

    /** 高抛高度系数：原版 0.4，大幅降低至 0.05 */
    private static final double LOFT_HEIGHT_FACTOR = 0.05;

    public Agm65CustomEntity(EntityType<? extends Agm65Entity> type, Level level) {
        super(type, level);
    }

    @Override
    public void turn(Vec3 vec3, float turnSpeed) {
        super.turn(recalculateToVec(), Math.min(turnSpeed, MAX_TURN_SPEED_PER_TICK));
    }

    /**
     * 按削弱后的高抛参数重新计算转向方向（与 Agm65Entity.tick 的制导逻辑一致，仅参数不同）：
     * - guideType 0（锁实体）：目标实体当前位置 + 高抛补偿，按目标运动速度解算提前量；
     * - guideType 1（锁坐标）：锁定坐标 + 高抛补偿，静态提前量解算；
     * - 无目标/非服务端：保持当前朝向（与原版行为一致）。
     */
    private Vec3 recalculateToVec() {
        Vec3 toVec = this.getLookAngle();
        Entity entity = EntityFindUtil.findEntity(this.level(), this.getTargetUUID());

        if (getGuideType() == 0) {
            if (!this.getTargetUUID().equals("none") && entity != null && level() instanceof ServerLevel) {
                double dis = entity.position().vectorTo(position()).horizontalDistance();
                double height = dis > LOFT_THRESHOLD ? LOFT_HEIGHT_FACTOR * (dis - LOFT_THRESHOLD) : 0.0;
                Vec3 targetPos = new Vec3(entity.getX(),
                        entity.getY() + (entity instanceof EnderDragon ? -2 : 0) + height, entity.getZ());
                toVec = RangeTool.calculateFiringSolution(position(), targetPos,
                        entity.getDeltaMovement(), getDeltaMovement().length(), 0.0);
            }
        } else if (level() instanceof ServerLevel && getTargetPos() != null) {
            double dis = getTargetPos().vectorTo(position()).horizontalDistance();
            double height = dis > LOFT_THRESHOLD ? LOFT_HEIGHT_FACTOR * (dis - LOFT_THRESHOLD) : 0.0;
            Vec3 targetPos = this.getTargetPos().add(0.0, height, 0.0);
            toVec = RangeTool.calculateFiringSolution(position(), targetPos, Vec3.ZERO, getDeltaMovement().length(), 0.0);
        }
        return toVec;
    }
}
