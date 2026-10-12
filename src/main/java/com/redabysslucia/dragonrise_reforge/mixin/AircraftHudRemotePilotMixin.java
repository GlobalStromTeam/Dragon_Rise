package com.redabysslucia.dragonrise_reforge.mixin;

import com.atsuishio.superbwarfare.client.overlay.weapon.AircraftHud;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 放开官方战斗机 HUD 的"必须是一号乘员"判断，让<b>遥控者</b>也能看到 {@code AircraftHud}。
 * <p>
 * {@code AircraftHud.render} 开头是：
 * <pre>if (player !== vehicle.getFirstPassenger()) return</pre>
 * 遥控驾驶时玩家不在载具上，这句直接让 HUD 不渲染。这里把它重定向为：
 * 真有乘员就按原样；没有乘员但当前玩家在本载具上<b>占着座位</b>（{@code getSeatIndex >= 0}，
 * 遥控者由 {@code ShuangweixieEntity#getSeatIndex} 返回座位号）时，就把该玩家当作一号乘员返回。
 * <p>
 * 只影响这一个方法内部；对真人驾驶的载具完全等价（真乘员走原路径）。
 * <p>
 * <b>注意 remap</b>：这里注入的目标 {@code VehicleEntity.getFirstPassenger()} 是<b>原版方法</b>
 * （继承自 Entity），正式环境里是 SRG 名，必须让 refmap 去重映射，
 * 所以本 mixin <b>不能</b>写 {@code remap = false}（那会导致 dev 能跑、打包后注入失败）。
 */
@Mixin(value = AircraftHud.class)
public class AircraftHudRemotePilotMixin {

    @Redirect(method = "render", at = @At(value = "INVOKE",
            target = "Lcom/atsuishio/superbwarfare/entity/vehicle/base/VehicleEntity;getFirstPassenger()Lnet/minecraft/world/entity/Entity;"))
    private Entity dragonrise$remotePilotAsFirstPassenger(VehicleEntity vehicle) {
        Entity real = vehicle.getFirstPassenger();
        if (real != null) {
            return real;
        }
        Player player = Minecraft.getInstance().player;
        if (player != null && vehicle.getSeatIndex(player) >= 0) {
            return player;
        }
        return null;
    }

}
