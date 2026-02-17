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
public class T3476Entity extends NightVisionVehicle {

        public T3476Entity(EntityType<T3476Entity> type, Level world) {
                super(type, world);
        }

        @Override
        public DamageModifier getDamageModifier() {
                return super.getDamageModifier()
                        .custom((source, damage) -> getSourceAngle(source, 0.25f) * damage);
        }

    private PlayState radar(AnimationState<T3476Entity> event) {

        return event.setAndContinue(RawAnimation.begin().thenLoop("625.animation.radar"));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
        data.add(new AnimationController<>(this, "radar", 0, this::radar));
    }

    @Override
    public ResourceLocation getNightVisionShader() {
        return new ResourceLocation("shaders/post/night-vision-wp.json");
    }
}
