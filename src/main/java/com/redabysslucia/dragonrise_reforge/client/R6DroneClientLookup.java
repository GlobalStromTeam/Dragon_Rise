package com.redabysslucia.dragonrise_reforge.client;

import com.redabysslucia.dragonrise_reforge.entities.special.R6DroneEntity;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.UUID;

/**
 * 侦察无人车/攻击无人机客户端查找器（纯客户端）。
 * <p>
 * 原 R6DroneEntity.findDrone 同时含服务端/客户端分支，客户端分支引用
 * ClientLevel.entitiesForRendering()（net.minecraft.client.*），而 R6DroneEntity 是
 * common 实体类（专用服务器也会加载）——类加载时 Forge dist cleaner 检测到
 * client 类引用直接抛错，导致服务器上 `new R6DroneEntity()`（部署器）失败：
 * "Attempted to load class ... for invalid dist DEDICATED_SERVER"。
 * <p>
 * 因此客户端查找（含缓存）整体迁到这里；服务端查找用 R6DroneEntity.findServerDrone。
 */
@OnlyIn(Dist.CLIENT)
public final class R6DroneClientLookup {

    private R6DroneClientLookup() {
    }

    // 客户端查找缓存：findDrone 在渲染线程每帧被相机 mixin 调用，
    // entitiesForRendering() 遍历全部实体是 O(N)，场景实体多时会造成掉帧卡顿。
    // 缓存有效引用（跨 tick 复用），实体消失/换图/换 UUID 时自动失效重查。
    private static R6DroneEntity cachedDrone;
    private static String cachedDroneUuid;
    private static long cachedDroneLevelTick = -1;

    /** 按监控平板 LinkedDrone 的 UUID 查找无人车（仅客户端；服务端用 findServerDrone） */
    public static R6DroneEntity findDrone(Level level, String uuidString) {
        if (uuidString == null || uuidString.length() != 36) return null;
        if (!(level instanceof ClientLevel clientLevel)) return null;

        UUID uuid;
        try {
            uuid = UUID.fromString(uuidString);
        } catch (IllegalArgumentException e) {
            return null;
        }

        // 缓存命中：引用仍有效（同图、未移除、UUID 一致）则直接复用，避免每帧 O(N) 遍历
        if (cachedDrone != null && !cachedDrone.isRemoved()
                && cachedDrone.level() == level
                && cachedDroneUuid != null && cachedDroneUuid.equals(uuidString)) {
            return cachedDrone;
        }
        // 缓存失效：本 tick 已查过则不再重复遍历（结果为空说明确实不存在，保持 null）
        long tick = level.getGameTime();
        if (cachedDroneLevelTick == tick && cachedDroneUuid != null
                && cachedDroneUuid.equals(uuidString)) {
            return cachedDrone;
        }
        cachedDroneLevelTick = tick;
        cachedDroneUuid = uuidString;
        cachedDrone = null;
        for (Entity ent : clientLevel.entitiesForRendering()) {
            if (ent.getUUID().equals(uuid) && ent instanceof R6DroneEntity drone) {
                cachedDrone = drone;
                break;
            }
        }
        return cachedDrone;
    }
}
