package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.event.ClientEventHandler;
import com.atsuishio.superbwarfare.entity.vehicle.base.GeoVehicleEntity;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import lombok.val;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

import java.util.UUID;

@SuppressWarnings("removal")
public class M3A3Entity extends GeoVehicleEntity {

    public static final EntityDataAccessor<Integer> MISSILE_STATE =
            SynchedEntityData.defineId(M3A3Entity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> DEPLOY_TIMER =
            SynchedEntityData.defineId(M3A3Entity.class, EntityDataSerializers.INT);

    public M3A3Entity(EntityType<M3A3Entity> type, Level world) {
        super(type, world);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(MISSILE_STATE, 0);
        this.entityData.define(DEPLOY_TIMER, 0);
    }

    @Override
    public DamageModifier getDamageModifier() {
        return super.getDamageModifier()
                .custom((source, damage) -> getSourceAngle(source, 0.25f) * damage);
    }

    private int lastMessageTick = -100;

    private boolean isMoving() {
        Vec3 motion = this.getDeltaMovement();
        return Math.abs(motion.x) > 0.01 || Math.abs(motion.z) > 0.01;
    }

    private void showClientMessage(LivingEntity living, String key) {
        if (level().isClientSide && living instanceof Player player) {
            if (ClientEventHandler.holdFireVehicle && tickCount - lastMessageTick > 20) {
                lastMessageTick = tickCount;
                player.displayClientMessage(Component.translatable(key), true);
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
        if (level().isClientSide) return;

        int currentState = entityData.get(MISSILE_STATE);
        var driver = getFirstPassenger();
        int seatIndex = driver != null ? getSeatIndex(driver) : -1;
        int selectedWeapon = seatIndex >= 0 ? getSelectedWeapon(seatIndex) : -1;
        boolean isMissileSelected = selectedWeapon == 1;
        boolean moving = isMoving();

        if (moving) {
            if (currentState == 2) {
                entityData.set(MISSILE_STATE, 3);
                entityData.set(DEPLOY_TIMER, 30);
            } else if (currentState == 1) {
                entityData.set(MISSILE_STATE, 3);
                entityData.set(DEPLOY_TIMER, 30);
            }
        } else if (isMissileSelected) {
            if (currentState == 0) {
                entityData.set(MISSILE_STATE, 1);
                entityData.set(DEPLOY_TIMER, 30);
            }
        } else {
            if (currentState == 2) {
                entityData.set(MISSILE_STATE, 3);
                entityData.set(DEPLOY_TIMER, 30);
            }
        }

        if (currentState == 1 || currentState == 3) {
            int timer = entityData.get(DEPLOY_TIMER) - 1;
            entityData.set(DEPLOY_TIMER, timer);
            if (timer <= 0) {
                entityData.set(MISSILE_STATE, currentState == 1 ? 2 : 0);
            }
        }
    }

    @Override
    public void vehicleShoot(LivingEntity living, UUID uuid, Vec3 targetPos) {
        int seatIndex = getSeatIndex(living);
        if (seatIndex < 0) {
            super.vehicleShoot(living, uuid, targetPos);
            return;
        }
        int selectedWeapon = getSelectedWeapon(seatIndex);
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

    private PlayState cannonFirePredicate(AnimationState<M3A3Entity> event) {
        if (getShootAnimationTimer(0, 0) > 0) {
            return event.setAndContinue(RawAnimation.begin().thenPlay("m3a3.animation.maincannon"));
        }
        return event.setAndContinue(RawAnimation.begin().thenLoop("m3a3.nothinghappen.new"));
    }

    public boolean shouldShowMissileOn(VehicleEntity vehicle, int missileWeaponIndex) {
        val driver = vehicle.getFirstPassenger();
        if (driver == null) return false;

        val seatIndex = vehicle.getSeatIndex(driver);
        if (seatIndex < 0) return false;

        val currentWeaponIndex = vehicle.getSelectedWeapon(seatIndex);
        if (currentWeaponIndex != missileWeaponIndex) return false;

        val gunData = vehicle.getGunData(seatIndex);
        return gunData != null && (gunData.ammo.get() > 0 || gunData.backupAmmoCount.get() > 0);
    }

    private PlayState MissileOn(AnimationState<M3A3Entity> event) {
        int state = entityData.get(MISSILE_STATE);
        if (state == 1) {
            return event.setAndContinue(RawAnimation.begin().thenPlay("m3a3.animation.missile_deploy"));
        } else if (state == 3) {
            return event.setAndContinue(RawAnimation.begin().thenPlay("m3a3.animation.missile_retract"));
        } else if (state == 2) {
            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("m3a3.animation.missile_hold"));
        } else {
            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("m3a3.animation.missile_off_hold"));
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
        data.add(new AnimationController<>(this, "missileon", 0, this::MissileOn));
        data.add(new AnimationController<>(this, "cannon", 0, this::cannonFirePredicate));
    }

}