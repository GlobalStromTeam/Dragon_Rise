package com.redabysslucia.dragonrise_reforge.entities.utils;

import com.atsuishio.superbwarfare.Mod;
import com.atsuishio.superbwarfare.data.DataLoader;
import com.atsuishio.superbwarfare.data.vehicle.DefaultVehicleData;
import com.atsuishio.superbwarfare.data.vehicle.subdata.EngineInfo;
import com.atsuishio.superbwarfare.data.vehicle.subdata.EngineType;
import com.redabysslucia.dragonrise_reforge.entities.utils.DragonriseVehicleBase;

import lombok.Setter;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.joml.Math;

import java.util.List;


public abstract class VariableEngineVehicle extends DragonriseVehicleBase {

    //public static final EntityDataAccessor<String> FLY_MODE = SynchedEntityData.defineId(VTOLEntity.class, EntityDataSerializers.STRING);
    private EngineInfo variableEngineCache;

    //@Setter
    private List<EngineType> engineTypeList;
    private EngineType currentEngineType;

    private int engineTypeIndex = 0;

    /** 同步到客户端的引擎模式（EngineType.ordinal()，-1 = 未初始化）——
     *  客户端需要据此判断当前模式（如直升机模式跳过机头补偿）。 */
    private static final EntityDataAccessor<Integer> ENGINE_MODE =
            SynchedEntityData.defineId(VariableEngineVehicle.class, EntityDataSerializers.INT);


