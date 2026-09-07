package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.client.animation.AnimationPlayType;
import com.atsuishio.superbwarfare.event.ClientEventHandler;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("removal")
public class M3A3Entity extends VehicleEntity {

    public static final EntityDataAccessor<Integer> MISSILE_STATE =
            SynchedEntityData.defineId(M3A3Entity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> DEPLOY_TIMER =
            SynchedEntityData.defineId(M3A3Entity.class, EntityDataSerializers.INT);

    // 客户端动画状态机缓存
    private String lastMissileAnim = "";

    public M3A3Entity(EntityType<M3A3Entity> type, Level world) {
        super(type, world);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(MISSILE_STATE, 0);
        this.entityData.define(DEPLOY_TIMER, 0);
    }


    private final Map<UUID, Integer> lastMessageTick = new HashMap<>();

    private boolean isMoving() {
        Vec3 motion = this.getDeltaMovement();
        return Math.abs(motion.x) > 0.01 || Math.abs(motion.z) > 0.01;
    }

    /**
     * 遍历所有乘客，检查是否有任意乘客在任意座位选择了导弹武器
     */
    private boolean anyPassengerHasMissile() {
        for (var passenger : getPassengers()) {
            int seatIndex = getSeatIndex(passenger);
            if (seatIndex >= 0 && getSelectedWeapon(seatIndex) == 1) {
                return true;
            }
        }
        return false;
    }

    private void showClientMessage(LivingEntity living, String key) {
        if (level().isClientSide && living instanceof Player player) {
            if (ClientEventHandler.holdFireVehicle) {
                int lastTick = lastMessageTick.getOrDefault(player.getUUID(), -100);
                if (tickCount - lastTick > 20) {
                    lastMessageTick.put(player.getUUID(), tickCount);
                    player.displayClientMessage(Component.translatable(key), true);
                }
            }
        }
    }

    @Override
    public boolean canShoot(LivingEntity living) {
        int seatIndex = getSeatIndex(living);
        if (seatIndex < 0) return super.canShoot(living);
        int selectedWeapon = getSelectedWeapon(seatIndex);
        if (selectedWeapon != 1) return super.canShoot(living);

        if (isMoving()) {
            showClientMessage(living, "message.dragonrise_reforge.m3a3_stop_to_shoot");
            return false;
        }

        if (entityData.get(MISSILE_STATE) != 2) {
            showClientMessage(living, "message.dragonrise_reforge.m3a3_wait_deploy");
            return false;
        }

        return super.canShoot(living);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            tickMissileAnimation();
            return;
        }

        int currentState = entityData.get(MISSILE_STATE);
        boolean anyMissileSelected = anyPassengerHasMissile();
        boolean moving = isMoving();

        if (moving) {
            // 移动时强制收起
            if (currentState == 2 || currentState == 1) {
                entityData.set(MISSILE_STATE, 3);
                entityData.set(DEPLOY_TIMER, 30);
            }
        } else if (anyMissileSelected) {
            // 停止且有任意乘客选择导弹时展开
            if (currentState == 0) {
                entityData.set(MISSILE_STATE, 1);
                entityData.set(DEPLOY_TIMER, 30);
            }
        } else {
            // 无人选择导弹时收起
            if (currentState == 2 || currentState == 1) {
                entityData.set(MISSILE_STATE, 3);
                entityData.set(DEPLOY_TIMER, 30);
            }
        }

        // 动画计时
        if (currentState == 1 || currentState == 3) {
            int timer = entityData.get(DEPLOY_TIMER) - 1;
            entityData.set(DEPLOY_TIMER, timer);
            if (timer <= 0) {
                entityData.set(MISSILE_STATE, currentState == 1 ? 2 : 0);
            }
        }
    }

    /**
     * 客户端导弹架动画状态机：
     *  - MISSILE_STATE 0=收起 1=展开中 2=展开 3=收起中
     *  动画由 M3A3Renderer 手动控制（绕开引擎 Z 方向问题），此处不再播放动画文件。
     */
    @OnlyIn(Dist.CLIENT)
    private void tickMissileAnimation() {
    }

    @Override
    public void vehicleShoot(LivingEntity living, UUID uuid, Vec3 targetPos) {
        int seatIndex = getSeatIndex(living);
        int selectedWeapon = seatIndex >= 0 ? getSelectedWeapon(seatIndex) : -1;

        // 客户端：主炮（索引 0）开火时播放一次性后坐动画
        if (level().isClientSide && selectedWeapon == 0) {
            var ani = getAnimationInstance();
            if (ani != null) {
                ani.getContext().playAnimation("animation.m3a3.main_cannon", AnimationPlayType.PLAY_ONCE_STOP, 0);
            }
        }

        if (seatIndex < 0) {
            super.vehicleShoot(living, uuid, targetPos);
            return;
        }
        if (selectedWeapon != 1) {
            super.vehicleShoot(living, uuid, targetPos);
            return;
        }

        if (isMoving()) {
            if (living instanceof Player player) {
                player.displayClientMessage(Component.translatable("message.dragonrise_reforge.m3a3_stop_to_shoot"), true);
            }
            return;
        }

        if (entityData.get(MISSILE_STATE) != 2) {
            if (living instanceof Player player) {
                player.displayClientMessage(Component.translatable("message.dragonrise_reforge.m3a3_wait_deploy"), true);
            }
            return;
        }

        super.vehicleShoot(living, uuid, targetPos);
    }

    /**
     * 获取导弹架状态（0=收起 1=展开中 2=展开 3=收起中）
     */
    public int getMissileState() {
        return this.entityData.get(MISSILE_STATE);
    }

    /**
     * 获取展开/收起剩余计时（tick，0~30）
     */
    public int getDeployTimer() {
        return this.entityData.get(DEPLOY_TIMER);
    }

public boolean shouldShowMissileOn(VehicleEntity vehicle, int missileWeaponIndex) {
        for (var passenger : vehicle.getPassengers()) {
            int seatIndex = vehicle.getSeatIndex(passenger);
            if (seatIndex < 0) continue;

            int currentWeaponIndex = vehicle.getSelectedWeapon(seatIndex);
            if (currentWeaponIndex != missileWeaponIndex) continue;

            var gunData = vehicle.getGunData(seatIndex);
            if (gunData != null && (gunData.ammo.get() > 0 || gunData.backupAmmoCount.get() > 0)) {
                return true;
            }
        }
        return false;
    }
}