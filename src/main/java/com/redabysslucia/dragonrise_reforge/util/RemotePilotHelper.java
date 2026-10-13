package com.redabysslucia.dragonrise_reforge.util;

import com.atsuishio.superbwarfare.tools.NBTTool;
import com.atsuishio.superbwarfare.init.ModItems;
import com.atsuishio.superbwarfare.item.misc.MonitorItem;
import com.atsuishio.superbwarfare.tools.EntityFindUtil;
import com.redabysslucia.dragonrise_reforge.entities.ShuangweixieEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 遥控链路判定工具（两侧通用）。
 * <p>
 * 服务器侧弹射物用"射手所骑乘的载具"来豁免自伤：
 * <ul>
 *   <li>{@code FastThrowableProjectile.onHit}: {@code entity.hasIndirectPassenger(owner)}；</li>
 *   <li>{@code IAdvancedHitDetection.findEntitiesOnPath}: {@code entity == owner.vehicle}；</li>
 *   <li>{@code AerialBombEntity.afterHitEntity}: {@code entity == owner.vehicle}。</li>
 * </ul>
 * 遥控驾驶时射手<b>不在</b>载具里，这三条豁免全部落空 → 自己打出的炮弹会打中自己的飞机。
 * 本工具提供"这台实体是不是该射手正在遥控的飞机"，供上面三处补一条等价豁免。
 */
public final class RemotePilotHelper {

    private RemotePilotHelper() {
    }

    /** 单 tick 缓存：一次路径扫描里会对每个候选实体问一遍，避免重复扫实体表 */
    private record Cached(long tick, Level level, ShuangweixieEntity aircraft) {
    }

    private static final Map<UUID, Cached> CACHE = new HashMap<>();

    /** 该玩家此刻遥控的飞机（主手监控平板 Using + Linked 且指向本模组双尾蝎）；没有返回 null */
    public static ShuangweixieEntity remoteAircraft(Player player) {
        if (player == null) {
            return null;
        }
        long tick = player.level().getGameTime();
        Cached cached = CACHE.get(player.getUUID());
        if (cached != null && cached.tick() == tick && cached.level() == player.level()) {
            return cached.aircraft();
        }

        ShuangweixieEntity found = resolve(player);
        CACHE.put(player.getUUID(), new Cached(tick, player.level(), found));
        return found;
    }

    private static ShuangweixieEntity resolve(Player player) {
        // 热路径：不能用 getOrCreateTag 给物品凭空造 NBT
        ItemStack stack = player.getMainHandItem();
        if (!stack.is(ModItems.MONITOR.get())) {
            return null;
        }
        CompoundTag tag = NBTTool.getTag(stack);
        if (tag == null || !tag.getBoolean(MonitorItem.USING) || !tag.getBoolean(MonitorItem.LINKED)) {
            return null;
        }
        var drone = EntityFindUtil.findDrone(player.level(), tag.getString(MonitorItem.LINKED_DRONE));
        return drone instanceof ShuangweixieEntity aircraft ? aircraft : null;
    }

    /**
     * 该玩家以遥控方式占用的席位号；没在遥控本模组飞机时返回 -1。
     * 用于"只有一号位能操控"的判定（与 SBW 骑乘时"只有 firstPassenger 发移动包"一致）。
     */
    /**
     * "骑乘中的载具优先，否则该玩家正在遥控的飞机"。
     * <p>
     * 供 {@code remap = false} 的 mixin 使用：那些 mixin 自身的原版成员调用<b>不会</b>被重映射，
     * 直接写 {@code entity.getVehicle()} 会在正式环境 NoSuchMethodError，
     * 因此统一走本方法（本类正常重映射）。
     */
    /**
     * {@code entity} 当前骑乘的载具（无则 null）。
     * <p>
     * 供 {@code remap = false} 的 mixin 使用：那类 mixin 自身的原版成员调用不会被重映射，
     * 直接写 {@code entity.getVehicle()} 打包后会 NoSuchMethodError，因此统一走本方法（本类正常重映射）。
     */
    /** 移除实体（供 remap = false 的 mixin 代调原版 {@code Entity.discard()}） */
    public static void discard(net.minecraft.world.entity.Entity entity) {
        if (entity != null) {
            entity.discard();
        }
    }