    public VariableEngineVehicle(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ENGINE_MODE, -1);
    }
    
    @Override
    public void tick() {
        super.tick();
        
        
    }

    public void setEngineTypeList(List<EngineType> engineTypeList) {
        this.engineTypeList = engineTypeList;
        this.currentEngineType = engineTypeList.get(engineTypeList.size() - 1);
    }

    public void toggleChangeMode() {
        engineTypeIndex = (engineTypeIndex + 1) % engineTypeList.size();
        var newMode = engineTypeList.get(engineTypeIndex);
        updateEngineCache(newMode);
    }

    /**
     * 当前生效的引擎模式。服务端返回本地字段；客户端读取同步值
     * （服务端切换模式时通过 entityData 同步，客户端可据此判断当前模式）。
     */
    public EngineType getCurrentEngineType() {
        if (level() != null && level().isClientSide()) {
            int ord = this.entityData.get(ENGINE_MODE);
            EngineType[] values = EngineType.values();
            if (ord >= 0 && ord < values.length) {
                return values[ord];
            }
        }
        return currentEngineType;
    }

    /** 记录并同步当前模式（两端调用；服务端为准，客户端读到服务端同步值） */
    private void syncEngineMode(EngineType type) {
        if (type != null) {
            this.entityData.set(ENGINE_MODE, type.ordinal());
        }
    }

    /**
     * 起落架强制启用钩子：部分载具 JSON 未配置 HasGear（起落架逻辑由引擎驱动），
     * 但模型带有起落架动画（gear_up/gear_down），需要代码层强制开启。
     */
    protected boolean forceGearEnabled() {
        return false;
    }

    /**
     * 固定翼（Aircraft）模式无重力无升力钩子：
     * 返回 true 时，Aircraft 模式会 1) 抵消通用重力（净无重力）、2) 把气动升力置 0；
     * 直升机（悬停）模式不受影响——重力与升力按 JSON/兜底正常生效。
     */
    protected boolean noGravityAndLiftInAircraftMode() {
        return false;
    }

    /**
     * 固定翼模式机头防下垂钩子：
     * 抵消 superb aircraftEngine → updateRotation 里写死的 -0.06 虚构下坠配平
     * （无重力悬停时该配平会把机头缓缓拉向 +90°），使静止时机头保持玩家姿态。
     */
    protected boolean cancelFakeNoseDropInAircraftMode() {
        return false;
    }

    /** 该载具是否配备起落架（由 Aircraft 引擎缓存记录，供直升机模式复用） */
    private boolean gearEquipped;
    /** 起落架旋转角度（来自 Aircraft 引擎缓存，默认 85） */
    private float gearRotateAngle = 85f;

    /** Aircraft 引擎缓存解析后的统一处理（升力按需归零、记录起落架配置） */
    private EngineInfo.Aircraft applyAircraftTweaks(EngineInfo.Aircraft ac) {
        if (forceGearEnabled() && !ac.getHasGear()) {
            ac.setHasGear(true);
        }
        if (noGravityAndLiftInAircraftMode()) {
            ac.setLiftSpeed(0f);
        }
        this.gearEquipped = ac.getHasGear();
        this.gearRotateAngle = ac.getGearRotateAngle();
        return ac;
    }

    /**
     * 直升机模式下的起落架驱动。
     * superb 的 helicopterEngine 不处理起落架（空格被"悬停模式切换"占用），
     * 这里接管：空格收放起落架（空中收、着地自动放），并推进 synchedGearRot。
     * 仅在服务端执行，客户端读取同步值。
     */
    private void tickHelicopterGear() {
        if (level() == null || level().isClientSide()) return;

        if (upInputDown()) {
            setUpInputDown(false);
            if (getSynchedGearRot() == 0f && !onGround()) {
                setGearUp(true);
            } else if (getSynchedGearRot() == 1f) {
                setGearUp(false);
            }
        }
        if (onGround()) {
            setGearUp(false);
        }
        setSynchedGearRot(getGearUp()
                ? java.lang.Math.min(getSynchedGearRot() + 0.05f, 1f)
                : java.lang.Math.max(getSynchedGearRot() - 0.05f, 0f));
        setGearRot(getSynchedGearRot() * gearRotateAngle);
    }

    /**
     * 撤销 updateRotation 因虚构下坠多产生的低头增量（细节见钩子注释）。
     */
    private void cancelFakeNoseDrop() {
        // 镜像 superb updateRotation 的触发条件（!onGround、tickCount > 5、|xRot| < 90）
        if (onGround() || isWreck() || tickCount <= 5) return;
        // 仅固定翼模式补偿（模式经 entityData 同步，两端判断一致）；
        // 直升机模式没有低头配平，补偿反而会把机头抬起来。
        if (getCurrentEngineType() != EngineType.AIRCRAFT) return;
        if (Math.abs(getXRot()) >= 90f) return;

        var v = getDeltaMovement();
        double horiz = v.horizontalDistance();

        // 原版配平目标（带 -0.06 虚构下坠），常量与 superb VehicleEngineUtils.updateRotation 一致
        float targetFake = (float) -java.lang.Math.toDegrees(java.lang.Math.atan2(v.y - 0.06, horiz));
        // 无虚构时用实际速度配平的目标
        float targetReal = (float) -java.lang.Math.toDegrees(java.lang.Math.atan2(v.y, horiz));

        float diffFake = Mth.wrapDegrees(targetFake - getXRot());
        float diffReal = Mth.wrapDegrees(targetReal - getXRot());
        // updateRotation 每 tick 增量 0.01 * diffFake，这里撤掉它超出真实配平的部分
        setXRot(getXRot() - 0.01f * (diffFake - diffReal));
    }

    /**
     * 按模式构建引擎缓存（纯解析，无副作用——不触碰 POWER / 模式字段）。
     * 服务端切换（updateEngineCache）与客户端跟随同步模式时复用。
     */
    private EngineInfo buildEngineCache(EngineType type) {
        var engineInfo = computed().getEngineInfo();
        try {
            return switch (type) {
                case WHEEL -> DataLoader.GSON.fromJson(engineInfo, EngineInfo.Wheel.class);
                case TRACK -> DataLoader.GSON.fromJson(engineInfo, EngineInfo.Track.class);
                case HELICOPTER -> {
                    var heli = DataLoader.GSON.fromJson(engineInfo, EngineInfo.Helicopter.class);
                    // 载具 JSON 把 LiftSpeed 配成接近 0（固定翼的微量气动升力参数，如 0.01~0.05）时，
                    // 直升机模式会没有悬停升力（且 power 下限 0.035/lift 会除零）——
                    // 兜底给一个基础悬停升力（与其它直升机一致）。正常直升机参数（≥0.1）不受影响。
                    if (heli.getLiftSpeed() < 0.1f) {
                        heli.setLiftSpeed(1f);
                    }
                    yield heli;
                }
                case SHIP -> DataLoader.GSON.fromJson(engineInfo, EngineInfo.Ship.class);
                case AIRCRAFT -> applyAircraftTweaks(DataLoader.GSON.fromJson(engineInfo, EngineInfo.Aircraft.class));
                case WHEELCHAIR -> DataLoader.GSON.fromJson(engineInfo, EngineInfo.WheelChair.class);
                case TOM6 -> DataLoader.GSON.fromJson(engineInfo, EngineInfo.Tom6.class);
                default -> null;
            };
        } catch (Exception e) {
            Mod.LOGGER.error("Failed to parse engine info for vehicle {}, {}", this, e);
            return null;
        }
    }

    /** 切换缓存并同步模式/引擎信息（不做 POWER 缩放） */
    private void applyEngineCache(EngineType type, EngineInfo cache) {
        this.variableEngineCache = cache;
        this.currentEngineType = type;
        syncEngineMode(type);
        // 同步到 VehicleEntity.engineInfo（HUD 提示 / 引擎状态读取依赖它）
        this.setEngineInfo(cache);
    }

    public void updateEngineCache(EngineType pEngineType) {
        // POWER 在两种引擎里语义不同（直升机模式缩小 12%），切换时换算
        if (currentEngineType == EngineType.HELICOPTER) {
            entityData.set(POWER, entityData.get(POWER) / 0.12f);
        }
        if (pEngineType == EngineType.HELICOPTER) {
            entityData.set(POWER, entityData.get(POWER) * 0.12f);
        }
        var cache = buildEngineCache(pEngineType);
        if (cache != null) {
            applyEngineCache(pEngineType, cache);
        }
    }

    @Override
    public void travel() {
        var computed = computed();

        var engineType = computed.getEngineType();
        if (engineType == EngineType.EMPTY) return;
        if (engineType == EngineType.FIXED) {
            this.fixedEngine();
            return;
        }

        if (variableEngineCache == null) {
            // 初始缓存按 JSON EngineType 建立，模式标记与实际一致
            var cache = buildEngineCache(engineType);
            if (cache != null) {
                applyEngineCache(engineType, cache);
            }
            return;
        }

        // 客户端跟随服务端同步的引擎模式：模式切换只在服务端执行（toggleChangeMode），
        // 客户端若不跟随，会一直按 JSON 模式（固定翼）预测——直升机模式的升力/配平差异
        // 会被服务端位置同步反复拉回，表现为悬停时的高频抽搐。
        if (level().isClientSide()) {
            EngineType synced = getCurrentEngineType();
            if (synced != null && synced != currentEngineType) {
                var cache = buildEngineCache(synced);
                if (cache != null) {
                    applyEngineCache(synced, cache);
                }
            }
        }

        // 直升机模式：起落架改由空格控制（superb 直升机里空格被"悬停模式切换"占用），
        // 并自动开启悬停稳定（hoverMode）——玩家无需手动切换，空格专用于收放起落架。
        if (getCurrentEngineType() == EngineType.HELICOPTER) {
            if (gearEquipped) {
                tickHelicopterGear();
            }
            if (!getHoverMode()) {
                setHoverMode(true);
            }
        }

        this.variableEngineCache.work(this);

        // 固定翼模式无重力：VehicleEntity.baseTick 会在 travel() 之后统一施加 -computed.gravity，
        // 这里先补回 +gravity 抵消，使 Aircraft 模式净重力为 0；
        // 直升机模式不补偿，重力正常生效（由升力抵消）。
        if (noGravityAndLiftInAircraftMode() && getCurrentEngineType() == EngineType.AIRCRAFT) {
            setDeltaMovement(getDeltaMovement().add(0.0, computed.getGravity(), 0.0));
        }

        // 固定翼模式机头防下垂：撤销 updateRotation 的虚构下坠低头增量
        if (cancelFakeNoseDropInAircraftMode()) {
            cancelFakeNoseDrop();
        }
    }

    @Override
    public float getEngineSoundVolume() {
        //DefaultVehicleData computed = this.computed();
        EngineType engineType = currentEngineType;
        if (engineType != EngineType.EMPTY && engineType != EngineType.FIXED && engineType != null) {
            EngineInfo engineInfo = this.variableEngineCache;
            if (engineInfo == null) {
                return 0.0F;
            } else {
                float var10000;
                switch (engineType) {
                    case TRACK ->
                            var10000 = Math.max(Mth.abs((Float) this.entityData.get(POWER)), Mth.abs(1.4F * (Float) this.entityData.get(DELTA_ROT))) * engineInfo.getEngineSoundVolume();
                    case HELICOPTER ->
                            var10000 = (Float) this.entityData.get(POWER) / 0.12f * engineInfo.getEngineSoundVolume();
                    default -> var10000 = Mth.abs((Float) this.entityData.get(POWER)) * engineInfo.getEngineSoundVolume();
                }

                return var10000;
            }
        } else {
            return 0.0F;
        }
    }

}
