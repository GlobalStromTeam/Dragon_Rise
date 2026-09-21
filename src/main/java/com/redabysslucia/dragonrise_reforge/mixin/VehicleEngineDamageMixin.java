package com.redabysslucia.dragonrise_reforge.mixin;

import com.atsuishio.superbwarfare.data.vehicle.subdata.EngineInfo;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.entity.vehicle.utils.VehicleEngineUtils;
import com.redabysslucia.dragonrise_reforge.config.server.VehicleCombatConfig;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 改变"部件血量清零"后的表现（履带 / 发动机）。
 * <p>
 * 卓越前线原有表现非常轻微：
 * <ul>
 *   <li>履带车：单侧履带报废只把功率乘 0.975 并给一点偏航，双侧乘 0.93；</li>
 *   <li>轮式车：同样的 0.975 / 0.93；</li>
 *   <li>发动机报废：履带 0.96、轮式 0.875、船 0.875、直升机 0.98 倍功率。</li>
 * </ul>
 * 本 mixin 改为：
 * <ul>
 *   <li><b>履带车</b>（{@code trackEngine}，天然不含轮式）：单侧履带报废 → 禁前进/后退输入，
 *       只保留转向 → 只能原地转向；报废侧履带值强制归零（该侧履带完全不动）。
 *       双侧报废 → 等同发动机失效。</li>
 *   <li><b>发动机失效</b>（{@code mainEngineDamaged}）→ 清空全部行驶输入并把功率归零，
 *       同时在引擎函数末尾把水平位移锁死 → 载具完全无法移动。
 *       覆盖 {@code trackEngine} / {@code wheelEngine} / {@code shipEngine} / {@code helicopterEngine}。</li>
 * </ul>
 * 清空输入 + 归零功率正是卓越前线自己在"能量耗尽"和"无乘客"时使用的做法
 * （{@code VehicleEngineUtils} 内 {@code energy <= energyCost} 与 {@code passenger0 == null} 分支），
 * 因此不会与它自身的逻辑冲突。
 * <p>
 * 未覆盖：固定翼 {@code aircraftEngine}（保留原版表现）。
 */
@Mixin(value = VehicleEngineUtils.class, remap = false)
public abstract class VehicleEngineDamageMixin {

    /** 发动机失效：清空所有行驶输入并归零功率（沿用卓越前线自身的做法）。 */
    private static void dragonrise$lockAllDrivingInputs(VehicleEntity vehicle) {
        vehicle.setForwardInputDown(false);
        vehicle.setBackInputDown(false);
        vehicle.setLeftInputDown(false);
        vehicle.setRightInputDown(false);
        vehicle.setPower(0f);
    }

    /** 只在水平方向锁死位移，保留竖直分量（重力 / 浮力继续生效）。 */
    private static void dragonrise$lockHorizontalMovement(VehicleEntity vehicle) {
        Vec3 delta = vehicle.getDeltaMovement();
        vehicle.setDeltaMovement(0.0, delta.y, 0.0);
    }

    /** 履带车：单侧报废只允许原地转向；双侧报废等同发动机失效。 */
    private static void dragonrise$applyTrackDamage(VehicleEntity vehicle) {
        boolean left = vehicle.getLeftWheelDamaged();
        boolean right = vehicle.getRightWheelDamaged();
        if (vehicle.getMainEngineDamaged() || (left && right)) {
            dragonrise$lockAllDrivingInputs(vehicle);
            return;
        }
        if (VehicleCombatConfig.DEAD_TRACK_PIVOT_ONLY && (left || right)) {
            // 单侧履带报废：禁前进/后退并归零功率（该侧履带无法带动车体直行），只保留转向 → 只能原地转向
            vehicle.setForwardInputDown(false);
            vehicle.setBackInputDown(false);
            vehicle.setPower(0f);
        }
    }

    // ==================== 履带 ====================

    @Inject(method = "trackEngine", at = @At("HEAD"))
    private static void dragonrise$trackEngineHead(VehicleEntity vehicle, EngineInfo.Track info, CallbackInfo ci) {
        dragonrise$applyTrackDamage(vehicle);
    }

    @Inject(method = "trackEngine", at = @At("TAIL"))
    private static void dragonrise$trackEngineTail(VehicleEntity vehicle, EngineInfo.Track info, CallbackInfo ci) {
        boolean left = vehicle.getLeftWheelDamaged();
        boolean right = vehicle.getRightWheelDamaged();
        if (left) {
            vehicle.setLeftTrack(0f);
        }
        if (right) {
            vehicle.setRightTrack(0f);
        }
        if (vehicle.getMainEngineDamaged() || (left && right)) {
            dragonrise$lockHorizontalMovement(vehicle);
        }
    }

    // ==================== 轮式 ====================

    @Inject(method = "wheelEngine", at = @At("HEAD"))
    private static void dragonrise$wheelEngineHead(VehicleEntity vehicle, EngineInfo.Wheel info, CallbackInfo ci) {
        if (vehicle.getMainEngineDamaged()) {
            dragonrise$lockAllDrivingInputs(vehicle);
        }
    }

    @Inject(method = "wheelEngine", at = @At("TAIL"))
    private static void dragonrise$wheelEngineTail(VehicleEntity vehicle, EngineInfo.Wheel info, CallbackInfo ci) {
        if (vehicle.getMainEngineDamaged()) {
            dragonrise$lockHorizontalMovement(vehicle);
        }
    }

    // ==================== 船 ====================

    @Inject(method = "shipEngine", at = @At("HEAD"))
    private static void dragonrise$shipEngineHead(VehicleEntity vehicle, EngineInfo.Ship info, CallbackInfo ci) {
        if (vehicle.getMainEngineDamaged()) {
            dragonrise$lockAllDrivingInputs(vehicle);
        }
    }

    @Inject(method = "shipEngine", at = @At("TAIL"))
    private static void dragonrise$shipEngineTail(VehicleEntity vehicle, EngineInfo.Ship info, CallbackInfo ci) {
        if (vehicle.getMainEngineDamaged()) {
            dragonrise$lockHorizontalMovement(vehicle);
        }
    }

    // ==================== 直升机 ====================

    @Inject(method = "helicopterEngine", at = @At("HEAD"))
    private static void dragonrise$helicopterEngineHead(VehicleEntity vehicle, EngineInfo.Helicopter info, CallbackInfo ci) {
        if (vehicle.getMainEngineDamaged()) {
            dragonrise$lockAllDrivingInputs(vehicle);
        }
    }

    @Inject(method = "helicopterEngine", at = @At("TAIL"))
    private static void dragonrise$helicopterEngineTail(VehicleEntity vehicle, EngineInfo.Helicopter info, CallbackInfo ci) {
        if (vehicle.getMainEngineDamaged()) {
            dragonrise$lockHorizontalMovement(vehicle);
        }
    }
}
