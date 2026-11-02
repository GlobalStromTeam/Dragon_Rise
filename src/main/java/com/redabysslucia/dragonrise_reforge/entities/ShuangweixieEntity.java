package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.client.animation.AnimationPlayType;
import com.atsuishio.superbwarfare.data.vehicle.subdata.EngineInfo;
import com.atsuishio.superbwarfare.entity.vehicle.DroneEntity;
import com.atsuishio.superbwarfare.entity.vehicle.utils.VehicleEngineUtils;
import com.redabysslucia.dragonrise_reforge.init.ModEntities;
import com.google.gson.JsonObject;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * 双尾蝎无人机（TB-001 一类的大型固定翼无人机），以载具形式接入 superbwarfare。
 * <p>
 * 遥控链路（绑定 / 席位 / 开火 / HUD / 相机 / 回收）全部在 {@link AbstractRemotePilotDrone}，
 * 本类只负责"它是一架固定翼"这件事：飞行引擎、起落架动画、地面摆正。
 * <p>
 * <b>操控</b>：直接调用 SBW 自己的固定翼引擎
 * {@code VehicleEngineUtils.aircraftEngine(vehicle, EngineInfo.Aircraft)}，
 * 参数取载具 JSON 的 {@code EngineInfo}，与 J-20 / F-15 同一个调参入口。
 * 它按"载具上必须有乘员"硬门控（无乘员会清空输入、掉功率、机头下坠），
 * 遥控时由 {@code AircraftEngineRemotePilotMixin} 在仅该方法内部把遥控者当作乘员喂进去。
 * <p>
 * <b>席位</b>：座位数 = 同时遥控人数上限。只有 1 号席位能操控（其余席位与 SBW 其他座位一致：
 * 能看、能用自己席位的武器、能切武器，但不驾驶；由 SeatOneOnly 系列 mixin 在服务端强制）。
 * <p>
 * <b>弹药</b>：完全走 SBW 原生机制 —— 武器从载具容器的弹药物品装填；
 * 操作者背包里的创造弹药盒则通过 {@code InventoryTool.hasCreativeAmmoBoxForVehicle}
 * （载具数据 {@code UsePassengerCreativeAmmoBox} + {@code InventoryToolRemotePilotBoxMixin}）
 * 转成"无限备弹"。
 */
public class ShuangweixieEntity extends AbstractRemotePilotDrone {

    /** DroneEntity.baseTick() 末尾的无人机自稳系数（roll *= 0.9），固定翼需要完整坡度，见 {@link #baseTick()} */
    private static final float DRONE_ROLL_DAMP = 0.9f;

    /** 起落架收起动画（见 shuangweixie.animation.json） */
    private static final String GEAR_UP_ANIM = "animation.shuangweixie.gear_up";

    /** 起落架放下动画 */
    private static final String GEAR_DOWN_ANIM = "animation.shuangweixie.gear_down";

    /**
     * 上一 tick 起落架是否处于"收起"状态（仅客户端）。
     * <p>
     * 与 superb 本体 A10 / 本模组 F18 一致：收放本身由 superb 的固定翼引擎
     * （{@code VehicleEngineUtils.aircraftEngine}，需载具数据 {@code HasGear=true}）按空格键推进，
     * 这里只负责在"收起/放下"翻转时播放对应动画。
     */
    private boolean dr$wasGearUp;

    public ShuangweixieEntity(EntityType<? extends DroneEntity> type, Level world) {
        super(type, world);
    }

    @Override
    protected EntityType<?> dr$containerType() {
        return ModEntities.SHUANGWEIXIE.get();
    }

    // ==================== 飞行：交给 SBW 的固定翼引擎 ====================

    @Override
    public void travel() {
        // 注意：两侧都要跑（不能只跑服务端）。
        // SBW 的 roll 是普通字段、不参与 entityData 同步，各客户端是靠"本地也跑一遍引擎"得到坡度的
        // —— 输入位与 DELTA_ROT / MOUSE_SPEED_X/Y 都是同步量，客户端复算结果与服务端一致。
        // 若只在服务端跑，客户端 roll 恒为 0，遥控视角的 cameraRoll 就永远为 0
        // （表现为：视角跟着抬头低头，但不跟着压坡度滚转）。
        EngineInfo cached = this.getEngineInfo();
        EngineInfo.Aircraft aircraft = cached instanceof EngineInfo.Aircraft ac
                ? ac
                : this.dr$aircraftConfig();
        this.setEngineInfo(aircraft);

        // SBW 的固定翼引擎（与 J-20 同一套代码）。"是否有乘员"的门控由 mixin 放行。
        VehicleEngineUtils.aircraftEngine(this, aircraft);
    }

