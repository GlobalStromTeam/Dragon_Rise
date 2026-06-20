package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.data.gun.GunData;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import com.atsuishio.superbwarfare.entity.vehicle.base.GeoVehicleEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.PlayState;

@SuppressWarnings("removal")
public class M3A3Entity extends GeoVehicleEntity {

        public M3A3Entity(EntityType<M3A3Entity> type, Level world) {
                super(type, world);
        }

        @Override
        public DamageModifier getDamageModifier() {
                return super.getDamageModifier()
                        .custom((source, damage) -> getSourceAngle(source, 0.25f) * damage);
        }

    private PlayState cannonFirePredicate(AnimationState<M3A3Entity> event) {
        if (getShootAnimationTimer(0, 0) > 0) {
            return event.setAndContinue(RawAnimation.begin().thenPlay("m3a3.animation.maincannon"));
        }
        return event.setAndContinue(RawAnimation.begin().thenLoop("m3a3.nothinghappen.new"));
    }

    public boolean shouldShowMissileOn(VehicleEntity vehicle, int missileWeaponIndex) {
        net.minecraft.world.entity.Entity driver = vehicle.getFirstPassenger();
        if (driver == null) return false;

        int seatIndex = vehicle.getSeatIndex(driver);
        if (seatIndex < 0) return false;

        int currentWeaponIndex = vehicle.getSelectedWeapon(seatIndex);
        if (currentWeaponIndex != missileWeaponIndex) return false;

        GunData gunData = vehicle.getGunData(seatIndex);
        return gunData != null && (gunData.ammo.get() > 0 || gunData.backupAmmoCount.get() > 0);
    }

    private PlayState MissileOn(AnimationState<M3A3Entity> event) {
        if (shouldShowMissileOn(this, 1)) {
            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold
                    ("m3a3.antimation.missileon"));
        } else {
            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold
                    ("m3a3.animation.missileoff"));
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
        data.add(new AnimationController<>(this, "missileon", 0, this::MissileOn));
        data.add(new AnimationController<>(this, "cannon", 0, this::cannonFirePredicate));
    }


}
