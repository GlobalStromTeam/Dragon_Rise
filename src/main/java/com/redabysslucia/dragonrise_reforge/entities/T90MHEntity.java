package com.redabysslucia.dragonrise_reforge.entities;

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
public class T90MHEntity extends GeoVehicleEntity {

        public T90MHEntity(EntityType<?> pEntityType, Level pLevel) {
                super(pEntityType, pLevel);
        }

//        @Override
//        public DamageModifier getDamageModifier() {
//                return super.getDamageModifier()
//                        .custom((source, damage) -> getSourceAngle(source, 0.3f) * damage);
//        }
//        private PlayState cannonFirePredicate(AnimationState<T90MHEntity> event) {
//                if (getShootAnimationTimer(0, 0) > 0) {
//                        return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.t80.fire"));
//                }
//                return event.setAndContinue(RawAnimation.begin().thenLoop("animation.t80.idle"));
//        }
//
//        @Override
//        public void registerControllers(AnimatableManager.ControllerRegistrar data) {
//                data.add(new AnimationController<>(this, "cannon", 0, this::cannonFirePredicate));
//        }


}
