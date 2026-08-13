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

public class REFALEEntity extends FireLightVisionVehicle {

    public REFALEEntity(EntityType<REFALEEntity> type, Level world) {
        super(type, world);
    }

    @Override
    public DamageModifier getDamageModifier() {
        return super.getDamageModifier()
                .custom((entity, source, damage) -> getSourceAngle(source, 0.25f) * damage * (getHealth() > 0.1f ? 0.4f : 0.05f));
    }

    private PlayState Sb(AnimationState<REFALEEntity> event) {
        if (this.onGround()) {
            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold
                    ("animation.j10.ONground_idle2"));
        }
        else {
        return event.setAndContinue(RawAnimation.begin().thenPlayAndHold
                ("animation.j10.OFFground_idle"));
        }
    }

    private PlayState Sb1(AnimationState<REFALEEntity> event) {
        if (this.sprintInputDown()) {
            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold
                    ("animation.j10.engon"));
        }
        else {
            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold
                    ("animation.j10.engoFF"));
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
        data.add(new AnimationController<>(this, "sb", 0, this::Sb));
        data.add(new AnimationController<>(this, "sb1", 0, this::Sb1));
    }



//    @Override
//    public boolean useAircraftCamera(int seatIndex) {
//        return ModKeyMappings.FREE_CAMERA.isDown() && !ClientEventHandler.zoom;
//    }

    @Override
    public double getMouseSensitivity() {
        return zoomVehicle ? 0.1 : 0.25;
    }
}
