package com.redabysslucia.dragonrise_reforge.mixin;

import com.atsuishio.superbwarfare.entity.vehicle.DroneEntity;
import com.redabysslucia.dragonrise_reforge.entities.ShuangweixieEntity;
import com.redabysslucia.dragonrise_reforge.util.RemotePilotHelper;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 让双尾蝎坠毁后留下残骸。
 * <p>
 * SBW 的载具销毁链是：{@code GeoVehicleEntity.destroy()} → 载体自己变成残骸
 * （{@code VehicleEntity.kt:2507 isWreck = true}），而 {@code VehicleDestroyUtils.destroy}
 * 只在数据 {@code NoWreck} 为真时才 discard。
 * <p>
 * 但 {@code DroneEntity.destroy()} 是**四轴无人机**的语义：先调父类 destroy()，最后再补一句
 * {@code discard()}（"无人机直接消失"）。我们的固定翼继承它之后，那句 discard 会把刚生成的残骸一起删掉
 * —— 表现就是"坠毁以后没有残骸"。
 * <p>
 * 处理：只有当实体<b>确实已经变成残骸</b>（isWreck）时才跳过这句 discard，
 * 其余情况（例如没有任何操作者时的静默移除）保持官方行为，不会漏下活实体。
 * <p>
 * <b>remap</b>：方法选择器 {@code destroy()V} 是 SBW 自有方法（类级 remap = false），
 * @At 目标 {@code Entity.discard()} 是原版方法（单独 remap = true）；
 * handler 内不直接调原版成员（改走 {@link RemotePilotHelper}），否则打包后会 NoSuchMethodError。
 */
@Mixin(value = DroneEntity.class, remap = false)
public class DroneDestroyKeepWreckMixin {

    /**
     * 注意持有者：字节码常量池里这句是 {@code DroneEntity.discard:()V}
     * （javap 只显示 {@code Method discard:()V} 不显示类名，说明持有者就是当前类），
     * 写成 {@code Entity.discard()} 会静默匹配不上（require = 0 下不报错也不生效）。
     * 方法名 discard 是原版方法，故 @At 仍需 remap = true 让它进 refmap。
     */
    @Redirect(method = "destroy()V", require = 0,
            at = @At(value = "INVOKE", remap = true,
                    target = "Lcom/atsuishio/superbwarfare/entity/vehicle/DroneEntity;discard()V"))
    private void dragonrise$keepWreck(DroneEntity self) {
        if (self instanceof ShuangweixieEntity aircraft && aircraft.isWreck()) {
            com.redabysslucia.dragonrise_reforge.Dragonrise_reforge.LOGGER.info(
                    "[DR-WRECK] 保留残骸 {}", aircraft.getStringUUID());
            return;     // 已经是残骸：留下它
        }
        RemotePilotHelper.discard(self);
    }
}
