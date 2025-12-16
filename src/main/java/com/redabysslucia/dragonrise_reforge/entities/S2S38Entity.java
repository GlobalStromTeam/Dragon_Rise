package com.redabysslucia.dragonrise_reforge.entities;

import com.redabysslucia.dragonrise_reforge.entities.utils.NightVisionVehicle;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

@SuppressWarnings("removal")
public class S2S38Entity extends NightVisionVehicle {

        public S2S38Entity(EntityType<?> pEntityType, Level pLevel) {
                super(pEntityType, pLevel);
        }

        private PlayState S2S38Fire(AnimationState<S2S38Entity> event) {
                if (getShootAnimationTimer(0, 0) > 0) {
                        return event.setAndContinue(RawAnimation.begin().thenLoop("animation.2s38.fire"));
                }
                return event.setAndContinue(RawAnimation.begin().thenLoop("animation.2s38.idle"));
        }

        @Override
        public void registerControllers(AnimatableManager.ControllerRegistrar data) {
                data.add(new AnimationController<>(this, "2s38fire", 0, this::S2S38Fire));
        }

        @Override
        public ResourceLocation getNightVisionShader() {
                return new ResourceLocation("shaders/post/night-vision-wp.json");
        }
}
