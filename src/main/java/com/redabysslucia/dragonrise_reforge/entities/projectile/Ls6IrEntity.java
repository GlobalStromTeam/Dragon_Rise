package com.redabysslucia.dragonrise_reforge.entities.projectile;

import com.atsuishio.superbwarfare.client.animation.entity.BasicProjectileAnimationInstance;
import com.atsuishio.superbwarfare.tools.EntityFindUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * LS-6 雷石-6 红外制导滑翔炸弹实体（卫星制导 + 实体锁定）。
 * 继承 GuidedBombEntity：重力下落、穿水、命中即爆。
 * 差异：
 * 1. 可锁定实体（OnlyLockEntity → targetUUID）：跟踪实体，目标消失按最后已知位置；
 *    也可卫星制导（guideType 1 → 锁定地面坐标）；无锁定时纯弹道下落（无激光架束）；
 * 2. 弹翼展开动画：投放后播放 animation.projectile.start（PLAY_ONCE_HOLD，保持末帧）；
 * 3. 滑翔弹翼，转向能力强（每次判定 15° = 60°/秒）。
 * 实体 id dragonrise_reforge:ls_6_ir → 模型/贴图/动画自动解析。
 */
public class Ls6IrEntity extends GuidedBombEntity {

    private final BasicProjectileAnimationInstance<?> anim =
            this.level().isClientSide ? new BasicProjectileAnimationInstance<>(this, false) : null;

    public Ls6IrEntity(EntityType<? extends Ls6IrEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected double getCorrectionDegreesPerTick() {
        return 15.0;
    }

    @Override
    public BasicProjectileAnimationInstance<?> getAnimationInstance() {
        return anim;
    }

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
                setTargetPos(targetPos);
                currentTarget = targetPos;
                return;
            }
            if (getTargetPos() != null) {
                currentTarget = getTargetPos();
                return;
            }
        }
        if (getGuideType() == 1) {
            currentTarget = getTargetPos();
        }
    }
}
