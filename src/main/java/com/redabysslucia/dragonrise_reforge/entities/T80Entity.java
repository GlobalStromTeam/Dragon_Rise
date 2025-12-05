package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.entity.vehicle.base.GeoVehicleEntity;
import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
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
                        return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.ztz99a.fire"));
                }
                return event.setAndContinue(RawAnimation.begin().thenLoop("animation.ztz99a.idle"));
        }

        @Override
        public void registerControllers(AnimatableManager.ControllerRegistrar data) {
                data.add(new AnimationController<>(this, "cannon", 0, this::cannonFirePredicate));
        }
        @Override
        public void addPassenger(Entity passenger) {
                super.addPassenger(passenger);
                super.tick();

                if (passenger instanceof Player) {
                        ((Player) passenger).addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false));
                }
        }
        @Override
        public void removePassenger(Entity passenger) {
                super.removePassenger(passenger);

                if (passenger instanceof Player) {
                        ((Player) passenger).removeEffect(MobEffects.NIGHT_VISION);
                }
        }
}
