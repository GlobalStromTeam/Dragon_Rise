package com.redabysslucia.dragonrise_reforge.mixin;

import com.atsuishio.superbwarfare.client.overlay.VehicleHudOverlay;
import com.redabysslucia.dragonrise_reforge.util.RemotePilotHelper;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 让遥控驾驶员也能看到官方的载具 HUD（含武器列表 / 弹药 / 状态条）。
 * <p>
 * 官方 VehicleHudOverlay 有两道"必须在载具上"的门槛：
 * <pre>
 * private fun shouldRenderHud(player: Player?) =
 *     !player.isSpectator &amp;&amp; player.vehicle is VehicleEntity          // (1)
 * override fun RenderContext.render() { val entity = player.vehicle; if (entity !is VehicleEntity) return }  // (2)
 * </pre>
 * 遥控者不骑乘，两处都拿不到载具 -> 整套 HUD（武器列表就在其中）不渲染。
 * 这里把两处的 player.vehicle 取值改判为"该玩家正在遥控的飞机"，
 * HUD 的布局/贴图/逻辑全部沿用官方实现（座位索引由 ShuangweixieEntity#getSeatIndex 提供）。
 * <p>
 * <b>目标持有者必须写准</b>：字节码里 (1) 是 Player.getVehicle()、(2) 是 LocalPlayer.getVehicle()，
 * 写成 Entity 会注入失败（此前正是如此，导致客户端启动即崩）。
 * 两处都带 require = 0：这是纯外观功能，万一以后 SBW 改了实现，
 * 也只会"没有 HUD"，不会让游戏起不来。
 */
@Mixin(VehicleHudOverlay.class)
public class VehicleHudOverlayRemoteMixin {

    /** 把"玩家骑乘的载具"改判为"玩家遥控的飞机"（原值非空时保持原值） */
    private static Entity dragonrise$vehicleOrRemoteAircraft(Entity self) {
        Entity ridden = self.getVehicle();
        if (ridden != null) {
            return ridden;
        }
        if (self instanceof Player player) {
            var aircraft = RemotePilotHelper.remoteAircraft(player);
            if (aircraft != null) {
                return aircraft;
            }
        }
        return null;
    }

    /** (1) shouldRenderHud(Player) 里的 player.vehicle */
    @Redirect(method = "shouldRenderHud", require = 0, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;getVehicle()Lnet/minecraft/world/entity/Entity;"))
    private Entity dragonrise$shouldRenderForRemotePilot(Player self) {
        return dragonrise$vehicleOrRemoteAircraft(self);
    }

    /** (2) render(RenderContext) 里的 player.vehicle（此处静态类型是 LocalPlayer） */
    @Redirect(method = "render(Lcom/atsuishio/superbwarfare/client/overlay/RenderContext;)V", require = 0,
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;getVehicle()Lnet/minecraft/world/entity/Entity;"))
    private Entity dragonrise$renderForRemotePilot(LocalPlayer self) {
        return dragonrise$vehicleOrRemoteAircraft(self);
    }
}
