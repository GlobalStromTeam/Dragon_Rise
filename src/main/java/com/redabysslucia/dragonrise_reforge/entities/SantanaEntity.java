package com.redabysslucia.dragonrise_reforge.entities;

import com.redabysslucia.dragonrise_reforge.entities.utils.DragonriseVehicleBase;
// import net.minecraft.util.Mth;   // 速度敏感转向停用后不再使用，保留此行以便整段恢复
import com.atsuishio.superbwarfare.client.animation.AnimationPlayType;
import com.redabysslucia.dragonrise_reforge.client.sound.SantanaLoopSoundInstance;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * 大众桑塔纳（VW Santana，民用量产轿车）。
 * 5 座、无武装；轮子骨骼已按 SBW 约定改名：LeftFrontWheel→wheelL0Turn、RightFrontWheel→wheelR0Turn、
 * LeftBackWheel→wheelL1、RightBackWheel→wheelR1（前轮带 Turn 后缀负责转向）。
 *
 * <p><b>驾驶手感：纯 SBW 原生转向</b>
 * 本类<b>不覆写</b> {@code travel()}，也<b>不写</b> {@code rudderRot}，转向全部交回 superb 的轮式引擎：
 * <ul>
 *   <li>舵角上限 = {@code clamp(输入, -0.8, 0.8) * 0.75} = 0.6 rad，由 {@code SteeringSpeed}（0.13）决定每 tick 转多快；</li>
 *   <li>偏航 = {@code yRot -= 12 × 车速 × rudderRot}，左右轮差速由 {@code WheelDifferential}（-0.3）负责；</li>
 *   <li>前轮 Turn 骨骼与方向盘（{@code move_control}）都读 {@code rudderRot}，所以视觉自动跟随。</li>
 * </ul>
 * 原先"车速越高舵角越小 / 转向越慢"的那套桑塔纳专用转向实现已整段注释，留档在文件末尾。
 *
 * <p><b>漂移相关代码同样已停用</b>：本类不继承 {@code HandbrakeDriftVehicle}，
 * 手刹漂移的一整套参数覆写也注释在文件末尾；按住空格只是 SBW 原版的手刹 / 刹车行为。
 */
@SuppressWarnings("removal")
public class SantanaEntity extends DragonriseVehicleBase {

    public SantanaEntity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    /* ==================== V 键循环动画 + 循环音 ==================== */

    /** 涂装动画里那一段循环动画的名字（assets/dragonrise_reforge/animations/santana.animation.json） */
    public static final String LIGHT_ANIMATION = "animation.santana.lighton";

    /** 是否正在播放点灯循环动画 / 循环音（同步字段，旁观者也能看到听到） */
    private static final EntityDataAccessor<Boolean> DR$LIGHT_ON =
            SynchedEntityData.defineId(SantanaEntity.class, EntityDataSerializers.BOOLEAN);

    @OnlyIn(Dist.CLIENT)
    private SantanaLoopSoundInstance dr$loopSound;

