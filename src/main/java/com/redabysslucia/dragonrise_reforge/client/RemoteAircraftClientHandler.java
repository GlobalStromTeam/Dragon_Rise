package com.redabysslucia.dragonrise_reforge.client;

import com.atsuishio.superbwarfare.event.ClientEventHandler;
import com.atsuishio.superbwarfare.init.ModItems;
import com.atsuishio.superbwarfare.item.misc.MonitorItem;
import com.atsuishio.superbwarfare.network.NetworkRegistry;
import com.atsuishio.superbwarfare.network.message.send.SwitchVehicleWeaponMessage;
import com.atsuishio.superbwarfare.tools.EntityFindUtil;
import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.entities.AbstractRemotePilotDrone;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 遥控驾驶时的客户端辅助（FORGE 总线）：
 * <ul>
 *   <li>左键：拦掉原版攻击动作（挥手臂/挖方块），并把"按住/松开"写进 SBW 的状态位
 *       {@code ClientEventHandler.holdFireVehicle}；开火本身走 SBW 自己的循环
 *       （{@code ClientEventHandler.handleVehicleGunShoot}，门槛由 ClientEventHandlerRemoteGunShootMixin 放行）；</li>
 *   <li>滚轮：发 SBW 原生 {@code SwitchVehicleWeaponMessage} 切武器；</li>
 *   <li>相机：把飞机的 roll 写进 {@code ClientEventHandler.cameraRoll}，让地平线跟着压坡度。</li>
 * </ul>
 * 早期版本这里自己算 RPM 间隔开火、自己发自定义包，现在已全部换回 SBW 原生机制。
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Dragonrise_reforge.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class RemoteAircraftClientHandler {

    /** 当前正在遥控的飞机；没在遥控返回 null */
    public static AbstractRemotePilotDrone controlledAircraft() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) {
            return null;
        }
        ItemStack stack = player.getMainHandItem();
        if (!stack.is(ModItems.MONITOR.get())) {
            return null;
        }
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.getBoolean(MonitorItem.USING) || !tag.getBoolean(MonitorItem.LINKED)) {
            return null;
        }
        var drone = EntityFindUtil.findDrone(mc.level, tag.getString(MonitorItem.LINKED_DRONE));
        return drone instanceof AbstractRemotePilotDrone aircraft ? aircraft : null;
    }

    /**
     * 是否由本处理器置起了 {@code holdFireVehicle}。
     * <p>
     * 这个状态位是 SBW 的<b>全局</b>单例：任何载具的自动武器连发都靠它。非遥控玩家（主手不是
     * 控制终端）根本不该碰它 —— 一旦在这里无条件清零，玩家按住左键连发时只要再按一下右键开镜
     * （或任何鼠标事件），连发就会被掐断，而且看起来像"切换放大倍率把自动武器停掉了"。
     * 只有我们自己置起来的，才有资格清回去。
     */
    private static boolean remoteFiring;

    @SubscribeEvent
    public static void onMouseButton(InputEvent.MouseButton.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        // 打开了任何界面（ESC 菜单 / 背包 / 聊天…）：一律不拦截，否则界面按钮点不动。
        // 界面的开合 SBW 自己在 handleVehicleGunShoot 开头就会清状态位，这里只管收回我们的。
        if (mc.screen != null) {
            releaseRemoteFire();
            return;
        }

        AbstractRemotePilotDrone aircraft = controlledAircraft();
        if (aircraft == null) {
            releaseRemoteFire();
            return;
        }
        if (event.getButton() != 0) {
            return;
        }

        // 拦掉原版左键（避免挥手臂/挖方块）；真正的开火由 SBW 原生的
        // handleVehicleGunShoot 循环按武器射速驱动，这里只负责把"按住/松开"转成它的状态位。
        boolean firing = event.getAction() != 0;    // 1 = 按下, 0 = 释放
        event.setCanceled(true);
        ClientEventHandler.holdFireVehicle = firing;
        remoteFiring = firing;
    }

    /** 只清我们自己的连发状态，绝不误伤玩家用普通载具武器按住的连发。 */
    private static void releaseRemoteFire() {
        if (remoteFiring) {
            ClientEventHandler.holdFireVehicle = false;
            remoteFiring = false;
        }
    }

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        AbstractRemotePilotDrone aircraft = controlledAircraft();
        if (aircraft == null) {
            return;
        }
        double delta = event.getScrollDelta();
        if (delta == 0) {
            return;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || aircraft.getSeatIndex(player) < 0 || !aircraft.hasWeapon(aircraft.getSeatIndex(player))) {
            return;
        }
        // SBW 原生切武器包（第一个参数是席位号，第二参取负 —— 官方客户端就是这么发的
        // ClickEventHandler.kt:213 `SwitchVehicleWeaponMessage(index, -scroll, true)`）。
        // 服务端 handler 里的 player.vehicle 门槛已由 SwitchVehicleWeaponMessageRemoteMixin 对遥控者放行。
        NetworkRegistry.PACKET_HANDLER.sendToServer(
                new SwitchVehicleWeaponMessage(aircraft.getSeatIndex(player), -delta, true));
        event.setCanceled(true);
    }

    private static long lastRollLogMs;

    /**
     * 让遥控视角跟着飞机压坡度。
     * <p>
     * SBW 的 roll 是普通字段（不参与 entityData 同步），各客户端靠"本地也跑一遍引擎"得到它，
     * 而它的相机角度处理只服务骑乘者 → 遥控者这里需要自己把 roll 写进
     * {@code ClientEventHandler.cameraRoll}（官方相机代码读这个字段）。
     */
    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        AbstractRemotePilotDrone aircraft = controlledAircraft();
        if (aircraft == null) {
            return;
        }
        ClientEventHandler.cameraRoll = aircraft.getRoll((float) event.getPartialTick());

        long now = System.currentTimeMillis();
        if (now - lastRollLogMs > 500L) {
            lastRollLogMs = now;
            Dragonrise_reforge.LOGGER.info("[DR-CAM] aircraftRoll={} cameraRoll={} pitch={} partialTick={}",
                    aircraft.getRoll(), ClientEventHandler.cameraRoll, aircraft.getXRot(), event.getPartialTick());
        }
    }
}
