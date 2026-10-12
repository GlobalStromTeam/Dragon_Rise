package com.redabysslucia.dragonrise_reforge.mixin;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.init.ModItems;
import com.atsuishio.superbwarfare.tools.InventoryTool;
import com.redabysslucia.dragonrise_reforge.entities.ShuangweixieEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 让遥控操作者背包里的<b>创造弹药盒</b>按 SBW 原生语义生效（无限弹药）。
 * <p>
 * SBW 本来就支持这件事，判定在 {@code InventoryTool.hasCreativeAmmoBoxForVehicle}：
 * <pre>
 * val passengers = vehicle.getPassengers()
 * val flag = passengers.anyMatch { hasItem(it, CREATIVE_AMMO_BOX) }
 *            &amp;&amp; vehicle.data().compute().usePassengerCreativeAmmoBox   // 载具数据开关
 * return flag || hasItem(vehicle, CREATIVE_AMMO_BOX)
 * </pre>
 * 再由 {@code GunData.hasInfiniteBackupAmmo(shooter)} 转成"无限备弹"，
 * 于是载具武器不消耗弹药。
 * <p>
 * 遥控操作者不是真乘员（不在 {@code getPassengers()} 里），所以原判定看不到他的盒子。
 * 这里只补一句：本模组飞机若其<b>遥控操作者</b>持有创造弹药盒，同样返回 true。
 * 载具数据里已打开 {@code UsePassengerCreativeAmmoBox}，两条路径语义一致。
 * <p>
 * <b>remap = false</b>：目标方法是 SBW 自有方法，生产环境保持原名。
 */
@Mixin(value = InventoryTool.class, remap = false)
public class InventoryToolRemotePilotBoxMixin {

    @Inject(method = "hasCreativeAmmoBoxForVehicle", at = @At("HEAD"), cancellable = true, require = 0)
    private static void dragonrise$remotePilotCreativeBox(VehicleEntity vehicle,
                                                          CallbackInfoReturnable<Boolean> cir) {
        if (!(vehicle instanceof ShuangweixieEntity aircraft)) {
            return;
        }
        Player controller = aircraft.getController();
        if (controller != null && InventoryTool.hasItem(controller, ModItems.CREATIVE_AMMO_BOX.get())) {
            cir.setReturnValue(true);
        }
    }
}
