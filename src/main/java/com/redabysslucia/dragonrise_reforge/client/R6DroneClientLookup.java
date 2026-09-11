package com.redabysslucia.dragonrise_reforge.client;

import com.redabysslucia.dragonrise_reforge.entities.special.R6DroneEntity;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

/**
 * 客户端按 UUID 查找本项目侦察无人车（{@link R6DroneEntity}）。
 *
 * <p>监控平板通过 {@code MonitorItem.link(stack, drone.getStringUUID())} 把无人车 UUID 写进
 * {@code LinkedDrone} 标签，本类负责把该 UUID 字符串还原成世界里的无人车实体。
 * 单独成类（而不是写在 {@link R6DroneEntity} 里）的原因：查找需要 {@link ClientLevel}，
 * 而实体是 common 类，引用 {@code net.minecraft.client.*} 会导致专用服务器加载实体类失败。
 * 服务端路径仍由 {@link R6DroneEntity#findServerDrone(ServerLevel, String)} 提供。
 *
 * <p>本类不做类型之外的分端逻辑：服务端只走 {@code ServerLevel} 分支，客户端只走
 * {@code ClientLevel} 分支，因此 {@code MonitorItemMixin}（双端生效）可以安全调用。
 */
public final class R6DroneClientLookup {

    /** 上一次命中的查找结果（同一 level + 同一 UUID 时直接复用，避免每帧遍历全部实体）。
     *  只存实体本身、不存 Level 引用，避免退出世界后静态字段滞留整个 ClientLevel。 */
    private static String cachedUuid;
    private static R6DroneEntity cachedDrone;

    private R6DroneClientLookup() {
    }

    /**
     * 按 UUID 字符串查找无人车实体。
     *
     * @param level      实体所在世界（客户端为 {@link ClientLevel}，服务端为 {@link ServerLevel}）
     * @param uuidString 无人车 UUID 字符串（{@code LinkedDrone} 标签内容）
     * @return 匹配的无人车；世界/字符串无效、实体不存在或不是本项目无人车时返回 {@code null}
     */
    @Nullable
    public static R6DroneEntity findDrone(@Nullable Level level, @Nullable String uuidString) {
        // 合法 UUID 字符串固定 36 字符：先做长度快速拒绝，
        // 避免 "none"/"undefined" 之类哨兵值进入 UUID.fromString 的异常 + 填栈开销。
        if (level == null || uuidString == null || uuidString.length() != 36) return null;

        // 服务端：交给实体里的既有实现（ServerLevel.getEntities().get(uuid)）
        if (level instanceof ServerLevel serverLevel) {
            return R6DroneEntity.findServerDrone(serverLevel, uuidString);
        }

        // 客户端：ClientLevel.getEntities() 是 protected，公开可见的是
        // entitiesForRendering()（= 全部已追踪实体），按缓存 → 遍历查找。
        if (!(level instanceof ClientLevel clientLevel)) return null;

        if (uuidString.equalsIgnoreCase(cachedUuid) && isUsable(cachedDrone, level)) {
            return cachedDrone;
        }

        R6DroneEntity found = null;
        for (Entity entity : clientLevel.entitiesForRendering()) {
            if (entity instanceof R6DroneEntity drone && uuidString.equalsIgnoreCase(drone.getStringUUID())) {
                found = drone;
                break;
            }
        }

        cachedUuid = uuidString;
        cachedDrone = found;
        return found;
    }

    /** 缓存有效性：实体仍在传入的这个世界里且未被移除 */
    private static boolean isUsable(@Nullable R6DroneEntity drone, Level level) {
        return drone != null && !drone.isRemoved() && drone.level() == level;
    }
}
