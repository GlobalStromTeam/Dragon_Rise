package com.redabysslucia.dragonrise_reforge.client.event;

import com.atsuishio.superbwarfare.api.event.ClientVehicleFireEvent;
import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.entities.Spg9Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 客户端开火动画派发。
 *
 * <p>superb 的开火链路是：客户端发送 VehicleFireMessage → 服务端调用 {@code vehicleShoot}
 * → 服务端广播 {@code VehicleShootClientMessage}；客户端收到该消息后<b>只</b>派发
 * {@link ClientVehicleFireEvent}，<b>不会</b>再走 {@code vehicleShoot}。
 * 因此载具实体里覆写 {@code vehicleShoot} 的客户端分支是死代码，开火动画必须挂在本事件上。
 */
@Mod.EventBusSubscriber(modid = Dragonrise_reforge.MODID, value = Dist.CLIENT)
public class VehicleFireAnimationHandler {

    @SubscribeEvent
    public static void onVehicleFire(ClientVehicleFireEvent event) {
        if (event.getVehicle() instanceof Spg9Entity spg9) {
            spg9.playFireAnimation();
        }
    }
}
