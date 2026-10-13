package com.redabysslucia.dragonrise_reforge.mixin;

import com.atsuishio.superbwarfare.client.overlay.VehicleMainWeaponHudOverlay;
import com.atsuishio.superbwarfare.client.overlay.weapon.AircraftHud;
import com.atsuishio.superbwarfare.data.gun.GunData;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.redabysslucia.dragonrise_reforge.util.RemotePilotHelper;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 遥控者不画"准星下方那行白色武器名"。
 * <p>
 * 放开官方 HUD 分发器门槛（{@link VehicleMainWeaponHudOverlayRemoteMixin}）后，
 * 官方的 {@code AircraftHud.render} 会为遥控者完整渲染，其中会调
 * {@code VehicleMainWeaponHudOverlay.renderWeaponInfoThirdAir} 在准星下方居中画武器名。
 * 遥控者的武器信息已经在右下角的武器列表里，这行既重复又挡视线，因此跳过。
 * 真乘客（骑乘中）不受影响，仍按官方行为绘制。
 * <p>
 * <b>为什么单独一个 mixin 且 remap = false</b>：
 * 本注入的 @At 目标是<b>模组自有方法</b> {@code renderWeaponInfoThirdAir}（生产环境保持原名，
 * 不需要也无法重映射；若用默认 remap = true，注解处理器会因为找不到映射而直接报错）。
 * 而 {@code AircraftHudRemotePilotMixin} 里另有一处注入的目标是<b>原版</b>
 * {@code Entity.getFirstPassenger()}（必须重映射），两者 remap 取向相反，故拆开。
 * 同理，这里<b>不能</b>直接调用原版成员（如 {@code player.getVehicle()}）——
 * remap = false 下那些调用不会被重映射，打包后会 NoSuchMethodError；
 * 所以判定改走本模组自己的 {@link RemotePilotHelper}。
 */
@Mixin(value = AircraftHud.class, remap = false)
public class AircraftHudWeaponNameMixin {

    @Redirect(method = "render", require = 0, at = @At(value = "INVOKE",
            target = "Lcom/atsuishio/superbwarfare/client/overlay/VehicleMainWeaponHudOverlay;renderWeaponInfoThirdAir(Lnet/minecraft/client/gui/GuiGraphics;Lcom/atsuishio/superbwarfare/entity/vehicle/base/VehicleEntity;Lnet/minecraft/world/entity/player/Player;Lcom/atsuishio/superbwarfare/data/gun/GunData;Lnet/minecraft/client/gui/Font;)V"))
    private void dragonrise$skipWeaponNameForRemotePilot(GuiGraphics guiGraphics, VehicleEntity vehicle,
                                                         Player player, GunData gunData, Font font) {
        if (RemotePilotHelper.isRemoteControlledVehicle(vehicle, player)) {
            return;     // 遥控者：不画这行
        }
        VehicleMainWeaponHudOverlay.renderWeaponInfoThirdAir(guiGraphics, vehicle, player, gunData, font);
    }
}
