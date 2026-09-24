package com.redabysslucia.dragonrise_reforge.mixin;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.redabysslucia.dragonrise_reforge.config.server.VehicleCombatConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 移除载具整车的"呼吸回血"。
 * <p>
 * 卓越前线在 {@code VehicleEntity.baseTick} 中：
 * {@code if (health <= selfHurtPercent*max) 自损 else if (repairCoolDown == 0 && health > 0) heal(repairAmount())}。
 * 这里把 {@code repairAmount()} 的返回值改写成 0，从而关闭整车被动回血。
 * <p>
 * 为什么改 {@code repairAmount()} 而不是重定向 {@code heal} 调用：
 * <ul>
 *   <li>{@code repairAmount()} 是卓越前线的自有方法（生产环境不改名），注入目标可稳定匹配；</li>
 *   <li>{@code heal(float)} 是原版 {@code LivingEntity} 的成员，在被改写的调用点里名字会变成 SRG 名，
 *       无法用可读字符串稳定匹配。</li>
 * </ul>
 * 低血量自损（selfHurt）保留；维修工具走 {@link RepairToolItem} 自己的分支，不受影响。
 */
@Mixin(VehicleEntity.class)
public abstract class VehicleRegenMixin {

    @Redirect(
            method = "baseTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/atsuishio/superbwarfare/entity/vehicle/base/VehicleEntity;repairAmount()F"
            )
    )
    private float dragonrise$disableBodyBreathingRegen(VehicleEntity vehicle) {
        if (!VehicleCombatConfig.DISABLE_PASSIVE_REGEN) {
            return vehicle.repairAmount();
        }
        return 0f;
    }
}
