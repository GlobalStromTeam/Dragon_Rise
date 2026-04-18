package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import com.redabysslucia.dragonrise_reforge.entities.utils.FireLightVisionVehicle;
import lombok.val;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

import static com.atsuishio.superbwarfare.event.ClientEventHandler.zoomVehicle;

public class TJGCEntity extends FireLightVisionVehicle {

    public TJGCEntity(EntityType<TJGCEntity> type, Level world) {
        super(type, world);
    }

    @Override
    public DamageModifier getDamageModifier() {
        return super.getDamageModifier()
                .custom((source, damage) -> getSourceAngle(source, 0.25f) * damage * (getHealth() > 0.1f ? 0.4f : 0.05f));
    }

    private PlayState Sb(AnimationState<TJGCEntity> event) {
        if (this.onGround()) {
            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold
                    ("on"));
        }
        else {
        return event.setAndContinue(RawAnimation.begin().thenPlayAndHold
                ("off"));
        }
    }

    private PlayState Sb1(AnimationState<TJGCEntity> event) {
        if (this.sprintInputDown()) {
            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold
                    ("enginon"));
        }
        else {
            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold
                    ("enginoff"));
        }
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

    private PlayState MissileOn(AnimationState<TJGCEntity> event) {
        if (shouldShowMissileOn(this, 1)) {
            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold
                    ("missileon"));
        } else {
            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold
                    ("missileoff"));
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
        data.add(new AnimationController<>(this, "sb", 0, this::Sb));
        data.add(new AnimationController<>(this, "sb1", 0, this::Sb1));
        data.add(new AnimationController<>(this, "missileon", 0, this::MissileOn));
    }



//    @Override
//    public boolean useAircraftCamera(int seatIndex) {
//        return ModKeyMappings.FREE_CAMERA.isDown() && !ClientEventHandler.zoom;
//    }

    @Override
    public double getMouseSensitivity() {
        return zoomVehicle ? 0.1 : 0.25;
    }
}
