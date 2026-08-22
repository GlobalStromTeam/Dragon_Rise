package com.redabysslucia.dragonrise_reforge.entities.projectile;

import com.atsuishio.superbwarfare.client.animation.entity.BasicProjectileAnimationInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * LS-6 雷石-6 滑翔制导炸弹实体（仅卫星制导）。
 * 继承 GuidedBombEntity：重力下落、穿水、命中即爆。
 * 差异：
 * 1. 仅卫星制导：只向锁定的地面坐标修正，未锁定时纯弹道下落（无激光架束）；
 * 2. 弹翼展开动画：投放后播放 animation.projectile.start（PLAY_ONCE_HOLD，保持末帧）；
 * 3. 滑翔弹翼，转向能力强（每次判定 15° = 60°/秒，与 JDAM-ER 同级）。
 * 实体 id dragonrise_reforge:ls_6 → 模型/贴图/动画自动解析。
 */
public class Ls6Entity extends GuidedBombEntity {

    private final BasicProjectileAnimationInstance<?> anim =
            this.level().isClientSide ? new BasicProjectileAnimationInstance<>(this, false) : null;

    public Ls6Entity(EntityType<? extends Ls6Entity> type, Level level) {
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
        // 仅卫星制导：只向锁定的地面坐标修正；未锁定（guideType 0）时纯弹道下落
        if (getGuideType() == 1) {
            currentTarget = getTargetPos();
        }
    }
}
