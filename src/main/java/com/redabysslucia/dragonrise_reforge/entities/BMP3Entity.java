package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import com.redabysslucia.dragonrise_reforge.entities.vehicle.IndirectFireVehicleBase;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

@SuppressWarnings("removal")
public class BMP3Entity extends IndirectFireVehicleBase {

        public BMP3Entity(EntityType<BMP3Entity> type, Level world) {
                super(type, world);
        }

        @Override
        public DamageModifier getDamageModifier() {
                return super.getDamageModifier()
                        .custom((entity, source, damage) -> getSourceAngle(source, 0.25f) * damage);
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
