package com.redabysslucia.dragonrise_reforge.entities.utils;

import com.atsuishio.superbwarfare.client.animation.AnimationPlayType;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * 整合包载具公共基类，集中处理由实体逻辑驱动的动画：
 * 1. 起落架（参照 superb 本体 A10）：收起/放下时播放
 *    animation.&lt;载具id&gt;.gear_up / gear_down（PLAY_ONCE_HOLD）。
 * 2. 防浪板（两栖载具）：进入水中播放 animation.&lt;载具id&gt;.splash_on，
 *    离开水播放 animation.&lt;载具id&gt;.splash_off（PLAY_ONCE_HOLD）。
 * 3. 引擎加力（喷气式飞机）：按住加速键（Ctrl）播放 animation.&lt;载具id&gt;.engine_on，
 *    松开播放 animation.&lt;载具id&gt;.engine_off（PLAY_ONCE_HOLD）。
 * 动画名按实体注册 id 自动推导，例如 f14 -> animation.f14.gear_up，
 * zbd04a -> animation.zbd04a.splash_on。没有对应动画的载具调用是安全空操作。
 */
public abstract class DragonriseVehicleBase extends VehicleEntity {

    private boolean wasSplash;
    private boolean wasEngineOn;
    private int splashCheckCooldown;
    private boolean gearAnimInitialized;

    public DragonriseVehicleBase(EntityType<?> type, Level level) {
        super(type, level);
        // 模型默认展开防浪板的载具（如 ZTD05/ZBD05）：陆地初始先由 splash_off 收起
        this.wasSplash = isSplashDefaultOpen();
    }

    /**
     * 防浪板在模型中的默认姿态是否为展开。
     * 默认 false（收起，如 ZBL08/ZBD04A/ZSL10）：陆地初始无需播放动画；
     * 返回 true（展开，如 ZTD05/ZBD05）时，陆地初始会先播放 splash_off 收起防浪板。
     */
    protected boolean isSplashDefaultOpen() {
        return false;
    }

    /**
     * 起落架在模型中的默认姿态是否为收起（需要初始播放放下动画）。
     * 默认 false：绑定姿态即"放下"，初始无需动画；
     * 返回 true 的载具（绑定姿态为收起）出生后处于放下状态时会先播放一次放下动画。
     */
    protected boolean isGearUpByDefault() {
        return false;
    }

    /**
     * 动画文件命名与内容是否相反（gear_up 文件内容是放下、gear_down 文件内容是收起）。
     * 默认 false：gear_up=收起 / gear_down=放下（与 superb 引擎状态一致）。
     */
    protected boolean swapGearAnimations() {
        return false;
    }

    @Override
    public void baseTick() {
        super.baseTick();
        this.tickGearAnimation();
        this.tickSplashAnimation();
        this.tickEngineAnimation();
    }

    private void tickGearAnimation() {
        if (!level().isClientSide()) return;
        var animationInstance = getAnim();
        if (animationInstance == null) return;
        var ctx = animationInstance.getContext();

        String prefix = "animation." + EntityType.getKey(getType()).getPath();
        String gearUpAnim = prefix + ".gear_up";
        String gearDownAnim = prefix + ".gear_down";

        // 动画命名与内容相反时对调：收起动作播 gear_down 文件、放下动作播 gear_up 文件
        boolean swap = swapGearAnimations();
        String retractAnim = swap ? gearDownAnim : gearUpAnim;   // 收起动作播放的动画
        String extendAnim = swap ? gearUpAnim : gearDownAnim;    // 放下动作播放的动画

        // 模型默认收起、但当前语义为"放下"（synchedGearRot == 0）的载具：
        // 出生后主动播放一次放下动画，让轮子落下来（否则状态机只在状态变化时播放，
        // 初始收起与"放下"状态不一致）。
        if (!gearAnimInitialized) {
            gearAnimInitialized = true;
            if (isGearUpByDefault() && !getWasGearUp() && getSynchedGearRot() == 0f) {
                ctx.stopAnimation(retractAnim, 0);
                ctx.playAnimation(extendAnim, AnimationPlayType.PLAY_ONCE_HOLD, 0);
            }
        }

        boolean gearUp = (getGearUp() && getSynchedGearRot() > 0 && getSynchedGearRot() < 1) || getSynchedGearRot() == 1f;
        boolean gearDown = (!getGearUp() && getSynchedGearRot() > 0 && getSynchedGearRot() < 1) || getSynchedGearRot() == 0f;

        if (gearUp && !getWasGearUp()) {
            ctx.stopAnimation(extendAnim, 0);
            ctx.playAnimation(retractAnim, AnimationPlayType.PLAY_ONCE_HOLD, 0);
        } else if (gearDown && getWasGearUp()) {
            ctx.stopAnimation(retractAnim, 0);
            ctx.playAnimation(extendAnim, AnimationPlayType.PLAY_ONCE_HOLD, 0);
        }
        setWasGearUp(gearUp);
    }

    private void tickSplashAnimation() {
        if (!level().isClientSide()) return;
        if (--splashCheckCooldown > 0) return;
        splashCheckCooldown = 10;

        var animationInstance = getAnim();
        if (animationInstance == null) return;
        var ctx = animationInstance.getContext();

        boolean inWater = isInWater();
        String prefix = "animation." + EntityType.getKey(getType()).getPath();
        String splashOn = prefix + ".splash_on";
        String splashOff = prefix + ".splash_off";

        if (inWater && !wasSplash) {
            ctx.stopAnimation(splashOff, 0);
            ctx.playAnimation(splashOn, AnimationPlayType.PLAY_ONCE_HOLD, 0);
        } else if (!inWater && wasSplash) {
            ctx.stopAnimation(splashOn, 0);
            ctx.playAnimation(splashOff, AnimationPlayType.PLAY_ONCE_HOLD, 0);
        }
        wasSplash = inWater;
    }

    /**
     * 引擎加力动画状态机：按住加速键（Ctrl，即 sprintInputDown）播放
     * animation.&lt;载具id&gt;.engine_on，松开播放 engine_off。
     * 仅在载具有乘客（被驾驶）时响应，避免地面停放时误触。
     */
    private void tickEngineAnimation() {
        if (!level().isClientSide()) return;
        var animationInstance = getAnim();
        if (animationInstance == null) return;
        var ctx = animationInstance.getContext();

        boolean engineOn = !getPassengers().isEmpty() && sprintInputDown();

        String prefix = "animation." + EntityType.getKey(getType()).getPath();
        String engineOnAnim = prefix + ".engine_on";
        String engineOffAnim = prefix + ".engine_off";

        if (engineOn && !wasEngineOn) {
            ctx.stopAnimation(engineOffAnim, 0);
            ctx.playAnimation(engineOnAnim, AnimationPlayType.PLAY_ONCE_HOLD, 0);
        } else if (!engineOn && wasEngineOn) {
            ctx.stopAnimation(engineOnAnim, 0);
            ctx.playAnimation(engineOffAnim, AnimationPlayType.PLAY_ONCE_HOLD, 0);
        }
        wasEngineOn = engineOn;
    }
}
