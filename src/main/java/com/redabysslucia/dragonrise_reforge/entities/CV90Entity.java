package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.data.mob_guns.MobGunData;
import com.atsuishio.superbwarfare.entity.vehicle.base.GeoVehicleEntity;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

public class CV90Entity extends GeoVehicleEntity {

        public CV90Entity(EntityType<CV90Entity> type, Level world) {
                super(type, world);
        }

        @Override
        public DamageModifier getDamageModifier() {
                return super.getDamageModifier()
                        .custom((source, damage) -> getSourceAngle(source, 0.25f) * damage);
        }

    private PlayState cannonFirePredicate(AnimationState<CV90Entity> event) {
        if (getShootAnimationTimer(0, 0) > 0) {
            return event.setAndContinue(RawAnimation.begin().thenPlay("cv90.animation.maincannon"));
        }
        return event.setAndContinue(RawAnimation.begin().thenLoop("cv90.nothinghappen.new"));
    }

    public boolean countMissile(VehicleEntity vehicle, int ammo) {
        var gunData = vehicle.getGunData("Missile");
        if (gunData == null) {
            return false;
        } else {
            return gunData.ammo.get() < ammo;
        }
    }

    private PlayState MissileOn(AnimationState<CV90Entity> event) {
        if (countMissile(this,0) || countMissile(this,1)) {
            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold
                    ("cv90.missileoff.new"));
        }
        else {
            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold
                    ("cv90.missileon.new"));
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
        data.add(new AnimationController<>(this, "missileon", 0, this::MissileOn));
        data.add(new AnimationController<>(this, "cannon", 0, this::cannonFirePredicate));
    }
    
}
