package com.redabysslucia.dragonrise_reforge.mixin;

import com.atsuishio.superbwarfare.client.overlay.RenderContext;
import com.atsuishio.superbwarfare.client.overlay.VehicleMainWeaponHudOverlay;
import com.redabysslucia.dragonrise_reforge.util.RemotePilotHelper;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.TickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 遥控驾驶员也走 SBW 原生的载具 HUD 分发器。
 * <p>
 * {@code VehicleMainWeaponHudOverlay.render} 是官方的 HUD 分发器：按载具的 {@code HudType}
 * 分发到 {@code LandVehicleHud} / {@code HelicopterHud} / {@code AircraftHud} … ，
 * 它的门槛只有一句 {@code val vehicle = player.vehicle; if (vehicle !is VehicleEntity) return}。
 * 遥控者不骑乘 → 整条原生 HUD 链都不跑，我此前只能自己写个覆盖层直接调 {@code AircraftHud.render}
 * （那就是重复造轮子）。
 * <p>
 * 这里把该门槛的 {@code player.vehicle} 取值改判为"该玩家正在遥控的飞机"，
 * 于是官方分发器 + 官方 HUD 内容（含武器信息、弹药等部件）全部照常工作，
 * 自有覆盖层 {@code RemoteAircraftHudOverlay} 因此可以删掉。
 * <p>
 * 两个注入点都带 {@code require = 0}：HUD 是纯外观，万一以后 SBW 改实现，
 * 最坏只是没有 HUD，不会让游戏起不来。
 * <b>remap</b>：注入目标是原版 {@code LocalPlayer.getVehicle()}，走 refmap（默认 remap = true）。
 */
@Mixin(VehicleMainWeaponHudOverlay.class)
public class VehicleMainWeaponHudOverlayRemoteMixin {

    private static Entity dragonrise$vehicleOrRemoteAircraft(Entity self) {
        Entity ridden = self.getVehicle();
        if (ridden != null) {
            return ridden;
        }
        if (self instanceof net.minecraft.world.entity.player.Player player) {
            var aircraft = RemotePilotHelper.remoteAircraft(player);
            if (aircraft != null) {
                return aircraft;
            }
        }
        return null;
    }

    /** 分发器 render(RenderContext)：player.vehicle */
    @Redirect(method = "render(Lcom/atsuishio/superbwarfare/client/overlay/RenderContext;)V", require = 0,
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;getVehicle()Lnet/minecraft/world/entity/Entity;"))
    private Entity dragonrise$renderDispatcher(LocalPlayer self) {
        return dragonrise$vehicleOrRemoteAircraft(self);
    }

    /** 配套的客户端 tick（维护武器列表动画状态）：player.vehicle */
    @Redirect(method = "onVehicleMainWeaponHudOverlayClientTick(Lnet/minecraftforge/event/TickEvent$ClientTickEvent;)V",
            require = 0, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/player/LocalPlayer;getVehicle()Lnet/minecraft/world/entity/Entity;"))
    private Entity dragonrise$tickDispatcher(LocalPlayer self) {
        return dragonrise$vehicleOrRemoteAircraft(self);
    }
}