    @Override
    public void baseTick() {
        super.baseTick();   // 内含 VehicleEntity.baseTick → travel()（上面那套飞机引擎）

        // 起落架收放动画（空格键触发），仅客户端
        this.dr$tickGearAnimation();

        // DroneEntity.baseTick() 在 super 之后会执行 setZRot(getRoll() * 0.9f)（无人机自稳阻尼）。
        // 那是四轴的自稳，对固定翼来说每 tick 削掉 10% 坡度会压不出杆，这里反算回去。
        this.setZRot(this.getRoll() / DRONE_ROLL_DAMP);

        // 地面自动摆正。
        // SBW 的飞机引擎里舵面输入和"自动回正"都写在 `if (!onGround())` 里，
        // 也就是停在地面上引擎完全不碰姿态；加上上面那行抵消了四轴自稳阻尼，
        // 于是会出现"带坡度落地后一直斜着不起来"。这里补一个只在接地时生效的缓慢回正
        // （不影响地面滑跑：地面上引擎本来也不响应鼠标姿态输入）。
        if (this.onGround()) {
            this.setXRot(this.getXRot() * 0.75f);
            this.setZRot(this.getRoll() * 0.75f);
        }
    }

    /**
     * 起落架收放动画状态机（参照 superb 本体 A10 / 本模组 {@code DragonriseVehicleBase}）。
     * <p>
     * 收放逻辑由 superb 固定翼引擎按空格键推进（{@code gearUp} / {@code synchedGearRot}，
     * 需载具数据 {@code HasGear=true}）：空中按空格收起、触地自动放下。
     * 这里只在"收起/放下"状态翻转时播放对应动画，并保持到最后一帧（PLAY_ONCE_HOLD）。
     * 出生时绑定姿态即"放下"、{@code synchedGearRot=0}，故无需初始动画。
     */
    private void dr$tickGearAnimation() {
        if (!this.level().isClientSide()) {
            return;
        }
        var ani = this.getAnimationInstance();
        if (ani == null) {
            return;
        }
        var ctx = ani.getContext();

        boolean gearUp = (this.getGearUp() && this.getSynchedGearRot() > 0 && this.getSynchedGearRot() < 1)
                || this.getSynchedGearRot() == 1f;
        boolean gearDown = (!this.getGearUp() && this.getSynchedGearRot() > 0 && this.getSynchedGearRot() < 1)
                || this.getSynchedGearRot() == 0f;

        if (gearUp && !this.dr$wasGearUp) {
            ctx.stopAnimation(GEAR_DOWN_ANIM, 0);
            ctx.playAnimation(GEAR_UP_ANIM, AnimationPlayType.PLAY_ONCE_HOLD, 0);
        } else if (gearDown && this.dr$wasGearUp) {
            ctx.stopAnimation(GEAR_UP_ANIM, 0);
            ctx.playAnimation(GEAR_DOWN_ANIM, AnimationPlayType.PLAY_ONCE_HOLD, 0);
        }
        this.dr$wasGearUp = gearUp;
    }

    /**
     * 用载具 JSON 的 {@code EngineInfo} 构造 SBW 的飞机引擎配置。
     * 不走 SBW 的序列化（那是 Kotlin 内部路径），直接逐项取值，缺省值取本模组飞机的常用值。
     */
    private EngineInfo.Aircraft dr$aircraftConfig() {
        JsonObject cfg = this.computed().getEngineInfo();

        EngineInfo.Aircraft ac = new EngineInfo.Aircraft();
        ac.setIncrement(dr$float(cfg, "Increment", 1.2f));
        ac.setDecrement(dr$float(cfg, "Decrement", 1.0f));
        ac.setPitchSpeed(dr$float(cfg, "PitchSpeed", 1.0f));
        ac.setYawSpeed(dr$float(cfg, "YawSpeed", 1.0f));
        ac.setRollSpeed(dr$float(cfg, "RollSpeed", 1.0f));
        // 升力系数：无人机翼载低，适当放大以便短场起飞；太飘就调小
        ac.setLiftSpeed(dr$float(cfg, "LiftSpeed", 1.25f));
        ac.setSpeed(dr$float(cfg, "Speed", 1.0f));
        ac.setClampPitch(dr$float(cfg, "ClampPitch", 60f));
        ac.setClampRoll(dr$float(cfg, "ClampRoll", 60f));
        ac.setSpeedRate(dr$float(cfg, "SpeedRate", 1.0f));
        ac.setGearRotateAngle(dr$float(cfg, "GearRotateAngle", 85f));
        ac.setHasGear(dr$bool(cfg, "HasGear", false));
        ac.setHasStukaSound(dr$bool(cfg, "HasStukaSound", false));
        ac.setResistance(dr$float(cfg, "Resistance", 1.0f));
        ac.setEnergyCostRate(dr$double(cfg, "EnergyCostRate", 256.0));
        ac.setEngineSoundVolume(dr$float(cfg, "EngineSoundVolume", 0.9f));
        ac.setEngineStartSound(SoundEvents.EMPTY);
        return ac;
    }
}
