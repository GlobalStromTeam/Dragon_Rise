package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
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
public class TYPE100Entity extends NightVisionVehicle {

        public TYPE100Entity(EntityType<?> pEntityType, Level pLevel) {
                super(pEntityType, pLevel);
        }

//        @Override
//        public void addPassenger(Entity passenger) {
//                super.addPassenger(passenger);
//                super.tick();
//
//                if (passenger instanceof Player player) {
//                        player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false));
//                }
//        }
//        @Override
//        public void removePassenger(Entity passenger) {
//                super.removePassenger(passenger);
//
//                if (passenger instanceof Player player) {
//                        player.removeEffect(MobEffects.NIGHT_VISION);
//                }
//        }

        @Override
        public ResourceLocation getNightVisionShader() {
                return new ResourceLocation("shaders/post/night-vision-wp.json");
        }
}
