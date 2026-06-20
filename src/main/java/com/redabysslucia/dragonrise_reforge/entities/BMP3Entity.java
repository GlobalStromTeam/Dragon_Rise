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
public class BMP3Entity extends GeoVehicleEntity {

        public BMP3Entity(EntityType<BMP3Entity> type, Level world) {
                super(type, world);
        }

        @Override
        public DamageModifier getDamageModifier() {
                return super.getDamageModifier()
                        .custom((source, damage) -> getSourceAngle(source, 0.25f) * damage);
        }

        private PlayState cannonShootPredicate(AnimationState<BMP3Entity> event) {

                if (getShootAnimationTimer(0, 0) > 0) {
                        return event.setAndContinue(RawAnimation.begin().thenPlay("bmp3.animation.subcannon.new"));
                }
                if (getShootAnimationTimer(0, 1) > 0) {
                        return event.setAndContinue(RawAnimation.begin().thenPlay("bmp3.animation.minecannon.new"));
                }

                return event.setAndContinue(RawAnimation.begin().thenLoop("animation.unknown.new"));

        }

        @Override
        public void registerControllers(AnimatableManager.ControllerRegistrar data) {
                data.add(new AnimationController<>(this, "cannon", 0, this::cannonShootPredicate));
        }


}
