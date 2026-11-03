package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.client.animation.AnimationPlayType;
import com.atsuishio.superbwarfare.data.gun.GunData;
import com.atsuishio.superbwarfare.event.ClientEventHandler;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.redabysslucia.dragonrise_reforge.entities.utils.MissileFireGate;
import com.redabysslucia.dragonrise_reforge.entities.utils.M3A3ModelTransforms;
import com.redabysslucia.dragonrise_reforge.entities.utils.AddonArmorHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4d;
import org.joml.Quaterniond;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("removal")
public class M3A3Entity extends VehicleEntity implements AddonArmorVehicle {

    public static final EntityDataAccessor<Integer> MISSILE_STATE =
            SynchedEntityData.defineId(M3A3Entity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> DEPLOY_TIMER =
            SynchedEntityData.defineId(M3A3Entity.class, EntityDataSerializers.INT);

    /** 附加装甲（TUSK）是否已安装：驱动 {@code move_tusk} 骨骼显隐，同时决定最大生命值上限 */
    public static final EntityDataAccessor<Boolean> ADDON_ARMOR_INSTALLED =
            SynchedEntityData.defineId(M3A3Entity.class, EntityDataSerializers.BOOLEAN);

    /**
     * defineSynchedData 是否已经跑完。
     * super 的 defineSynchedData 会用 getMaxHealth() 初始化 HEALTH，那一刻本类的同步位还没定义，
     * 所以 getMaxHealth() 必须先判断这个标志，否则会抛 "数据参数未注册"。
     */
    private boolean addonArmorDataReady;

    /** 座位武器表里导弹的下标（m3a3.json 的 Seats[].Weapons = [Cannon, Missile, MainMachineGun]） */
    private static final int MISSILE_WEAPON_INDEX = 1;

    /** 发射架展开/收起一次所需的 tick，必须与 M3A3Renderer 里的 30 保持一致 */
    private static final int RACK_ANIM_TICKS = M3A3ModelTransforms.RACK_ANIM_TICKS;

    // 客户端动画状态机缓存
    private String lastMissileAnim = "";

    public M3A3Entity(EntityType<M3A3Entity> type, Level world) {
        super(type, world);
        getPositionTransform().put("Tow", this::getTowTransform);
        getRotationTransform().put("Tow", ticks -> getTowTransform(ticks).getNormalizedRotation(new Quaterniond()));
    }

    public float getMissileRackRoll() {
        return M3A3ModelTransforms.rackRollDegrees(getMissileState(), getDeployTimer());
    }

    public Matrix4d getTowTransform(float partialTicks) {
        return M3A3ModelTransforms.towTransform(getTurretTransform(partialTicks), getMissileRackRoll());
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(MISSILE_STATE, 0);
        this.entityData.define(DEPLOY_TIMER, 0);
        this.entityData.define(ADDON_ARMOR_INSTALLED, false);
        this.addonArmorDataReady = true;
    }

    @Override
    public float getMaxHealth() {
        return AddonArmorHelper.maxHealth(super.getMaxHealth(),
                this.addonArmorDataReady && this.entityData.get(ADDON_ARMOR_INSTALLED));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("TuskInstalled", hasAddonArmor());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("TuskInstalled")) {
            setAddonArmor(compound.getBoolean("TuskInstalled"));
        }
    }

    @Override
    public boolean hasAddonArmor() {
        return this.entityData.get(ADDON_ARMOR_INSTALLED);
    }

    @Override
    public void setAddonArmor(boolean installed) {
        this.entityData.set(ADDON_ARMOR_INSTALLED, installed);
    }


    private final Map<UUID, Integer> lastMessageTick = new HashMap<>();

    /**
     * 导弹炮位是否还有备弹（弹匣里的 + 备用弹药）。
     * 这是发射架自动展开的条件之一：没弹就没必要竖起来。
     */
    private boolean hasMissileAmmo() {
        GunData missile = getGunData("Missile");
        return missile != null && (missile.ammo.get() > 0 || missile.backupAmmoCount.get() > 0);
    }

    /**
     * 是否有乘员坐在能用导弹炮位的座位上。
     * 无人时发射架必须收起——否则空车停在场上也会一直竖着发射架。
     * 座位武器表里没有导弹这一格的座位不算数，所以只有炮手位能让它竖起来。
     */
    private boolean hasMissileCrew() {
        for (Entity passenger : getPassengers()) {
            int seatIndex = getSeatIndex(passenger);
            if (seatIndex >= 0 && getGunName(seatIndex, MISSILE_WEAPON_INDEX) != null) {
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
        if (selectedWeapon != MISSILE_WEAPON_INDEX) return super.canShoot(living);

        // 速度门槛：±14 km/h 以内允许发射（原先 0.01 格/tick 等于要求完全停下）
        if (!MissileFireGate.canFireMissile(this)) {
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

        // 展开条件：速度符合发射门槛 + 导弹有备弹 + 车上有炮手。
        // 不再要求乘客把武器切到导弹——只要"能打"，发射架就自己竖起来。
        boolean shouldDeploy = MissileFireGate.canFireMissile(this) && hasMissileAmmo() && hasMissileCrew();

        if (shouldDeploy) {
            // 收起过程中(3)被打断时也要能转回展开，不必等它先收完
            if (currentState == 0 || currentState == 3) {
                switchRackDirection(currentState, 1);
            }
        } else if (currentState == 2 || currentState == 1) {
            switchRackDirection(currentState, 3);
        }

        // 动画计时（用切换后的状态判断，避免同 tick 内刚反向就被判定为动画结束）
        int state = entityData.get(MISSILE_STATE);
        if (state == 1 || state == 3) {
            int timer = entityData.get(DEPLOY_TIMER) - 1;
            entityData.set(DEPLOY_TIMER, timer);
            if (timer <= 0) {
                entityData.set(MISSILE_STATE, state == 1 ? 2 : 0);
            }
        }
    }

    /**
     * 切换发射架的运动方向（1=展开中 / 3=收起中）。
     *
     * <p>中途反向时保留动画进度：状态 1 的进度是 {@code 1 - timer/30}，状态 3 的进度是
     * {@code timer/30}，两种互转都对应 {@code timer' = 30 - timer}，
     * 所以不会出现"竖到一半又从头竖"或"收到一半突然全展开"的跳变。</p>
     */
    private void switchRackDirection(int currentState, int newState) {
        int timer = RACK_ANIM_TICKS;
        if (currentState == 1 || currentState == 3) {
            timer = Math.max(1, RACK_ANIM_TICKS - entityData.get(DEPLOY_TIMER));
        }
        entityData.set(MISSILE_STATE, newState);
        entityData.set(DEPLOY_TIMER, timer);
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
        if (selectedWeapon != MISSILE_WEAPON_INDEX) {
            super.vehicleShoot(living, uuid, targetPos);
            return;
        }

        if (!MissileFireGate.canFireMissile(this)) {
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
