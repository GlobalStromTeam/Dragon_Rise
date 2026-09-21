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
 *   <li><b>履带车</b>（{@code trackEngine}，天然不含轮式）：
 *       <b>单侧履带报废</b>时——① 冻结该侧履带与负重轮（该侧完全不动）；
 *       ② 试图前进/后退时自动叠加"朝报废侧转向"的输入（左坏=叠加 A，右坏=叠加 D），
 *       于是按 W 等同于 W+A、按 S 等同于 S+A；
 *       ③ 功率上限压到 {@link VehicleCombatConfig#DEAD_TRACK_POWER_CAP}（默认 0.25，即减少 75%）。
 *       <b>双侧</b>履带报废则等同发动机失效。</li>
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

    /** 发动机失效 / 双侧履带报废：清空所有行驶输入并归零功率（沿用卓越前线自身的做法）。 */
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

    /** 履带车：仅"发动机失效 / 双侧履带报废"限制行驶；单侧报废走"向坏侧转向 + 功率限幅"。 */
    private static void dragonrise$applyTrackDamage(VehicleEntity vehicle) {
        boolean left = vehicle.getLeftWheelDamaged();
        boolean right = vehicle.getRightWheelDamaged();
        if (vehicle.getMainEngineDamaged() || (left && right)) {
            dragonrise$lockAllDrivingInputs(vehicle);
            return;
        }
        if (left ^ right) {
            dragonrise$applyDeadTrackHandling(vehicle, left);
        }
    }

    /**
     * 单侧履带报废的处理：
     * <ol>
     *   <li>试图前进/后退时，自动叠加"朝报废侧转向"的输入 —— 左侧履带报废时按 W 等同于 W+A、
     *       按 S 等同于 S+A；右侧报废同理（右边是叠加 D）。</li>
     *   <li>功率上限压到 {@link VehicleCombatConfig#DEAD_TRACK_POWER_CAP}（默认 0.25，即减少 75%）。
     *       在引擎计算前限幅，所以本 tick 的位移也是按限幅后的功率算的。</li>
     * </ol>
     *
     * @param leftDead 报废的是否为左侧履带
     */
    private static void dragonrise$applyDeadTrackHandling(VehicleEntity vehicle, boolean leftDead) {
        if (VehicleCombatConfig.DEAD_TRACK_STEER_TO_DEAD_SIDE
                && (vehicle.forwardInputDown() || vehicle.backInputDown())) {
            if (leftDead) {
                vehicle.setLeftInputDown(true);
            } else {
                vehicle.setRightInputDown(true);
            }
        }

        float cap = VehicleCombatConfig.DEAD_TRACK_POWER_CAP;
        if (vehicle.getPower() > cap) {
            vehicle.setPower(cap);
        } else if (vehicle.getPower() < -cap) {
            vehicle.setPower(-cap);
        }
    }

    /** 冻结报废侧的履带与负重轮（该侧完全不动），另一侧照常。 */
    private static void dragonrise$freezeDeadTrackSide(VehicleEntity vehicle) {
        if (!VehicleCombatConfig.FREEZE_DEAD_TRACK_SIDE) {
            return;
        }
        if (vehicle.getLeftWheelDamaged()) {
            vehicle.setLeftTrack(0f);
            vehicle.setLeftWheelRot(0f);
        }
        if (vehicle.getRightWheelDamaged()) {
            vehicle.setRightTrack(0f);
            vehicle.setRightWheelRot(0f);
        }
    }

    // ==================== 履带 ====================

    @Inject(method = "trackEngine", at = @At("HEAD"))
    private static void dragonrise$trackEngineHead(VehicleEntity vehicle, EngineInfo.Track info, CallbackInfo ci) {
        dragonrise$applyTrackDamage(vehicle);
    }

    @Inject(method = "trackEngine", at = @At("TAIL"))
    private static void dragonrise$trackEngineTail(VehicleEntity vehicle, EngineInfo.Track info, CallbackInfo ci) {
        dragonrise$freezeDeadTrackSide(vehicle);

        boolean left = vehicle.getLeftWheelDamaged();
        boolean right = vehicle.getRightWheelDamaged();
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
