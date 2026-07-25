package com.redabysslucia.dragonrise_reforge.firecontrol;

import com.atsuishio.superbwarfare.data.gun.GunData;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.entity.vehicle.utils.VehicleVecUtils;
import com.atsuishio.superbwarfare.tools.TrajectoryCalculator;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

/**
 * 曲射弹道解算。
 * <p>
 * 注意 SBW {@link TrajectoryCalculator#calculateLaunchVector} 的 boolean 形参语义与名字相反：
 * {@code true} → {@code getFlatTrajectory} → 列表中飞行时间更长的解 → <b>高仰角</b>；
 * {@code false} → {@code getHighTrajectory} → 飞行时间更短的解 → <b>低仰角</b>。
 * 因此 {@link TrajectoryMode#LOW} 必须传 {@code false}。
 */
public final class IndirectFireBallistics {

    public static final int MAX_RADIUS = 99;
    public static final int RANGE_TABLE_ROWS = 8;
    /** 与 ZBD04A / BMP3 JSON 中 100MM_Cannon 武器索引一致 */
    public static final int MAIN_CANNON_WEAPON_INDEX = 1;
    private static final double ANGLE_EPSILON = 0.05;

    private IndirectFireBallistics() {
    }

    public static FireControlComputation solve(
            VehicleEntity vehicle,
            int seatIndex,
            BlockPos target,
            TrajectoryMode mode
    ) {
        return solve(vehicle, seatIndex, target.getCenter(), mode);
    }

    public static FireControlComputation solve(
            VehicleEntity vehicle,
            int seatIndex,
            Vec3 target,
            TrajectoryMode mode
    ) {
        // 始终用主炮弹道参数，不依赖当前选中的自动炮/机枪
        GunData gunData = resolveMainCannonGunData(vehicle, seatIndex);
        if (gunData == null) {
            return FireControlComputation.failure(FireControlStatus.INVALID_WEAPON);
        }

        double velocity = vehicle.getProjectileVelocity(gunData);
        double gravity = vehicle.getProjectileGravity(gunData);
        if (velocity <= 0 || gravity <= 0) {
            return FireControlComputation.failure(FireControlStatus.INVALID_WEAPON);
        }

        // 座位射击点（当前武器位姿）；主炮与自动炮同炮塔时足够
        Vec3 muzzle = vehicle.getShootPos(seatIndex, 1.0f);
        if (muzzle == null) {
            return FireControlComputation.failure(FireControlStatus.INVALID_WEAPON);
        }

        double distance = muzzle.distanceTo(target);
        // 与 FCP 相同的落点高度补偿（略低于方块中心，模拟命中地表）
        Vec3 adjustedTarget = target.add(0, -1.0 - 0.0015 * distance, 0);

        // SBW boolean：true=误名 flat=高弧，false=误名 high=低弧 → LOW 必须 false
        boolean useSbwHighArcApi = mode == TrajectoryMode.HIGH;
        Vec3 direction = TrajectoryCalculator.calculateLaunchVector(
                muzzle,
                adjustedTarget,
                velocity,
                gravity,
                useSbwHighArcApi
        );
        if (direction == null || direction.lengthSqr() < 1.0E-8) {
            return FireControlComputation.failure(FireControlStatus.OUT_OF_RANGE);
        }

        direction = direction.normalize();
        // SBW getXRotFromVector：向上为正；TurretPitchRange 如 [-6, 60]
        double pitch = VehicleVecUtils.getXRotFromVector(direction);
        if (pitch + ANGLE_EPSILON < vehicle.getTurretMinPitch()
                || pitch - ANGLE_EPSILON > vehicle.getTurretMaxPitch()) {
            return FireControlComputation.failure(FireControlStatus.PITCH_LIMIT, pitch);
        }

        double forwardYaw = VehicleVecUtils.getYRotFromVector(vehicle.getForward());
        double desiredYaw = VehicleVecUtils.getYRotFromVector(direction);
        double relativeYaw = Mth.wrapDegrees(desiredYaw - forwardYaw);
        if (relativeYaw + ANGLE_EPSILON < vehicle.getTurretMinYaw()
                || relativeYaw - ANGLE_EPSILON > vehicle.getTurretMaxYaw()) {
            return FireControlComputation.failure(FireControlStatus.YAW_LIMIT, pitch);
        }

        double dx = target.x - muzzle.x;
        double dz = target.z - muzzle.z;
        double range = Math.sqrt(dx * dx + dz * dz);
        double horizontalSpeed = velocity * Math.sqrt(direction.x * direction.x + direction.z * direction.z);
        double flightTime = horizontalSpeed > 1.0E-8 ? range / horizontalSpeed : 0;
        double yaw = -VehicleVecUtils.getYRotFromVector(direction);

        return FireControlComputation.success(new FireControlSolution(
                muzzle, target, adjustedTarget, direction, range, pitch, yaw, flightTime
        ));
    }

    /**
     * 取主炮 GunData：优先 seat + 武器索引 1，再按名称 100MM_Cannon 回退。
     */
    private static GunData resolveMainCannonGunData(VehicleEntity vehicle, int seatIndex) {
        if (seatIndex >= 0) {
            GunData byIndex = vehicle.getGunData(seatIndex, MAIN_CANNON_WEAPON_INDEX);
            if (byIndex != null) {
                return byIndex;
            }
        }
        GunData byName = vehicle.getGunData("100MM_Cannon");
        if (byName != null) {
            return byName;
        }
        // 最后回退当前座位选中武器（兼容其它火控载具）
        if (seatIndex >= 0) {
            return vehicle.getGunData(seatIndex);
        }
        return null;
    }

    public static Vec3 sampleTarget(BlockPos center, int radius, RandomSource random) {
        Vec3 target = center.getCenter();
        if (radius <= 0) {
            return target;
        }

        double sampledRadius = radius * Math.sqrt(random.nextDouble());
        double angle = random.nextDouble() * Math.PI * 2.0;
        return target.add(sampledRadius * Math.cos(angle), 0, sampledRadius * Math.sin(angle));
    }

    public static double rangeAtPitch(
            double velocity,
            double gravity,
            double muzzleY,
            double targetY,
            double pitchDegrees
    ) {
        if (velocity <= 0 || gravity <= 0 || pitchDegrees <= 0) {
            return 0;
        }

        double pitch = Math.toRadians(pitchDegrees);
        double verticalSpeed = velocity * Math.sin(pitch);
        double discriminant = verticalSpeed * verticalSpeed - 2.0 * gravity * (targetY - muzzleY);
        if (discriminant < 0) {
            return 0;
        }

        double flightTime = (verticalSpeed + Math.sqrt(discriminant)) / gravity;
        return velocity * Math.cos(pitch) * flightTime;
    }
}
