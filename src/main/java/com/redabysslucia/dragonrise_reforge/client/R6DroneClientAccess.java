package com.redabysslucia.dragonrise_reforge.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * R6DroneEntity 的客户端专用访问入口。
 * <p>
 * 实体类在专用服务器上也会被加载和校验。若实体里直接写 {@code Player p = Minecraft.getInstance().player;}，
 * JVM 校验时需要确认 LocalPlayer 是 Player 的子类，会去加载 LocalPlayer，而 NeoForge 在服务端禁止加载
 * 客户端类，结果 r6_drone / attack_drone 在服务器上无法生成。把客户端调用集中到这里，
 * 并且只返回通用类型（Player、Entity），实体类就不会引用任何客户端类。
 * 本类只能在 {@code level.isClientSide()} 为 true 的分支中调用。
 * <p>
 * Client-only access for R6DroneEntity. Entity classes are also loaded and verified on a dedicated
 * server; writing {@code Player p = Minecraft.getInstance().player;} in the entity makes the verifier
 * load LocalPlayer to check the assignment, which NeoForge forbids on the server, so the drones could
 * not be spawned there. Keeping those calls here, and returning only common types, keeps client
 * classes out of the entity. Only call this from {@code level.isClientSide()} branches.
 */
public final class R6DroneClientAccess {

    private R6DroneClientAccess() {
    }

    /** 本地玩家（可能为 null）/ The local player, or null */
    @Nullable
    public static Player localPlayer() {
        return Minecraft.getInstance().player;
    }

    /** 客户端世界中参与渲染的实体 / Entities of a client level that are being rendered */
    public static Iterable<Entity> entitiesForRendering(Level level) {
        return ((ClientLevel) level).entitiesForRendering();
    }
}
