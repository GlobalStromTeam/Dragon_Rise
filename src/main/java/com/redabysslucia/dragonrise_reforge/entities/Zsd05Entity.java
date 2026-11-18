package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.client.animation.AnimationPlayType;
import com.redabysslucia.dragonrise_reforge.entities.utils.DragonriseVehicleBase;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * ZSD-05 两栖装甲输送车。
 *
 * <p>防浪板状态机是从 {@link DragonriseVehicleBase} 复制过来的一份**独立实现**（而不是继续共用基类那份），
 * 这样以后调整 zsd05 的动画时长、检查频率或展开角度时不会连带影响 ZBD05/ZTD05：
 * <ul>
 *   <li>入水且上一帧不在水中 → 播 {@code animation.zsd05.splash_on}（PLAY_ONCE_HOLD）</li>
 *   <li>出水且上一帧在水中 → 播 {@code animation.zsd05.splash_off}（PLAY_ONCE_HOLD）</li>
 *   <li>每 {@link #SPLASH_CHECK_INTERVAL} tick 检查一次水位，避免每帧遍历流体</li>
 * </ul>
 *
 * <p>动画文件 {@code animations/bedrock/vehicle/zsd05.animation.json} 同样是 zbd05 的独立副本
 * （两条防浪板片段的骨骼关键帧按 zsd05 自己的骨骼名 {@code animation.zsd05.*} 命名）。
 * 因为两台车的 10 根防浪板骨骼 pivot 完全一致，目前两份动画的数值也一致；
 * 若要让 zsd05 动得不一样，只需改同目录下 zsd05 的这份文件，不影响 zbd05。
 */
public class Zsd05Entity extends DragonriseVehicleBase {

    /** 防浪板两条动画的固定名字（与 zbd05 解耦，改名只需动这里）。 */
    private static final String SPLASH_ON = "animation.zsd05.splash_on";
    private static final String SPLASH_OFF = "animation.zsd05.splash_off";
    /** 水位检查间隔（tick）。 */
    private static final int SPLASH_CHECK_INTERVAL = 10;

    /** 上一帧是否在水中（模型出厂即展开防浪板，所以初始视为"展开"）。 */
    private boolean zsd05WasSplash = true;
    /** 距离下次检查水位还剩多少 tick。 */
    private int zsd05SplashCooldown;

    public Zsd05Entity(EntityType<Zsd05Entity> type, Level world) {
        super(type, world);
    }

    @Override
    protected boolean isSplashDefaultOpen() {
        // 模型里防浪板默认展开，陆地初始先播 splash_off 收起，入水再 splash_on 展开。
        return true;
    }

    /**
     * 独立的一份防浪板状态机（基类同名方法是 protected，这里完全覆写，基类那份不再执行）。
     */
    @Override
    protected void tickSplashAnimation() {
        if (!level().isClientSide()) {
            return;
        }
        if (--this.zsd05SplashCooldown > 0) {
            return;
        }
        this.zsd05SplashCooldown = SPLASH_CHECK_INTERVAL;

        var animationInstance = getAnimationInstance();
        if (animationInstance == null) {
            return;
        }
        var ctx = animationInstance.getContext();

        boolean inWater = isInWater();
        if (inWater && !this.zsd05WasSplash) {
            ctx.stopAnimation(SPLASH_OFF, 0);
            ctx.playAnimation(SPLASH_ON, AnimationPlayType.PLAY_ONCE_HOLD, 0);
        } else if (!inWater && this.zsd05WasSplash) {
            ctx.stopAnimation(SPLASH_ON, 0);
            ctx.playAnimation(SPLASH_OFF, AnimationPlayType.PLAY_ONCE_HOLD, 0);
        }
        this.zsd05WasSplash = inWater;
    }
}
