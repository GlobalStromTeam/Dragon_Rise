package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import com.atsuishio.superbwarfare.entity.vehicle.base.GeoVehicleEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

@SuppressWarnings("removal")
public class BMPT72Entity extends GeoVehicleEntity {

        public BMPT72Entity(EntityType<BMPT72Entity> type, Level world) {
                super(type, world);
        }

        @Override
        public DamageModifier getDamageModifier() {
                return super.getDamageModifier()
                        .custom((source, damage) -> getSourceAngle(source, 0.25f) * damage);
        }

        private PlayState cannonShootPredicate(AnimationState<BMPT72Entity> event) {

                if (getShootAnimationTimer(0, 0) > 0) {
                        return event.setAndContinue(RawAnimation.begin().thenPlay("bmpt.maincannon.fire"));
                }
                if (getShootAnimationTimer(0, 1) > 0) {
                        return event.setAndContinue(RawAnimation.begin().thenPlay("bmpt.maincannon.fire"));
                }

                return event.setAndContinue(RawAnimation.begin().thenLoop("animation.unknown.new"));

        }

        @Override
        public void registerControllers(AnimatableManager.ControllerRegistrar data) {
                data.add(new AnimationController<>(this, "cannon", 0, this::cannonShootPredicate));
        }


}