    @OnlyIn(Dist.CLIENT)
    private boolean dr$animPlaying;

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DR$LIGHT_ON, false);
    }

    public boolean dr$isLightOn() {
        return this.entityData.get(DR$LIGHT_ON);
    }

    /** 服务端调用：翻转开关，客户端通过同步字段收到 */
    public void dr$toggleLight() {
        this.entityData.set(DR$LIGHT_ON, !this.dr$isLightOn());
    }

    @Override
    public void baseTick() {
        super.baseTick();
        if (this.level().isClientSide()) {
            this.dr$tickLight();
        }
    }

    /** 客户端每 tick：按开关状态起停循环动画与循环音 */
    @OnlyIn(Dist.CLIENT)
    private void dr$tickLight() {
        boolean on = this.dr$isLightOn();

        // 动画
        if (on != this.dr$animPlaying) {
            var animationInstance = this.getAnimationInstance();
            if (animationInstance != null) {
                var ctx = animationInstance.getContext();
                if (on) {
                    ctx.playAnimation(LIGHT_ANIMATION, AnimationPlayType.LOOP, 0);
                } else {
                    ctx.stopAnimation(LIGHT_ANIMATION, 0);
                }
                this.dr$animPlaying = on;
            }
        }

        // 循环音
        if (on) {
            if (this.dr$loopSound == null) {
                this.dr$loopSound = new SantanaLoopSoundInstance(this);
                net.minecraft.client.Minecraft.getInstance().getSoundManager().play(this.dr$loopSound);
            }
        } else if (this.dr$loopSound != null) {
            net.minecraft.client.Minecraft.getInstance().getSoundManager().stop(this.dr$loopSound);
            this.dr$loopSound = null;
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        if (this.level().isClientSide() && this.dr$loopSound != null) {
            net.minecraft.client.Minecraft.getInstance().getSoundManager().stop(this.dr$loopSound);
            this.dr$loopSound = null;
        }
        super.remove(reason);
    }

    @Override
    public float getTurretMaxHealth() {
        return 20;
    }

    @Override
    public float getWheelMaxHealth() {
        return 30;
    }

    @Override
    public float getEngineMaxHealth() {
        return 50;
    }

    /* ==================== 速度敏感转向（已停用，留档，仅 1201 停用） ==================== */
    //
    // 说明：下面这一整段是桑塔纳专用的"车速越高舵角越小、转向越慢"实现，
    // 它会覆写 travel() 并把 SBW 算出来的 rudderRot 换成本类的平滑舵角，再用
    // yRot 反向补偿差量。停用后本类完全走 SBW 原生转向，上面这些行为全部消失。
    // 想恢复：把整段取消注释，并把文件头被注释的 Mth import 一起放开即可。
    //
    // /** SBW {@code wheelEngine} 的舵角上限：{@code clamp(..., -0.8, 0.8) * 0.75}。 */
    // private static final float STEER_MAX_ANGLE = 0.6f;
    //
    // /** SBW 每 tick 的偏航系数：{@code yRot -= 12 × 车速 × rudderRot}。 */
    // private static final double SUPERB_YAW_RATE = 12.0;
    //
    // /* ---- 速度敏感转向的参数 ---- */
    //
    // /** 本类自己维护的平滑舵角（rad）。渲染和物理都改用它，见 {@link #applySpeedSensitiveSteering()}。 */
    // private float steerAngle;
    //
    // /**
    //  * 速度敏感转向的接入点：先让 SBW 正常算一遍（油门 / 转向 / 抓地），
    //  * 再把它的舵角换成"受车速限制的角度"，并把车头因为差量多转 / 少转的部分补回去。
    //  */
    // @Override
    // public void travel() {
    //     super.travel();
    //     this.applySpeedSensitiveSteering();
    // }
    //
    // /* ==================== 速度敏感转向 ==================== */
    //
    // /** 舵角 / 转向速率开始衰减的参考车速（格/tick）。1.0 ≈ 72 km/h。 */
    // protected double steerSpeedRef() {
    //     return 1.0;
    // }
    //
    // /** 低速时的舵角系数（1.0 = SBW 原值 0.6 rad 满舵）。 */
    // protected float steerAngleFactorLow() {
    //     return 1.5f;
    // }
    //
    // /** 接近参考车速时的舵角系数：0.32 表示只剩三成舵角。 */
    // protected float steerAngleFactorHigh() {
    //     return 0.2f;
    // }
    //
    // /** 低速时每 tick 舵角能走多少（0.055 rad ≈ 11 tick 打满）。 */
    // protected float steerRateLow() {
    //     return 0.030f;
    // }
    //
    // /** 高速时每 tick 舵角能走多少（0.010 rad ≈ 打满要 20+ tick，明显"变重变慢"）。 */
    // protected float steerRateHigh() {
    //     return 0.030f;
    // }
    //
    // /** 当前车速系数 0~1：舵角上限与转向速率都按它缩放。 */
    // public float getSteerSpeedFactor() {
    //     double hs = getDeltaMovement().horizontalDistance();
    //     return (float) Mth.clamp(hs / Math.max(steerSpeedRef(), 1.0E-3), 0.0, 1.0);
    // }
    //
    // /** 当前车速下的最大舵角（rad，带符号由输入决定，这里返回幅值）。 */
    // public float getSteerMaxAngle() {
    //     return STEER_MAX_ANGLE * Mth.lerp(getSteerSpeedFactor(), steerAngleFactorLow(), steerAngleFactorHigh());
    // }
    //
    // /** 转向输入（+1 = 左 / -1 = 右 / 0 = 直行）。与 SBW 的 rudderRot 符号一致。 */
    // private float steerInput() {
    //     if (rightInputDown()) return -1f;
    //     if (leftInputDown()) return 1f;
    //     return 0f;
    // }
    //
    // /**
    //  * 每秒重算一次舵角：目标角 = 输入 × 速度限制后的最大舵角，
    //  * 每 tick 最多走近 {@code rate}（高速更小），然后：
    //  * <ol>
    //  *   <li>把 SBW 本 tick 按它自己的 rudderRot 转掉的车头角度换成我们的舵角
    //  *       （差值 = {@code 12 × 车速 × (原舵角 − 新舵角)}，直接反向补回 yRot）；</li>
    //  *   <li>把 {@code rudderRot} 写成我们的舵角，轮胎骨骼与方向盘渲染都读它。</li>
    //  * </ol>
    //  */
    // private void applySpeedSensitiveSteering() {
    //     if (level() == null) return;
    //
    //     double hs = getDeltaMovement().horizontalDistance();
    //     float f = getSteerSpeedFactor();
    //
    //     float maxAngle = getSteerMaxAngle();
    //     float rate = Mth.lerp(f, steerRateLow(), steerRateHigh());
    //
    //     float target = steerInput() * maxAngle;
    //     this.steerAngle += Mth.clamp(target - this.steerAngle, -rate, rate);
    //     // 数值兜底：别让角度超过当前车速允许的上限
    //     this.steerAngle = Mth.clamp(this.steerAngle, -maxAngle, maxAngle);
    //
    //     float rawRudder = getRudderRot();
    //     double sign = getPower() > 0f ? 1.0 : -1.0;
    //     double correction = SUPERB_YAW_RATE * hs * sign * (rawRudder - this.steerAngle);
    //     setYRot((float) (getYRot() + correction));
    //     setRudderRot(this.steerAngle);
    // }

    /* ==================== 漂移代码（已停用，留档） ==================== */
    //
    // 说明：下面这些是此前"按住空格 = 漂移"时的参数覆写，它们属于
    // HandbrakeDriftVehicle。现在桑塔纳不再继承那个基类，全部注释掉。
    // 想恢复漂移：把类声明改回 extends HandbrakeDriftVehicle，再把这些方法取消注释即可。
    //
    // /** 约 13 km/h（0.18 格/tick）以上才进入漂移；低于此速度按住空格就是纯手刹。 */
    // @Override
    // protected double driftMinSpeed() {
    //     return 0.18;
    // }
    //
    // /** 低速端目标侧滑角 8 度。 */
    // @Override
    // protected float driftSlipLowSpeed() {
    //     return 8f;
    // }
    //
    // /** 高速端目标侧滑角 24 度。 */
    // @Override
    // protected float driftSlipHighSpeed() {
    //     return 24f;
    // }
    //
    // /** 参考车速 0.85 格/tick（约 61 km/h）：到这个速度才张开到 24 度、偏航给满。 */
    // @Override
    // protected double driftSlipSpeedRef() {
    //     return 0.85;
    // }
    //
    // /** 伺服增益 1.15。 */
    // @Override
    // protected double driftYawGain() {
    //     return 1.15;
    // }
    //
    // /** 速率上限 5.5 度/tick。 */
    // @Override
    // protected double driftMaxYawRate() {
    //     return 5.5;
    // }
    //
    // /** 介入基础速率 0.08/tick。 */
    // @Override
    // protected double driftBlendIn() {
    //     return 0.08;
    // }
    //
    // /** 侧向冲量 0.022。 */
    // @Override
    // protected double driftLateralPush() {
    //     return 0.022;
    // }
    //
    // /** 沿车头净保留 0.992。 */
    // @Override
    // protected double driftDrag() {
    //     return 0.992;
    // }
    //
    // /** 侧向净保留 0.995。 */
    // @Override
    // protected double driftLateralGain() {
    //     return 0.995;
    // }
    //
    // /** 补推力上限 0.05。 */
    // @Override
    // protected double driftThrust() {
    //     return 0.05;
    // }
    //
    // /** 补推力增益 0.25。 */
    // @Override
    // protected double driftHoldGain() {
    //     return 0.25;
    // }
    //
    // /** 漂移时仍能加速到的车速上限 0.75（约 54 km/h）。 */
    // @Override
    // protected double driftSpeedCap() {
    //     return 0.75;
    // }
    //
    // /** 进入速度作为保速下限，每 tick 衰减 0.995。 */
    // @Override
    // protected double driftHoldDecay() {
    //     return 0.995;
    // }
    //
    // /** 纯手刹捏合速度 0.15/tick。 */
    // @Override
    // protected double handbrakeBlendIn() {
    //     return 0.15;
    // }
    //
    // /** 侧滑上限 72 度。 */
    // @Override
    // protected float maxSlipAngle() {
    //     return 72f;
    // }
    //
    // /** 松手回正 0.22。 */
    // @Override
    // protected double recoverGrip() {
    //     return 0.22;
    // }
    //
    // /** 回正期每 tick 保速 0.95。 */
    // @Override
    // protected double recoverScrub() {
    //     return 0.95;
    // }
    //
    // /** 纯手刹锁死 0.92。 */
    // @Override
    // protected double handbrakeLockDrag() {
    //     return 0.92;
    // }
}
