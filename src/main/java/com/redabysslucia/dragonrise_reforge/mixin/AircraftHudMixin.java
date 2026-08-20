package com.redabysslucia.dragonrise_reforge.mixin;

import com.atsuishio.superbwarfare.client.overlay.weapon.AircraftHud;
import com.atsuishio.superbwarfare.data.gun.GunData;
import com.atsuishio.superbwarfare.data.gun.GunProp;
import com.atsuishio.superbwarfare.data.gun.ProjectileInfo;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.event.ClientEventHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 本包载具使用 "@AirBomb" 武器（如 jas39e 的 GBU-12）并处于缩放/吊舱视角时，
 * 取消官方 AircraftHud 的整屏投弹镜（BOMB_SCOPE 边框 + BOMB_SCOPE_PITCH 准星）渲染。
 * 吊舱视角下的画面由本包自绘的提前落点环与炸弹锁定框补充（见 ClientEvent）。
 * 不影响第三视角/第一人称未缩放状态下的官方落点环与 HUD。
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
}
