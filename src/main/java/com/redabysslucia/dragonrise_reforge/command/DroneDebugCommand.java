package com.redabysslucia.dragonrise_reforge.command;

import com.atsuishio.superbwarfare.tools.NBTTool;
import com.atsuishio.superbwarfare.data.gun.GunData;
import com.atsuishio.superbwarfare.entity.vehicle.DroneEntity;
import com.atsuishio.superbwarfare.init.ModItems;
import com.atsuishio.superbwarfare.item.misc.MonitorItem;
import com.atsuishio.superbwarfare.tools.EntityFindUtil;
import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.entities.ShuangweixieEntity;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

/**
 * 遥控开火诊断命令（服务端执行）：
 * <pre>
 * /dr_drone        —— 打印遥控链路 + 席位 + 武器 + 弹药 + 容器状态
 * /dr_drone fire   —— <b>绕过网络包</b>直接在服务端调一次 vehicleShoot
 * </pre>
 * 用途：区分「客户端包没送到服务端」与「服务端武器/弹药逻辑拒绝开火」。
 * 若 {@code fire} 能打出弹丸而左键不能，问题一定在网络包通道；
 * 若 {@code fire} 也打不出，看打印出来的 ammo / canShoot / reloading 就能定位。
 */
@EventBusSubscriber(modid = Dragonrise_reforge.MODID, bus = EventBusSubscriber.Bus.GAME)
public class DroneDebugCommand {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("dr_drone")
                        .requires(source -> source.hasPermission(0))
                        .executes(ctx -> {
                            report(ctx.getSource());
                            return 1;
                        })
                        .then(Commands.literal("fire").executes(ctx -> {
                            forceFire(ctx.getSource());
                            return 1;
                        }))
        );
    }

    private static void say(CommandSourceStack source, String text) {
        source.sendSuccess(() -> Component.literal("[DR] " + text), false);
    }

    private static ShuangweixieEntity resolve(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            say(source, "必须由玩家执行");
            return null;
        }
        ItemStack stack = player.getMainHandItem();
        if (!stack.is(ModItems.MONITOR.get())) {
            say(source, "主手不是监控平板");
            return null;
        }
        CompoundTag tag = NBTTool.getTag(stack);
        if (tag == null || !tag.getBoolean(MonitorItem.USING) || !tag.getBoolean(MonitorItem.LINKED)) {
            say(source, "平板未处于遥控状态 (Using/Linked = " + (tag == null ? "无NBT" : tag.getBoolean(MonitorItem.USING) + "/" + tag.getBoolean(MonitorItem.LINKED)) + ")");
            return null;
        }
        String linked = tag.getString(MonitorItem.LINKED_DRONE);
        DroneEntity drone = EntityFindUtil.findDrone(player.level(), linked);
        say(source, "LinkedDrone=" + linked + " → " + (drone == null ? "未找到实体" : drone.getType().toString()));
        if (drone instanceof ShuangweixieEntity aircraft) {
            return aircraft;
        }
        if (drone != null) {
            say(source, "链接到的不是本模组双尾蝎（是 " + drone.getClass().getSimpleName() + "）");
        }
        return null;
    }

    private static void report(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        ShuangweixieEntity aircraft = resolve(source);
        if (player == null || aircraft == null) {
            return;
        }

        int seat = aircraft.getSeatIndex(player);
        say(source, "席位 getSeatIndex=" + seat + " / 座位数(最大乘员)=" + aircraft.getMaxPassengers());
        say(source, "hasWeapon(" + seat + ")=" + aircraft.hasWeapon(seat));
        say(source, "容器: size=" + aircraft.getContainerSize()
                + " hasMenu=" + aircraft.hasMenu()
                + " type=" + aircraft.computed().getVehicleContainerType());

        GunData gun = aircraft.getGunData(seat);
        if (gun == null) {
            say(source, "getGunData=" + null + "  ← 第 0 号武器解析不到（名字/定义对不上）");
            say(source, "gunDataMap 里的武器键=" + aircraft.getGunDataMap().keySet());
            return;
        }

        say(source, "武器名=" + aircraft.getGunName(seat) + " 选中槽位=" + aircraft.getSelectedWeapon(seat));
        say(source, "ammoCount(seat)=" + aircraft.getAmmoCount(seat)
                + " useBackpackAmmo=" + gun.useBackpackAmmo()
                + " 容器备弹=" + gun.countBackupAmmo(aircraft));
        say(source, "hasEnoughAmmo=" + gun.hasEnoughAmmoToShoot(aircraft)
                + " canShoot=" + gun.canShoot(aircraft)
                + " reloading=" + gun.reloading()
                + " overHeat=" + gun.overHeat.get());
        say(source, "提示：若 canShoot=false，看上两项即可知道是缺弹还是过热/装填中");
    }

    private static void forceFire(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        ShuangweixieEntity aircraft = resolve(source);
        if (player == null || aircraft == null) {
            return;
        }
        int seat = aircraft.getSeatIndex(player);
        say(source, "直接调用 vehicleShoot(player, null, null)（绕过网络包）… seat=" + seat);
        try {
            aircraft.vehicleShoot(player, (java.util.UUID) null, null);
            say(source, "调用完成，没有抛异常");
        } catch (Throwable t) {
            say(source, "抛异常: " + t.getClass().getSimpleName() + ": " + t.getMessage());
        }
    }
}
