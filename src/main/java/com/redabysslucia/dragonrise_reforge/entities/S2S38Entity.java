package com.redabysslucia.dragonrise_reforge.entities;

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
public class S2S38Entity extends GeoVehicleEntity {

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


}
