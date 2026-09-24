package com.redabysslucia.dragonrise_reforge.mixin;

import com.atsuishio.superbwarfare.entity.vehicle.utils.VehicleWeaponUtils;
import com.redabysslucia.dragonrise_reforge.config.server.VehicleCombatConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * 炮塔损坏（{@code TurretHealth = 0} → {@code TurretDamaged}）后的转速惩罚。
 * <p>
 * 卓越前线 {@code VehicleWeaponUtils.turretAutoAimFromVector} 中：
 * {@code if (vehicle.entityData.get(VehicleEntity.TURRET_DAMAGED)) { ySpeed *= 0.2f; xSpeed *= 0.2f; }}
 * （该方法是玩家手动瞄准与自动瞄准的唯一路径：{@code VehicleEntity.baseTick → adjustTurretAngle → turretAutoAimFromVector}）。
 * <p>
 * 这里把该 0.2 常量替换为 {@link VehicleCombatConfig#TURRET_DAMAGED_SPEED_FACTOR}（默认 0.05），
 * 即炮塔报废后转向速度降为 5%。该方法内只有这一处 0.2 常量。
 */
@Mixin(value = VehicleWeaponUtils.class, remap = false)
public abstract class VehicleTurretSpeedMixin {

    @ModifyConstant(method = "turretAutoAimFromVector", constant = @Constant(floatValue = 0.2F))
    private static float dragonrise$turretDamagedSpeedFactor(float original) {
        return VehicleCombatConfig.TURRET_DAMAGED_SPEED_FACTOR;
    }
}
