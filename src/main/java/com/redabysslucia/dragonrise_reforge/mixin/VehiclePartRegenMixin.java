package com.redabysslucia.dragonrise_reforge.mixin;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.entity.vehicle.utils.VehicleEffectUtils;
import com.redabysslucia.dragonrise_reforge.config.server.VehicleCombatConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 移除载具部件的被动回血。
 * <p>
 * 卓越前线 {@code VehicleEffectUtils.handlePartHealth} 在方法末尾把 5 个部件
 * （炮塔 / 左右履带（车轮）/ 主副发动机）各回复 {@code 0.25%} 最大血量。
 * 这里在方法头部记录 5 个部件血量，在方法返回前原样写回，从而"吃掉"这段回血，
 * 同时完整保留该方法里的其它逻辑（血量 ≤0 → Damaged 标志、>95% → 解除标志、
 * 以及"整车血量过低时强制打坏炮塔与发动机"）。
 * <p>
 * 不用 {@code @Redirect} 改 {@code kotlin.math.min} 的原因：避免依赖 Kotlin 内联/编译细节，
 * 且对卓越前线的公式改动更健壮。部件血量是同步数据（SynchedEntityData），
 * 服务端在这里写回后客户端同样会看到"不回复"。
 */
@Mixin(value = VehicleEffectUtils.class, remap = false)
public abstract class VehiclePartRegenMixin {

    /** 每个线程一份的快照缓存（服务端/客户端线程各自独立，方法本身两端都会执行）。 */
    private static final ThreadLocal<float[]> DRAGONRISE_PART_SNAPSHOT =
            ThreadLocal.withInitial(() -> new float[5]);

    @Inject(method = "handlePartHealth", at = @At("HEAD"))
    private static void dragonrise$capturePartHealth(VehicleEntity vehicle, CallbackInfo ci) {
        if (!VehicleCombatConfig.DISABLE_PASSIVE_REGEN) {
            return;
        }
        float[] snapshot = DRAGONRISE_PART_SNAPSHOT.get();
        snapshot[0] = vehicle.getTurretHealth();
        snapshot[1] = vehicle.getLeftWheelHealth();
        snapshot[2] = vehicle.getRightWheelHealth();
        snapshot[3] = vehicle.getMainEngineHealth();
        snapshot[4] = vehicle.getSubEngineHealth();
    }

    @Inject(method = "handlePartHealth", at = @At("RETURN"))
    private static void dragonrise$restorePartHealth(VehicleEntity vehicle, CallbackInfo ci) {
        if (!VehicleCombatConfig.DISABLE_PASSIVE_REGEN) {
            return;
        }
        float[] snapshot = DRAGONRISE_PART_SNAPSHOT.get();
        vehicle.setTurretHealth(snapshot[0]);
        vehicle.setLeftWheelHealth(snapshot[1]);
        vehicle.setRightWheelHealth(snapshot[2]);
        vehicle.setMainEngineHealth(snapshot[3]);
        vehicle.setSubEngineHealth(snapshot[4]);
    }
}
