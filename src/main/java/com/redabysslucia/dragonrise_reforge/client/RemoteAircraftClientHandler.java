package com.redabysslucia.dragonrise_reforge.client;

import com.atsuishio.superbwarfare.tools.NBTTool;
import com.atsuishio.superbwarfare.event.ClientEventHandler;
import com.atsuishio.superbwarfare.init.ModItems;
import com.atsuishio.superbwarfare.item.misc.MonitorItem;
import com.atsuishio.superbwarfare.network.message.send.SwitchVehicleWeaponMessage;
import com.atsuishio.superbwarfare.tools.EntityFindUtil;
import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.entities.ShuangweixieEntity;
import com.redabysslucia.dragonrise_reforge.entities.ZBD04AEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

public class RemoteAircraftClientHandler {

    /** 当前正在遥控的飞机；没在遥控返回 null */
    public static ShuangweixieEntity controlledAircraft() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) {
            return null;
        }
        ItemStack stack = player.getMainHandItem();
        if (!stack.is(ModItems.MONITOR.get())) {
            return null;
        }
        CompoundTag tag = NBTTool.getTag(stack);
        if (tag == null || !tag.getBoolean(MonitorItem.USING) || !tag.getBoolean(MonitorItem.LINKED)) {
            return null;
        }
        var drone = EntityFindUtil.findDrone(mc.level, tag.getString(MonitorItem.LINKED_DRONE));
        return drone instanceof ShuangweixieEntity aircraft ? aircraft : null;
    }

    @SubscribeEvent
    public static void onMouseButton(InputEvent.MouseButton.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        // 打开了任何界面（ESC 菜单 / 背包 / 聊天…）：一律不拦截，否则界面按钮点不动。
        // 同时清掉开火状态 —— 界面会吞掉"松开左键"事件，不清就会卡在连发上。
        if (mc.screen != null) {
            ClientEventHandler.holdFireVehicle = false;
            return;
        }

        ShuangweixieEntity aircraft = controlledAircraft();
        if (aircraft == null) {
            ClientEventHandler.holdFireVehicle = false;
            return;
        }
        if (event.getButton() != 0) {
            return;
        }

        // 拦掉原版左键（避免挥手臂/挖方块）；真正的开火由 SBW 原生的
        // handleVehicleGunShoot 循环按武器射速驱动，这里只负责把"按住/松开"转成它的状态位。
        event.setCanceled(true);
        ClientEventHandler.holdFireVehicle = event.getAction() != 0;
    }

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        ShuangweixieEntity aircraft = controlledAircraft();
        if (aircraft == null) {
            return;
        }
        // 1.21.1 的 MouseScrollingEvent 把滚动量拆成 X/Y 两个分量（1.20.1 只有一个 getScrollDelta()）
        double delta = event.getScrollDeltaY();
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
        // 1.21.1：SBW 的消息也是 CustomPacketPayload，用 NeoForge 的 PacketDistributor 发。
        net.neoforged.neoforge.network.PacketDistributor.sendToServer(
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
        ShuangweixieEntity aircraft = controlledAircraft();
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
