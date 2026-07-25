package com.redabysslucia.dragonrise_reforge.entities.utils;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class VehicleLeashData {

    // 固定高位 id，避免 defineId(VehicleEntity.class, …) 与 SBW VehicleEntity
    // 的同步数据 id 冲突（会在创造栏 ContainerItemDecorator 临时 new 实体时
    // 抛出 Duplicate id value for 61!）。100/101 已被多车防浪板占用。
    public static final EntityDataAccessor<Optional<UUID>> LEASH_HOLDER =
            new EntityDataAccessor<>(200, EntityDataSerializers.OPTIONAL_UUID);

    /** 是否为铁链连接（不会断 + 双向拉力） */
    public static final EntityDataAccessor<Boolean> CHAIN_LEASHED =
            new EntityDataAccessor<>(201, EntityDataSerializers.BOOLEAN);

    public static final ResourceLocation LEAD_TEXTURE =
            new ResourceLocation("minecraft", "textures/entity/lead_knot.png");
    public static final ResourceLocation CHAIN_TEXTURE =
            new ResourceLocation("minecraft", "textures/block/chain.png");

    public static final Map<UUID, UUID> PLAYER_SELECTED_VEHICLE = new ConcurrentHashMap<>();

    /** 每 tick 处理拖拽 */
    public static void tickLeash(VehicleEntity self) {
        if (self.level().isClientSide) return;
        if (!(self.level() instanceof ServerLevel serverLevel)) return;

        Optional<UUID> holderUUID = self.getEntityData().get(LEASH_HOLDER);
        if (holderUUID.isEmpty()) return;

        Entity holder = serverLevel.getEntity(holderUUID.get());
        if (holder == null || !holder.isAlive() || holder.isRemoved()) {
            self.getEntityData().set(LEASH_HOLDER, Optional.empty());
            self.getEntityData().set(CHAIN_LEASHED, false);
            return;
        }

        boolean chain = self.getEntityData().get(CHAIN_LEASHED);
        double dist = self.distanceToSqr(holder);

        if (chain) {
            // 铁链：弹簧阻尼 F = -k*超出 - d*背离速度，10格硬边界
            double chainLen = 10.0;
            if (dist > chainLen * chainLen) {
                double currentDist = Math.sqrt(dist);
                double overshoot = currentDist - chainLen;

                Vec3 dir = self.position().subtract(holder.position()).normalize();
                double velAlong = self.getDeltaMovement().dot(dir);

                double k = 0.6; // 弹簧刚性
                double d = 0.15; // 阻尼
                double force = -(k * overshoot) - (d * velAlong);

                self.setDeltaMovement(self.getDeltaMovement().add(dir.scale(force)));
            }
            return;
        }

        // 栓绳：超 30 格断开
        if (dist > 900) {
            self.getEntityData().set(LEASH_HOLDER, Optional.empty());
            return;
        }
        // 栓绳：10 格内松弛
        if (dist <= 100) return;

        // 栓绳：超 10 格拉紧
        Vec3 diff = holder.position().subtract(self.position()).normalize();
        self.setDeltaMovement(self.getDeltaMovement().add(diff.scale(0.08)));

        if (holder instanceof VehicleEntity holderVehicle) {
            Vec3 reverse = self.position().subtract(holder.position()).normalize();
            holderVehicle.setDeltaMovement(holderVehicle.getDeltaMovement().add(reverse.scale(0.08)));
        }
    }
}
