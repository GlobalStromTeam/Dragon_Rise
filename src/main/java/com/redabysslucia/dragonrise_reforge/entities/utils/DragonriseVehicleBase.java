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
 * 动画名按实体注册 id 自动推导，例如 f14 -> animation.f14.gear_up，
 * zbd04a -> animation.zbd04a.splash_on。没有对应动画的载具调用是安全空操作。
 */
public abstract class DragonriseVehicleBase extends VehicleEntity {

    private boolean wasSplash;
    private int splashCheckCooldown;

    public DragonriseVehicleBase(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Override
    public void baseTick() {
        super.baseTick();
        this.tickGearAnimation();
        this.tickSplashAnimation();
    }

    private void tickGearAnimation() {
        if (!level().isClientSide()) return;
        var animationInstance = getAnim();
        if (animationInstance == null) return;
        var ctx = animationInstance.getContext();

        boolean gearUp = (getGearUp() && getSynchedGearRot() > 0 && getSynchedGearRot() < 1) || getSynchedGearRot() == 1f;
        boolean gearDown = (!getGearUp() && getSynchedGearRot() > 0 && getSynchedGearRot() < 1) || getSynchedGearRot() == 0f;

        String prefix = "animation." + EntityType.getKey(getType()).getPath();
        String gearUpAnim = prefix + ".gear_up";
        String gearDownAnim = prefix + ".gear_down";

        if (gearUp && !getWasGearUp()) {
            ctx.stopAnimation(gearDownAnim, 0);
            ctx.playAnimation(gearUpAnim, AnimationPlayType.PLAY_ONCE_HOLD, 0);
        } else if (gearDown && getWasGearUp()) {
            ctx.stopAnimation(gearUpAnim, 0);
            ctx.playAnimation(gearDownAnim, AnimationPlayType.PLAY_ONCE_HOLD, 0);
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
}