    public static net.minecraft.world.entity.Entity riddenVehicle(net.minecraft.world.entity.Entity entity) {
        return entity == null ? null : entity.getVehicle();
    }

    /**
     * 该监控平板是否指向这台无人机（UUID 比对）。
     * 同样用于 {@code remap = false} 的 mixin，避免它们直接调 {@code stack.getTag()} / {@code getStringUUID()}。
     */
    public static boolean monitorPointsAt(net.minecraft.world.entity.Entity drone,
                                          net.minecraft.world.item.ItemStack stack) {
        if (drone == null || stack == null || stack.isEmpty()) {
            return false;
        }
        net.minecraft.nbt.CompoundTag tag = NBTTool.getTag(stack);
        if (tag == null) {
            return false;
        }
        String linked = tag.getString(
                com.atsuishio.superbwarfare.item.misc.MonitorItem.LINKED_DRONE);
        return !linked.isEmpty() && linked.equals(drone.getStringUUID());
    }

    /** 副手是否拿着指定物品（供 remap = false 的 mixin 使用） */
    public static boolean holdsInOffhand(Player player, net.minecraft.world.item.Item item) {
        return player != null && player.getOffhandItem().is(item);
    }

    /**
     * 把一声载具音效播给"遥控操作者"。
     * <p>
     * 遥控者常离无人机几百格，而 SBW 把这类音效播在机身位置（{@code level.playSound(null, this, ...)}），
     * 于是"操作发生了但听不到"。这里在远处时改播到操作者位置；
     * 距离 ≤16 格时按原样播在载具位置，避免机身与操作者各响一声。
     */
    public static void playSoundForController(net.minecraft.world.level.Level level,
                                              net.minecraft.world.entity.Entity vehicle,
                                              net.minecraft.world.entity.player.Player except,
                                              net.minecraft.world.entity.Entity entity,
                                              net.minecraft.sounds.SoundEvent sound,
                                              net.minecraft.sounds.SoundSource source,
                                              float volume, float pitch) {
        if (level == null || sound == null) {
            return;
        }
        ShuangweixieEntity controller = controllerShip(vehicle);
        if (controller != null) {
            Player pilot = controller.getController();
            if (pilot != null && pilot.distanceTo(vehicle) > 16.0f) {
                level.playSound(null, pilot.getX(), pilot.getY(), pilot.getZ(), sound, source, volume, pitch);
                return;
            }
        }
        level.playSound(except, entity, sound, source, volume, pitch);
    }

    /** 若该实体是本模组飞机则返回它（否则 null） */
    private static ShuangweixieEntity controllerShip(net.minecraft.world.entity.Entity entity) {
        return entity instanceof ShuangweixieEntity aircraft ? aircraft : null;
    }

    public static net.minecraft.world.entity.Entity vehicleOrRemoteAircraft(
            net.minecraft.world.entity.Entity entity) {
        net.minecraft.world.entity.Entity ridden = entity.getVehicle();
        if (ridden != null) {
            return ridden;
        }
        if (entity instanceof Player player) {
            return remoteAircraft(player);
        }
        return null;
    }

    /**
     * 玩家是否正手持"已激活"的监控平板遥控本模组飞机；是则返回那架飞机，否则 null。
     * 语义等同 {@link #remoteAircraft(Player)}，只是名字更贴近调用处（用于 remap = false 的 mixin）。
     */
    public static ShuangweixieEntity activeRemoteMonitor(Player player) {
        return remoteAircraft(player);
    }

    public static int remoteSeat(Player player) {
        ShuangweixieEntity aircraft = remoteAircraft(player);
        return aircraft == null ? -1 : aircraft.dr$remoteSeat(player);
    }

    /**
     * {@code entity} 是否就是"射手遥控驾驶的那架飞机"。
     * 与原版"射手骑乘的载具"语义等价，用于弹射物自伤豁免。
     */
    public static boolean isRemoteControlledVehicle(Entity entity, Entity shooter) {
        if (entity == null || !(shooter instanceof Player player)) {
            return false;
        }
        return remoteAircraft(player) == entity;
    }
}
