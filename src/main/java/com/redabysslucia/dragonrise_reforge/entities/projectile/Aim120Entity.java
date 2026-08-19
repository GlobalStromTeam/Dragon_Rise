package com.redabysslucia.dragonrise_reforge.entities.projectile;

import com.atsuishio.superbwarfare.entity.projectile.Agm65Entity;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.tools.EntityFindUtil;
import com.atsuishio.superbwarfare.tools.RangeTool;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * AIM-120 AMRAAM 导弹实体。
 * 以 superb 的 AGM-65 (Agm65Entity) 为基底：继承其全部制导/追踪/爆炸逻辑，
 * 实体 id 为 dragonrise_reforge:aim120，因此渲染时自动从
 * models/bedrock/projectile/aim120.geo.json 加载新版模型（SBM v2 按实体注册名推导模型路径）。
 *
 * 与原版 AGM-65 的差异：
 * 1. 转向速度上限从 15°/tick 降为 18°/秒（= 0.9°/tick，按 20 TPS 换算）——
 *    Agm65Entity.tick 每 tick 以 invokevirtual 调用 turn()，子类覆写即可拦截并限制转向速率，
 *    AGM-65 本体（Agm65CustomEntity）不受影响；
 * 2. 近炸引信：周围半径 5 格内出现载具（排除发射者乘坐的载具）时立即自爆；
 * 3. 彻底移除高抛：瞄准点就是目标位置本身，不再随距离抬高（全程直瞄 + 提前量解算）。
 *    实现方式：Agm65Entity.tick 内部算出的转向方向 toVec 会经虚方法 turn(toVec, speed) 传入，
 *    此处覆写 turn() 忽略传入方向、按无高抛逻辑重新解算后再转向（tick 链/移动/碰撞/爆炸逻辑保持不变）。
 */
public class Aim120Entity extends Agm65Entity {

    /** 最大转向速度：18°/秒 ÷ 20 tick/秒 = 0.9°/tick */
    private static final float MAX_TURN_SPEED_PER_TICK = 18.0f / 20.0f;

    /** 近炸引信半径（格） */
    private static final double PROXIMITY_FUZE_RADIUS = 5.0;

    public Aim120Entity(EntityType<? extends Agm65Entity> type, Level level) {
        super(type, level);
    }

    @Override
    public void turn(Vec3 vec3, float turnSpeed) {
        super.turn(recalculateToVec(), Math.min(turnSpeed, MAX_TURN_SPEED_PER_TICK));
    }

    /**
     * 重新计算转向方向（无高抛版：瞄准点就是目标位置本身，仅做提前量解算）：
     * - guideType 0（锁实体）：目标实体当前位置，按目标运动速度解算提前量；
     * - guideType 1（锁坐标）：锁定坐标，静态提前量解算；
     * - 无目标/非服务端：保持当前朝向（与原版行为一致）。
     */
    private Vec3 recalculateToVec() {
        Vec3 toVec = this.getLookAngle();
        Entity entity = EntityFindUtil.findEntity(this.level(), this.getTargetUUID());

        if (getGuideType() == 0) {
            if (!this.getTargetUUID().equals("none") && entity != null && level() instanceof ServerLevel) {
                Vec3 targetPos = new Vec3(entity.getX(),
                        entity.getY() + (entity instanceof EnderDragon ? -2 : 0), entity.getZ());
                toVec = RangeTool.calculateFiringSolution(position(), targetPos,
                        entity.getDeltaMovement(), getDeltaMovement().length(), 0.0);
            }
        } else if (level() instanceof ServerLevel && getTargetPos() != null) {
            toVec = RangeTool.calculateFiringSolution(position(), this.getTargetPos(),
                    Vec3.ZERO, getDeltaMovement().length(), 0.0);
        }
        return toVec;
    }

    @Override
    public void tick() {
        super.tick();

        // 近炸引信（仅服务端）：半径 5 格内有载具 → 自爆
        if (level() instanceof ServerLevel && isAlive()) {
            Entity owner = getOwner();
            Entity shooterVehicle = owner != null ? owner.getVehicle() : null;

            List<VehicleEntity> vehicles = level().getEntitiesOfClass(
                    VehicleEntity.class,
                    getBoundingBox().inflate(PROXIMITY_FUZE_RADIUS),
                    v -> v != shooterVehicle
            );

            if (!vehicles.isEmpty()) {
                this.discard();
                causeExplode(position());
            }
        }
    }
}
