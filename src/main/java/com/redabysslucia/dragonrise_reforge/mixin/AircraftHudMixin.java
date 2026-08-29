package com.redabysslucia.dragonrise_reforge.mixin;

import com.atsuishio.superbwarfare.client.overlay.VehicleMainWeaponHudOverlay;
import com.atsuishio.superbwarfare.client.overlay.weapon.AircraftHud;
import com.atsuishio.superbwarfare.data.gun.GunData;
import com.atsuishio.superbwarfare.data.gun.GunProp;
import com.atsuishio.superbwarfare.data.gun.ProjectileInfo;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.event.ClientEventHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.gui.overlay.ForgeGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 本包载具使用 "@AirBomb" 武器（如 jas39e 的 GBU-12）并处于缩放/吊舱视角时，
 * 取消官方 AircraftHud 的整屏投弹镜（BOMB_SCOPE 边框 + BOMB_SCOPE_PITCH 准星）渲染。
 * 吊舱视角下的画面由本包自绘的提前落点环与炸弹锁定框补充（见 ClientEvent）。
 * 不影响第三视角/第一人称未缩放状态下的官方落点环与 HUD。
 * <p>
 * 同时补充双座飞机后座（非一号位乘客）的武器名称 HUD：
 * 官方 AircraftHud.render 仅对第一乘客渲染，后座直接 return 无任何 HUD；
 * 本 Mixin 在方法返回时（后座走的就是提前 return 路径）于屏幕下方渲染当前武器名称，
 * 打开吊舱视角（UseNacelleCamera + 缩放）时隐藏。
 */
@Mixin(value = AircraftHud.class, remap = false)
public class AircraftHudMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void dragonrise$skipBombScope(
            VehicleEntity vehicle, Player player, ForgeGui gui,
            GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight,
            CallbackInfo ci) {
        if (!ClientEventHandler.zoomVehicle) return;

        GunData data = vehicle.getGunData(player);
        if (data == null) return;
        if (!"@AirBomb".equals(data.get(GunProp.CROSSHAIR))) return;

        Object projectile = data.get(GunProp.PROJECTILE);
        if (projectile instanceof ProjectileInfo info && info.getItemId().startsWith("dragonrise_reforge:")) {
            ci.cancel();
        }
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void dragonrise$rearSeatWeaponName(
            VehicleEntity vehicle, Player player, ForgeGui gui,
            GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight,
            CallbackInfo ci) {
        // 仅后座（非一号位）：一号位由官方 HUD 渲染，此处跳过
        if (player == vehicle.getFirstPassenger()) return;
        // 仅本包双座飞机（不影响 AC-130 等官方多座载具的炮手 HUD）
        if (!"dragonrise_reforge".equals(net.minecraft.world.entity.EntityType.getKey(vehicle.getType()).getNamespace())) return;
        // 吊舱视角（UseNacelleCamera + 缩放）时不显示武器名称
        if (ClientEventHandler.isNacelleCam(player)) return;

        GunData data = vehicle.getGunData(player);
        if (data == null) return;

        // 与一号位同款样式：屏幕下方居中，缩放 0.75
        var pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(screenWidth / 2.0, screenHeight / 2.0 + 50.0, 0.0);
        pose.scale(0.75f, 0.75f, 1.0f);
        VehicleMainWeaponHudOverlay.renderWeaponInfoThirdAir(
                guiGraphics, vehicle, player, data, Minecraft.getInstance().font);
        pose.popPose();
    }
}
