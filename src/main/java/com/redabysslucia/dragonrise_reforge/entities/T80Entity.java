package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.entity.vehicle.base.GeoVehicleEntity;
import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

public class T80Entity extends GeoVehicleEntity {
        public T80Entity(EntityType<?> pEntityType, Level pLevel) {
                super(pEntityType, pLevel);
        }
        @Override
        public DamageModifier getDamageModifier() {
                return super.getDamageModifier()
                        .custom((source, damage) -> getSourceAngle(source, 0.3f) * damage);
        }
        private PlayState cannonFirePredicate(AnimationState<T80Entity> event) {
                if (getShootAnimationTimer(0, 0) > 0) {
                        return event.setAndContinue(RawAnimation.begin().thenPlay("animation.t80.fire"));
                }
                return event.setAndContinue(RawAnimation.begin().thenLoop("animation.t80.idle"));
        }

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
