package com.redabysslucia.dragonrise_reforge.entities.utils;

import net.minecraft.world.entity.Entity;

/**
 * 导弹发射速度门槛（全项目唯一的调参点）。
 *
 * <p>约定：载具「水平速度 ≤ {@link #MAX_SPEED_KMH} km/h」才允许发射导弹。
 * 判据用的是速度大小而不是方向，所以正车、倒车共用一个值（±14 km/h）。</p>
 *
 * <p>换算：{@link Entity#getDeltaMovement()} 的单位是「格/tick」，
 * 1 格/tick = 20 格/秒 = 72 km/h，因此 km/h = 格/tick × 72。
 * 14 km/h ≈ 0.1944 格/tick（旧写法用 0.01 格/tick 当门槛，只有 0.72 km/h，等于"必须完全停下"）。</p>
 *
 * <p>使用位置：{@code M3A3Entity} / {@code ZBL08Entity} / {@code Zbd05Entity} 的
 * {@code canShoot} 与 {@code vehicleShoot}，以及 M3A3 发射架的自动展开条件。</p>
 */
public final class MissileFireGate {

    /** 允许发射导弹的最大水平速度（km/h）。调这一个值即可统一改动所有载具的门槛。 */
    public static final double MAX_SPEED_KMH = 14.0;

    /** 格/tick → km/h 的换算系数（20 tick/s × 3.6）。 */
    public static final double KMH_PER_BLOCK_PER_TICK = 72.0;

    private MissileFireGate() {
    }

    /**
     * 当前水平速度（km/h）。只取水平分量，忽略上下坡/落体的垂直速度。
     */
    public static double horizontalSpeedKmh(Entity entity) {
        return entity.getDeltaMovement().horizontalDistance() * KMH_PER_BLOCK_PER_TICK;
    }

    /**
     * 是否满足发射导弹的速度条件。
     */
    public static boolean canFireMissile(Entity entity) {
        return horizontalSpeedKmh(entity) <= MAX_SPEED_KMH;
    }
}
