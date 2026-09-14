package com.redabysslucia.dragonrise_reforge.entities.utils;

import com.atsuishio.superbwarfare.client.animation.AnimationPlayType;
import com.atsuishio.superbwarfare.client.animation.entity.VehicleAnimationInstance;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.resource.model.VehicleModelReloadListener;
import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

/**
 * 整合包载具公共基类，集中处理由实体逻辑驱动的动画状态机：
 * 1. 起落架（参照 superb 本体 A10）：收起/放下时播放
 *    animation.&lt;载具id&gt;.gear_up / gear_down（PLAY_ONCE_HOLD）。
 * 2. 防浪板（两栖载具）：进入水中播放 animation.&lt;载具id&gt;.splash_on，
 *    离开水播放 animation.&lt;载具id&gt;.splash_off（PLAY_ONCE_HOLD）。
 * 3. 引擎加力（喷气式飞机）：按住加速键（Ctrl）播放 animation.&lt;载具id&gt;.engine_on，
 *    松开播放 animation.&lt;载具id&gt;.engine_off（PLAY_ONCE_HOLD）。
 * 4. 导弹发射架：切换武器时展开/收拢，播放 animation.&lt;载具id&gt;.missile_on / missile_off
 *    （PLAY_ONCE_HOLD）。是否应展开由 {@link #isMissileRackDeployed()} 决定（默认不启用）。
 *
 * 动画名默认按实体注册 id 自动推导，例如 f14 -> animation.f14.gear_up，
 * zbd04a -> animation.zbd04a.splash_on。没有对应动画的载具调用是安全空操作。
 * 动画文件命名与约定不同的载具（例如 tjgc 的起落架动画叫 bay_off / bay_on）覆写对应的
 * 动画名钩子即可，状态机本身无需改动。
 */
public abstract class DragonriseVehicleBase extends VehicleEntity {

    private boolean wasSplash;
    private boolean wasEngineOn;
    private boolean wasMissileRackDeployed;
    private int splashCheckCooldown;
    private boolean gearAnimInitialized;

    /** 涂装自带模型时使用的动画实例（懒创建，见 {@link #getSkinAnimationInstance()}） */
    private VehicleAnimationInstance<VehicleEntity> skinAnimation;
    private String skinAnimationKey;
    /** 创建涂装动画时所绑定的烘焙模型，用于识别资源重载后需要重建 */
    private Object skinAnimationModel;

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

    /**
     * 起落架"收起"动作播放的动画名（默认 animation.&lt;载具id&gt;.gear_up）。
     * 动画文件里叫别的名字时覆写，例如 tjgc 的收起动画是 animation.tjgc.bay_off。
     */
    protected String gearRetractAnimation(String prefix) {
        return prefix + (swapGearAnimations() ? ".gear_down" : ".gear_up");
    }

    /**
     * 起落架"放下"动作播放的动画名（默认 animation.&lt;载具id&gt;.gear_down）。
     * 动画文件里叫别的名字时覆写，例如 tjgc 的放下动画是 animation.tjgc.bay_on。
     */
    protected String gearExtendAnimation(String prefix) {
        return prefix + (swapGearAnimations() ? ".gear_up" : ".gear_down");
    }

    /**
     * 导弹发射架当前是否应处于展开状态（由选中的武器决定）。
     * 默认 false：没有发射架动画的载具不响应。
     */
    protected boolean isMissileRackDeployed() {
        return false;
    }

    /** 发射架"展开"动作播放的动画名（默认 animation.&lt;载具id&gt;.missile_on）。 */
    protected String missileRackDeployAnimation(String prefix) {
        return prefix + ".missile_on";
    }

    /** 发射架"收拢"动作播放的动画名（默认 animation.&lt;载具id&gt;.missile_off）。 */
    protected String missileRackRetractAnimation(String prefix) {
        return prefix + ".missile_off";
    }

    @Override
    public void baseTick() {
        super.baseTick();
        this.tickGearAnimation();
        this.tickSplashAnimation();
        this.tickEngineAnimation();
        this.tickMissileRackAnimation();
    }

