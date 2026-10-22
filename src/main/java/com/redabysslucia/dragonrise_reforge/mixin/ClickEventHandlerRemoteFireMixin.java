package com.redabysslucia.dragonrise_reforge.mixin;

import com.atsuishio.superbwarfare.event.ClickEventHandler;
import com.atsuishio.superbwarfare.event.ClientEventHandler;
import com.atsuishio.superbwarfare.init.ModItems;
import com.atsuishio.superbwarfare.tools.EntityFindUtil;
import com.redabysslucia.dragonrise_reforge.entities.ShuangweixieEntity;
import com.redabysslucia.dragonrise_reforge.util.RemotePilotHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 手持监控平板遥控双尾蝎时，左键不再走官方的"无人机左键"。
 * <p>
 * 官方 {@code handleWeaponFirePress} 里对监控平板是这样处理的：
 * <pre>if (stack.is(MONITOR) &amp;&amp; !副手炮击指示器) droneLeftClick(stack, player)</pre>
 * 而 {@code droneLeftClick} 是拿无人机做一次射线检测，然后发 {@code DroneFireMessage}
 * （用途是迫击炮/炮击标定，或让无人机进入自爆），跟载具武器没有关系。
 * 武装固定翼的左键应该是"开火" —— 那一发由 {@code RemoteAircraftClientHandler} 按 RPM 发
 * {@code VehicleFireMessage}，所以这里把官方的这条分支拦掉，
 * 免得点一下左键不但不开炮，反而给无人机下了一个自爆/标定指令。
 * <p>
 * 副手拿炮击指示器时保留官方行为（那时玩家确实是在标定目标）。
 */
@Mixin(value = ClickEventHandler.class, remap = false)
public class ClickEventHandlerRemoteFireMixin {

    @Inject(method = "handleWeaponFirePress", at = @At("HEAD"), cancellable = true)
    private void dragonrise$remoteFireInsteadOfDroneCommand(Player player, ItemStack stack, CallbackInfo ci) {
        // 只用本模组 helper：本 mixin remap = false，自身调原版成员打包后会 NoSuchMethodError。
        ShuangweixieEntity aircraft = RemotePilotHelper.activeRemoteMonitor(player);
        if (aircraft == null) {
            return;
        }
        if (RemotePilotHelper.holdsInOffhand(player, ModItems.ARTILLERY_INDICATOR.get())) {
            return;     // 炮击标定：保持官方逻辑
        }

        // 用官方自己的开火状态位接管：官方循环 handleVehicleGunShoot 读的就是它
        // （门槛已由 ClientEventHandlerRemoteGunShootMixin 对遥控者放行），
        // 这样左键与 FIRE 键都能按武器射速连发，时序/预测/音效全归官方。
        ClientEventHandler.holdFireVehicle = true;
        ci.cancel();
    }

    /**
     * 松手时清掉开火状态位（与官方 handleWeaponFireRelease 同样的语义）。
     * 官方那份对遥控者不一定会执行到，这里兜底，避免松开后仍连发。
     */
    @Inject(method = "handleWeaponFireRelease", at = @At("HEAD"))
    private void dragonrise$remoteFireRelease(CallbackInfo ci) {
        // 目标方法没有参数（官方那份直接清状态位），所以玩家从本模组的客户端处理器取。
        // 本 mixin remap = false，不能在这里直接调原版成员（如 Minecraft.getInstance()）。
        if (com.redabysslucia.dragonrise_reforge.client.RemoteAircraftClientHandler.controlledAircraft() != null) {
            ClientEventHandler.holdFireVehicle = false;
        }
    }
}
