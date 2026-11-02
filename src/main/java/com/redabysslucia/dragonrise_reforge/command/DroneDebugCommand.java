package com.redabysslucia.dragonrise_reforge.command;

import com.atsuishio.superbwarfare.data.gun.GunData;
import com.atsuishio.superbwarfare.entity.vehicle.DroneEntity;
import com.atsuishio.superbwarfare.init.ModItems;
import com.atsuishio.superbwarfare.item.misc.MonitorItem;
import com.atsuishio.superbwarfare.tools.EntityFindUtil;
import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.entities.AbstractRemotePilotDrone;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 遥控开火诊断命令（服务端执行）：
 * <pre>
 * /dr_drone        —— 打印遥控链路 + 席位 + 武器 + 弹药 + 容器状态
 * /dr_drone fire   —— <b>绕过网络包</b>直接在服务端调一次 vehicleShoot
 * /dr_drone shot   —— 打印出膛点诊断：JSON 配置的偏移、变换后的世界坐标、相机坐标、两者差
 * </pre>
 * 用途：区分「客户端包没送到服务端」与「服务端武器/弹药逻辑拒绝开火」。
 * 若 {@code fire} 能打出弹丸而左键不能，问题一定在网络包通道；
 * 若 {@code fire} 也打不出，看打印出来的 ammo / canShoot / reloading 就能定位。
 */
@Mod.EventBusSubscriber(modid = Dragonrise_reforge.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
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

    private static AbstractRemotePilotDrone resolve(CommandSourceStack source) {
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
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.getBoolean(MonitorItem.USING) || !tag.getBoolean(MonitorItem.LINKED)) {
            say(source, "平板未处于遥控状态 (Using/Linked = " + (tag == null ? "无NBT" : tag.getBoolean(MonitorItem.USING) + "/" + tag.getBoolean(MonitorItem.LINKED)) + ")");
            return null;
        }
        String linked = tag.getString(MonitorItem.LINKED_DRONE);
        DroneEntity drone = EntityFindUtil.findDrone(player.level(), linked);
        say(source, "LinkedDrone=" + linked + " → " + (drone == null ? "未找到实体" : drone.getType().toString()));
        if (drone instanceof AbstractRemotePilotDrone aircraft) {
            return aircraft;
        }
        if (drone != null) {
            say(source, "链接到的不是本模组遥控无人机（是 " + drone.getClass().getSimpleName() + "）");
        }
        return null;
    }

    private static void report(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        AbstractRemotePilotDrone aircraft = resolve(source);
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

    /**
     * 出膛点诊断：把"配置值 → 变换后世界坐标 → 与相机的关系"整条链打出来。
     * <p>
     * 开一炮看不出弹从哪出来时，用这个对着看三件事即可定位：
     * <ol>
     *   <li>{@code 配置偏移} —— 载具 json 里 {@code Weapons.<武器>.ShootPos.Positions} 的原始值（Barrel 空间）；</li>
     *   <li>{@code 世界出膛点} —— 经 {@code getShootPos} 变换（含 BarrelPos 平移 + turretXRot 旋转）后的真实坐标；</li>
     *   <li>{@code 与相机的差} —— 出膛点减相机点，正前方是 +Z；这个向量的长度就是"弹从镜头前面多远冒出来"。</li>
     * </ol>
     */
    private static void shotReport(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        AbstractRemotePilotDrone aircraft = resolve(source);
        if (player == null || aircraft == null) {
            return;
        }
        int seat = aircraft.getSeatIndex(player);
        GunData gun = aircraft.getGunData(seat);
        if (gun == null) {
            say(source, "getGunData=null（席位 " + seat + "），出膛点会退回 getEyePosition（= 眼睛/镜头位置）");
            return;
        }

        net.minecraft.world.phys.Vec3 raw = gun.firePosition();
        net.minecraft.world.phys.Vec3 world = aircraft.getShootPos(player, 1f);
        net.minecraft.world.phys.Vec3 cam = aircraft.getCameraPos(player, 1f);
        net.minecraft.world.phys.Vec3 veh = aircraft.position();
        net.minecraft.world.phys.Vec3 dir = aircraft.getShootVec(player, 1f);
        net.minecraft.world.phys.Vec3 diff = world.subtract(cam);

        say(source, "席位=" + seat + " 武器=" + aircraft.getGunName(seat));
        say(source, "配置偏移(Barrel 空间) = [" + fmt(raw.x) + ", " + fmt(raw.y) + ", " + fmt(raw.z) + "]");
        say(source, "BarrelPos(机体) = " + aircraft.getBarrelPosition()
                + "  turretXRot=" + fmt(aircraft.getTurretXRot()) + " 度（>0 = 炮口下压）");
        say(source, "载具坐标 = [" + fmt(veh.x) + ", " + fmt(veh.y) + ", " + fmt(veh.z) + "]");
        say(source, "相机坐标 = [" + fmt(cam.x) + ", " + fmt(cam.y) + ", " + fmt(cam.z) + "]");
        say(source, "出膛点   = [" + fmt(world.x) + ", " + fmt(world.y) + ", " + fmt(world.z) + "]");
        say(source, "出膛点-相机 = [" + fmt(diff.x) + ", " + fmt(diff.y) + ", " + fmt(diff.z)
                + "]  距离 " + fmt(diff.length()) + " 格");
        double along = diff.dot(dir);
        double lateral = Math.sqrt(Math.max(0.0, diff.lengthSqr() - along * along));
        say(source, "弹道方向 = [" + fmt(dir.x) + ", " + fmt(dir.y) + ", " + fmt(dir.z) + "]");
        say(source, "沿炮轴 = " + fmt(along) + " 格   横向偏差 = " + fmt(lateral) + " 格"
                + (lateral < 0.02 ? "  ← 在准星正中心（横向≈0）" : "  ← 偏离准星轴线"));
        say(source, "正前方 = +Z；出膛点减相机的 z 接近 0 或为负 → 仍然是从镜头里冒出来");
    }

    private static String fmt(double v) {
        return String.format(java.util.Locale.ROOT, "%.3f", v);
    }

    private static void forceFire(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        AbstractRemotePilotDrone aircraft = resolve(source);
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
