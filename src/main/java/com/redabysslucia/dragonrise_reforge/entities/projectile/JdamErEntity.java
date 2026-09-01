package com.redabysslucia.dragonrise_reforge.entities.projectile;

import com.atsuishio.superbwarfare.client.animation.entity.BasicProjectileAnimationInstance;
import com.atsuishio.superbwarfare.tools.EntityFindUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * JDAM-ER 制导炸弹实体（增程型联合直接攻击弹药）。
 * 继承 GuidedBombEntity：重力下落、穿水、命中即爆、激光架束/卫星制导。
 *
 * 差异：
 * 1. 弹翼展开动画：投放后播放 animation.projectile.start（PLAY_ONCE_HOLD，保持末帧）；
 * 2. 可锁定实体：武器配置 SeekWeaponInfo 使用 OnlyLockEntity 锁定时（发射写入 targetUUID），
 *    炸弹实时向锁定实体位置修正，目标消失后按最后已知位置继续下坠。
 * 实体 id dragonrise_reforge:jdam_er → 模型/贴图/动画自动解析。
 */
public class JdamErEntity extends GuidedBombEntity {

    private final BasicProjectileAnimationInstance<?> anim =
            this.level().isClientSide ? new BasicProjectileAnimationInstance<>(this, false) : null;

    public JdamErEntity(EntityType<? extends JdamErEntity> type, Level level) {
        super(type, level);
        // 250kg 级制导炸弹：范围/威力缩减为基线的 1/4（1300/32 → 325/8）
        this.setExplosionDamage(325f);
        this.setExplosionRadius(8f);
    }

    /**
     * JDAM-ER 增程滑翔弹翼，转向能力远强于 GBU-12（每次判定 15° = 每 tick 平均 3° = 60°/秒）。
     */
    @Override
    protected double getCorrectionDegreesPerTick() {
        return 15.0;
    }

    @Override
    public BasicProjectileAnimationInstance<?> getAnimationInstance() {
        return anim;
    }

    /**
     * 目标判定（每 5 tick 由基类调用）：
     * - 实体锁定（OnlyLockEntity → targetUUID 有效）：跟踪锁定实体，目标消失后按最后已知位置；
     * - 无锁定时退回基类的激光架束/卫星制导。
     * 转向由基类每 tick 平滑执行（每次转判定角度的 1/5），不会抽搐。
     */
    @Override
    protected void updateTarget() {
        String uuid = getTargetUUID();
        if (uuid != null && !uuid.equals("none")) {
            Entity target = EntityFindUtil.findEntity(level(), uuid);
            if (target != null && target.isAlive()) {
                Vec3 targetPos = new Vec3(
                        target.getX(),
                        target.getY() + target.getBbHeight() * 0.5,
                        target.getZ());
                // 同步 targetPos，让客户端炸弹锁定框跟随目标显示
                setTargetPos(targetPos);
                currentTarget = targetPos;
                return;
            }
            // 目标已消失：沿用最后已知位置继续下坠
            if (getTargetPos() != null) {
                currentTarget = getTargetPos();
                return;
            }
        }
        super.updateTarget();
    }
}
