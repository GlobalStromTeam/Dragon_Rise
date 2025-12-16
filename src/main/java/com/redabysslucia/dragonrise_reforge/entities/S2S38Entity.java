package com.redabysslucia.dragonrise_reforge.entities;

import com.redabysslucia.dragonrise_reforge.entities.utils.NightVisionVehicle;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

@SuppressWarnings("removal")
public class S2S38Entity extends NightVisionVehicle {

        public S2S38Entity(EntityType<?> pEntityType, Level pLevel) {
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

        @Override
        public ResourceLocation getNightVisionShader() {
                return new ResourceLocation("shaders/post/night-vision-wp.json");
        }
}
