package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.redabysslucia.dragonrise_reforge.entities.utils.SyncCameraVehicle;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

@SuppressWarnings("removal")
public class type3Entity extends SyncCameraVehicle {
    public type3Entity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }


//    private PlayState cannonFirePredicate(AnimationState<PANZER4Entity> event) {
//        if (getShootAnimationTimer(0, 0) > 0) {
//            return event.setAndContinue(RawAnimation.begin().thenPlay("ztq15.fire.animation"));
//        }
//        return event.setAndContinue(RawAnimation.begin().thenLoop("animation.ztq15.idle"));
//    }
//
//    @Override
//    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
//        data.add(new AnimationController<>(this, "cannon", 0, this::cannonFirePredicate));
//    }

    @Override
    public int getTrackAnimationLength() {
        return 80;
    }

    @Override
    public float getTurretMaxHealth() {
        return 100;
    }

    @Override
    public float getWheelMaxHealth() {
        return 100;
    }

    @Override
    public float getEngineMaxHealth() {
        return 150;
    }
}