    /**
     * 当前涂装若自带模型，则改用与该模型配套的涂装动画实例。
     *
     * <p>superb 的动画在加载时按模型烘焙骨骼索引
     * （{@code BedrockAnimation.createAnimation(file, BoneIndexProvider)}），且模型与动画**按文件名配对**
     * （{@code models/bedrock/vehicle/{id}_{涂装}.geo.json} ↔ {@code animations/bedrock/vehicle/{id}_{涂装}.animation.json}）。
     * 所以只换模型不换动画时，姿态打在旧模型的骨骼索引上，表现为"换涂装后动画不播放"。
     * 这里按同一套命名约定取涂装动画；模型或动画任一缺失都返回 null，完全走 superb 原逻辑。
     */
    @Nullable
    protected VehicleAnimationInstance<VehicleEntity> getSkinAnimationInstance() {
        if (!level().isClientSide()) return null;

        String skinId = getSkinId();
        if (skinId == null || skinId.isBlank()) return null;

        String key = EntityType.getKey(getType()).getPath() + "_" + skinId;
        ResourceLocation modelPath = new ResourceLocation(Dragonrise_reforge.MODID,
                "models/bedrock/vehicle/" + key + ".geo.json");
        ResourceLocation animPath = new ResourceLocation(Dragonrise_reforge.MODID,
                "animations/bedrock/vehicle/" + key + ".animation.json");

        var baked = VehicleModelReloadListener.INSTANCE.getModel(modelPath);
        if (baked == null) return null;

        if (skinAnimation != null && key.equals(skinAnimationKey) && skinAnimationModel == baked) {
            return skinAnimation;
        }
        if (VehicleModelReloadListener.INSTANCE.getAnimation(animPath) == null) return null;

        skinAnimation = new VehicleAnimationInstance<>(this, animPath);
        skinAnimationKey = key;
        skinAnimationModel = baked;
        return skinAnimation;
    }

    /** 渲染与状态机统一取"当前生效"的动画实例：涂装带模型时用涂装动画，否则用 superb 默认的 */
    @Override
    public VehicleAnimationInstance<VehicleEntity> getAnimationInstance() {
        VehicleAnimationInstance<VehicleEntity> skinAnim = getSkinAnimationInstance();
        return skinAnim != null ? skinAnim : super.getAnimationInstance();
    }

    private void tickGearAnimation() {
        if (!level().isClientSide()) return;
        var animationInstance = getAnimationInstance();
        if (animationInstance == null) return;
        var ctx = animationInstance.getContext();

        String prefix = "animation." + EntityType.getKey(getType()).getPath();
        String retractAnim = gearRetractAnimation(prefix);   // 收起动作播放的动画
        String extendAnim = gearExtendAnimation(prefix);     // 放下动作播放的动画

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

        var animationInstance = getAnimationInstance();
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
        var animationInstance = getAnimationInstance();
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

    /**
     * 导弹发射架动画状态机：当前选中的武器需要发射架时播放
     * animation.&lt;载具id&gt;.missile_on（展开，PLAY_ONCE_HOLD 保持末帧），
     * 切回其它武器时播放 missile_off（收拢）。
     * 判断条件由 {@link #isMissileRackDeployed()} 提供（默认恒为 false，即不启用）。
     */
    private void tickMissileRackAnimation() {
        if (!level().isClientSide()) return;
        var animationInstance = getAnimationInstance();
        if (animationInstance == null) return;
        var ctx = animationInstance.getContext();

        boolean deployed = isMissileRackDeployed();

        String prefix = "animation." + EntityType.getKey(getType()).getPath();
        String deployAnim = missileRackDeployAnimation(prefix);
        String retractAnim = missileRackRetractAnimation(prefix);

        if (deployed && !wasMissileRackDeployed) {
            ctx.stopAnimation(retractAnim, 0);
            ctx.playAnimation(deployAnim, AnimationPlayType.PLAY_ONCE_HOLD, 0);
        } else if (!deployed && wasMissileRackDeployed) {
            ctx.stopAnimation(deployAnim, 0);
            ctx.playAnimation(retractAnim, AnimationPlayType.PLAY_ONCE_HOLD, 0);
        }
        wasMissileRackDeployed = deployed;
    }
}
