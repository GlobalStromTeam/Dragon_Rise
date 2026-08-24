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
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * AIM-120 AMRAAM 导弹实体（雷达制导空空弹基类，PL-12/PL-15/R-77 均继承）。
 * 以 superb 的 AGM-65 (Agm65Entity) 为基底：继承其全部制导/追踪/爆炸逻辑，
 * 实体 id 为 dragonrise_reforge:aim120，因此渲染时自动从
 * models/bedrock/projectile/aim120.geo.json 加载新版模型（SBM v2 按实体注册名推导模型路径）。
 *
 * 与原版 AGM-65 的差异：
 * 1. 转向速度上限从 15°/tick 降为 18°/秒（= 0.9°/tick，按 20 TPS 换算）；
 * 2. 近炸引信：周围半径 5 格内出现载具（排除发射者乘坐的载具）时立即自爆；
 * 3. 彻底移除高抛：瞄准点就是目标位置本身，不再随距离抬高；
 * 4. 雷达弹专属制导规则（真实雷达弹行为）：
 *    a) 低空目标保护：目标离地面高度 &lt; 25 格时无法维持追踪，导弹进入惯导（直线飞行）；
 *    b) 视野丢失记忆：目标从视野消失后继续朝最后已知位置飞行，
 *       目标重新出现在视野中时恢复追踪。
 */
public class Aim120Entity extends Agm65Entity {

    /** 最大转向速度：18°/秒 ÷ 20 tick/秒 = 0.9°/tick */
    private static final float MAX_TURN_SPEED_PER_TICK = 18.0f / 20.0f;

    /** 近炸引信半径（格） */
    private static final double PROXIMITY_FUZE_RADIUS = 5.0;

    /** 低空保护高度：目标离地低于此值（格）时雷达弹放弃追踪转惯导 */
    private static final double MIN_TRACK_HEIGHT = 25.0;

    /** 低空/丢失检测间隔（tick，每秒检测一次 = 20 tick） */
    private static final int LOW_ALT_CHECK_INTERVAL = 20;

    /** 惯导模式：为 true 时不再转向目标，保持当前方向直线飞行 */
    private boolean inertialGuidance;

    /** 最后一次有效追踪的目标位置（丢失/低空后朝这里飞） */
    private Vec3 lastKnownTargetPos;

    /** 低空/丢失检测计时器 */
    private int lowAltCheckTick;

    public Aim120Entity(EntityType<? extends Agm65Entity> type, Level level) {
        super(type, level);
    }

    @Override
    public void turn(Vec3 vec3, float turnSpeed) {
        super.turn(recalculateToVec(), Math.min(turnSpeed, MAX_TURN_SPEED_PER_TICK));
    }

    /**
     * 重新计算转向方向（无高抛版：瞄准点就是目标位置本身，仅做提前量解算）：
     * - 惯导模式：保持当前朝向（直线飞行），不转向；
     * - guideType 0（锁实体）：目标实体当前位置，按目标运动速度解算提前量；
     * - guideType 1（锁坐标）：锁定坐标，静态提前量解算；
     * - 无目标/非服务端：保持当前朝向。
     */
    private Vec3 recalculateToVec() {
        Vec3 toVec = this.getLookAngle();

        // 惯导模式：朝最后已知目标位置直线飞行（无目标信息则保持当前朝向）
        if (inertialGuidance) {
            if (lastKnownTargetPos != null && level() instanceof ServerLevel) {
                toVec = RangeTool.calculateFiringSolution(position(), lastKnownTargetPos,
                        Vec3.ZERO, getDeltaMovement().length(), 0.0);
            }
            return toVec;
        }

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
                return;
            }

            // 雷达弹制导规则：每秒检测一次目标状态
            if (++lowAltCheckTick >= LOW_ALT_CHECK_INTERVAL) {
                lowAltCheckTick = 0;
                updateGuidanceState();
            }
        }
    }

    /**
     * 每秒更新一次制导状态：
     * 1. 目标离地低于 25 格 → 进入惯导（放弃追踪，朝最后已知位置直线飞行）；
     * 2. 目标丢失（无目标/实体不存在）→ 朝最后已知位置飞；
     * 3. 惯导期间目标重现且离地 ≥ 25 格 → 恢复追踪。
     */
    private void updateGuidanceState() {
        if (!(level() instanceof ServerLevel)) return;

        Entity target = EntityFindUtil.findEntity(this.level(), this.getTargetUUID());
        boolean hasTarget = getGuideType() == 0
                && !this.getTargetUUID().equals("none")
                && target != null;

        if (hasTarget) {
            // 记录最后已知目标位置
            lastKnownTargetPos = target.position();

            // 目标离地高度 < 25 格 → 进入惯导
            double heightAboveGround = target.getY()
                    - level().getHeight(Heightmap.Types.WORLD_SURFACE, target.blockPosition().getX(), target.blockPosition().getZ());
            if (heightAboveGround < MIN_TRACK_HEIGHT) {
                enterInertialGuidance();
            } else {
                // 目标有效且高度正常 → 退出惯导（若之前处于惯导）
                inertialGuidance = false;
            }
        } else {
            // 目标丢失：若之前有追踪记录，进入惯导朝最后已知位置飞
            if (lastKnownTargetPos != null) {
                enterInertialGuidance();
            }
        }
    }

    /** 进入惯导：记录目标消失位置，停止转向（直线飞行直到自毁或目标重现） */
    private void enterInertialGuidance() {
        if (!inertialGuidance) {
            inertialGuidance = true;
        }
    }
}
