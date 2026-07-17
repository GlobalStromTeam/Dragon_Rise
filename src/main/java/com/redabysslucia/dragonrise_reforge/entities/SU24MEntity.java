package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import com.redabysslucia.dragonrise_reforge.entities.utils.FireLightVisionVehicle;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

import static com.atsuishio.superbwarfare.event.ClientEventHandler.zoomVehicle;

public class SU24MEntity extends FireLightVisionVehicle {

    public SU24MEntity(EntityType<SU24MEntity> type, Level world) {
        super(type, world);
    }

    private PlayState A12(AnimationState<SU24MEntity> event) {
        if (this.onGround()) {
            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold
                    ("animation"));
        } else {
            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold
                    ("animation2"));
        }
    }
    private PlayState A34(AnimationState<SU24MEntity> event) {
        if (this.getPower() >= 0.6) {
            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold
                    ("add"));
        } else {
            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold
                    ("add2"));
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
        data.add(new AnimationController<>(this, "A12", 0, this::A12));
        data.add(new AnimationController<>(this, "A34", 0, this::A34));
    }
}