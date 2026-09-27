package com.redabysslucia.dragonrise_reforge.mixin;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.redabysslucia.dragonrise_reforge.entities.utils.VehicleDirectionArmor;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * 让载具 JSON 里官方格式的方向抗性真正生效：
 * <pre>$entity.getSourceAngle(source, X) * damage</pre>
 * <p>
 * 卓越前线自身的脚本型修正会在 {@code VehicleData.compute() -> getDefault().copy()} 的
 * Gson 往返中丢掉脚本闭包（{@code @Transient modifyFunction}），因此永远返回原伤害；
 * 这里在 {@link VehicleEntity#hurt} 的入口用同一个公式（正面 {@code 1-m} / 侧面 {@code 1} /
 * 背面 {@code 1+m}，下限 0.5）把伤害调整一次，位置与原版脚本一致（先于部件血量结算）。
 * <p>
 * 注意：{@code hurt} 是原版 {@code Entity/LivingEntity} 方法的覆写，方法名会被 SRG 重映射，
 * 所以这里**不能**加 {@code remap = false}（与 {@code VehicleRegenMixin} 注入 baseTick 同理）。
 */
@Mixin(VehicleEntity.class)
public abstract class VehicleDirectionArmorMixin {

    @ModifyVariable(method = "hurt", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float dragonrise$applyDirectionArmor(float amount, DamageSource source) {
        return VehicleDirectionArmor.apply((VehicleEntity) (Object) this, source, amount);
    }
}
