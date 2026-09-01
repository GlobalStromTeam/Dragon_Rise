package com.redabysslucia.dragonrise_reforge.entities.projectile;

import com.atsuishio.superbwarfare.client.animation.entity.BasicProjectileAnimationInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * GBU-24 制导炸弹实体（2000lb 激光制导炸弹）。
 * 继承 GuidedBombEntity：重力下落、穿水、命中即爆、激光架束/卫星制导。
 * 弹翼展开动画：投放后播放 animation.projectile.start（PLAY_ONCE_HOLD，保持末帧）。
 * 实体 id dragonrise_reforge:gbu_24 → 模型/贴图/动画自动解析。
 */
public class Gbu24Entity extends GuidedBombEntity {

    private final BasicProjectileAnimationInstance<?> anim =
            this.level().isClientSide ? new BasicProjectileAnimationInstance<>(this, false) : null;

    public Gbu24Entity(EntityType<? extends Gbu24Entity> type, Level level) {
        super(type, level);
    }

    /**
     * GBU-24 弹体更重，转向能力弱于 GBU-12（每次判定 3.75° = 每 tick 平均 0.75° = 15°/秒）。
     */
    @Override
    protected double getCorrectionDegreesPerTick() {
        return 3.75;
    }

    @Override
    public BasicProjectileAnimationInstance<?> getAnimationInstance() {
        return anim;
    }
}
